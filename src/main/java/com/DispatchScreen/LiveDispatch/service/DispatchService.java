package com.DispatchScreen.LiveDispatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DispatchService {

    @Autowired
    private JdbcTemplate jdbc; // single datasource

    // Slot representation
    public static class Slot {
        public final LocalDateTime start;
        public final LocalDateTime end;
        public final String label;

        public Slot(LocalDateTime s, LocalDateTime e, String label) {
            this.start = s;
            this.end = e;
            this.label = label;
        }
    }

    // Shift info including detected current slot label
    public static class ShiftInfo {
        public final LocalDateTime shiftStart;
        public final LocalDateTime shiftEnd;
        public final List<Slot> slots;
        public String currentSlotLabel = null;

        public ShiftInfo(LocalDateTime s, LocalDateTime e, List<Slot> slots) {
            this.shiftStart = s;
            this.shiftEnd = e;
            this.slots = slots;
        }
    }

    // Cell data used by the UI
    public static class CellData {
        public String cellname;
        public List<Integer> partIds = new ArrayList<>();
        public long totalTarget = 0L;
        public Map<String, Long> cumulativeTargetBySlot = new LinkedHashMap<>();
        public Map<String, Long> cumulativeActualBySlot = new LinkedHashMap<>();
        public Map<String, String> slotClasses = new HashMap<>();
        public long totalActual = 0L;

        public String getCellname() {
            return cellname;
        }
    }

    // fetch master rows (cellname, part_id, daily_target)
    public List<Map<String, Object>> fetchMasterRows(int screenNo, int plantId) {
        String sql = "SELECT cellname, part_id, daily_target FROM dispatch_screen_master WHERE screen_no = ? AND plnt_id = ?";
        return jdbc.queryForList(sql, screenNo, plantId);
    }

    // consolidate rows into cells
    public List<CellData> consolidateCells(List<Map<String, Object>> rows) {
        Map<String, CellData> map = new LinkedHashMap<>();
        if (rows == null) return new ArrayList<>();

        for (Map<String, Object> r : rows) {
            String cell = String.valueOf(r.get("cellname"));
            //System.out.println("cell names " + r.get("cellname"));
            Integer partId = Integer.parseInt(String.valueOf(r.get("part_id")));
            Long dailyTarget = r.get("daily_target") == null ? 0L : Long.valueOf(String.valueOf(r.get("daily_target")));

            CellData cd = map.computeIfAbsent(cell, k -> {
                CellData c = new CellData();
                c.cellname = k;
                return c;
            });

            cd.partIds.add(partId);
            cd.totalTarget += dailyTarget;
        }
        return new ArrayList<>(map.values());
    }

    // build shift and 6 two-hour slots; detect which slot contains now()
    public ShiftInfo buildShiftAndSlotsForNow() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        LocalDateTime start;
        List<Slot> slots = new ArrayList<>();

        if (hour >= 7 && hour < 19) {
            // Day shift 07:00 - 19:00
            LocalDate today = now.toLocalDate();
            start = LocalDateTime.of(today, LocalTime.of(7, 0));
        } else {
            // Night shift 19:00 - next day 07:00
            LocalDate today = now.toLocalDate();
            start = (hour >= 19) ? LocalDateTime.of(today, LocalTime.of(19, 0))
                    : LocalDateTime.of(today.minusDays(1), LocalTime.of(19, 0));
        }

        for (int i = 0; i < 6; ++i) {
            LocalDateTime a = start.plusHours(2L * i);
            LocalDateTime b = a.plusHours(2);
            String label = String.format("%02d:00 - %02d:00", a.getHour(), b.getHour());
            slots.add(new Slot(a, b, label));
        }

        ShiftInfo info = new ShiftInfo(start, start.plusHours(12), slots);

        // detect current slot label
        for (Slot s : slots) {
            if (!now.isBefore(s.start) && now.isBefore(s.end)) {
                info.currentSlotLabel = s.label;
                break;
            }
        }
        return info;
    }

    // populate cumulative target/actual and css classes
    public void populateCumulativeValues(List<CellData> cells, ShiftInfo shiftInfo, int plantId) {
        if (cells == null || cells.isEmpty()) return;

        List<Slot> slots = shiftInfo.slots;
        List<String> slotLabels = slots.stream().map(s -> s.label).collect(Collectors.toList());
        int slotCount = slotLabels.size();

        // gather part ids
        Set<Integer> allPartIds = new LinkedHashSet<>();
        for (CellData c : cells) allPartIds.addAll(c.partIds);
        if (allPartIds.isEmpty()) {
            // still fill targets with zeros
            for (CellData cell : cells) {
                long shiftTarget = cell.totalTarget / 2; // safe fallback
                long perSlotBase = (slotCount==0?0:shiftTarget / slotCount);
                long remainder = (slotCount==0?0:shiftTarget % slotCount);
                long cumulative = 0L;
                for (int i = 0; i < slotCount; ++i) {
                    long add = perSlotBase + (i == slotCount-1 ? remainder : 0L);
                    cumulative += add;
                    cell.cumulativeTargetBySlot.put(slotLabels.get(i), cumulative);
                    cell.cumulativeActualBySlot.put(slotLabels.get(i), 0L);
                    cell.slotClasses.put(slotLabels.get(i), "actual-none");
                }
                cell.totalTarget = shiftTarget;
                cell.totalActual = 0L;
            }
            return;
        }

        // build SQL to fetch actuals for shift
        String placeholders = allPartIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT od.itemid, om.out_date, SUM(od.qty) AS qty " +
                "FROM owmain om JOIN owdetail od ON om.ownssid = od.ownssid " +
                "WHERE om.iplantid = ? AND od.itemid IN (" + placeholders + ") " +
                "AND om.out_date >= ? AND om.out_date < ? " +
                "GROUP BY od.itemid, om.out_date";

        List<Object> params = new ArrayList<>();
        params.add(plantId);
        // add all part ids in same order as placeholders
        for (Integer pid : allPartIds) params.add(pid);
        params.add(Timestamp.valueOf(shiftInfo.shiftStart));
        params.add(Timestamp.valueOf(shiftInfo.shiftEnd));

        List<Map<String, Object>> rows = jdbc.queryForList(sql, params.toArray());

        // group rows by itemid
        Map<Integer, List<Map<String,Object>>> byItem = new HashMap<>();
        for (Map<String, Object> r : rows) {
            Integer itemid = Integer.parseInt(String.valueOf(r.get("itemid")));
            byItem.computeIfAbsent(itemid, k -> new ArrayList<>()).add(r);
        }

        // compute per cell
        for (CellData cell : cells) {

            long fullDayTarget = cell.totalTarget; // from master (24h)
            long shiftTarget = fullDayTarget / 2;  // 12h = 50%
            int sc = slotCount;
            long[] perSlotActual = new long[sc];

            // map production rows into slots
            for (Integer pid : cell.partIds) {
                List<Map<String,Object>> list = byItem.get(pid);
                if (list == null) continue;

                for (Map<String, Object> r : list) {
                    Timestamp ts = (Timestamp) r.get("out_date");
                    long qty = ((Number) r.get("qty")).longValue();
                    LocalDateTime ldt = ts.toLocalDateTime();

                    for (int si = 0; si < sc; ++si) {
                        Slot slot = slots.get(si);
                        if (!ldt.isBefore(slot.start) && ldt.isBefore(slot.end)) {
                            perSlotActual[si] += qty;
                            break;
                        }
                    }
                }
            }

            // distribute shift target across slots, produce cumulative targets & actuals
            long perSlotBase = (sc == 0 ? 0L : shiftTarget / sc);
            long remainder = (sc == 0 ? 0L : shiftTarget % sc);

            cell.totalTarget = shiftTarget;
            long cumulativeTarget = 0L;
            long cumulativeActual = 0L;

            for (int si = 0; si < sc; ++si) {
                long addTarget = perSlotBase + ((si == sc - 1) ? remainder : 0L);
                cumulativeTarget += addTarget;
                cumulativeActual += perSlotActual[si];

                String label = slotLabels.get(si);
                cell.cumulativeTargetBySlot.put(label, cumulativeTarget);
                cell.cumulativeActualBySlot.put(label, cumulativeActual);

                String css;
                if (cumulativeActual == 0L) css = "actual-none";
                else if (cumulativeActual < cumulativeTarget) css = "actual-bad";
                else css = "actual-good";
                cell.slotClasses.put(label, css);
            }

            cell.totalActual = cumulativeActual;
        }
    }
}
