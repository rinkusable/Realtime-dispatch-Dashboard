package com.DispatchScreen.LiveDispatch.controller;

import com.DispatchScreen.LiveDispatch.service.DispatchService;
import com.DispatchScreen.LiveDispatch.service.DispatchService.CellData;
import com.DispatchScreen.LiveDispatch.service.DispatchService.ShiftInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ScreenController {

    @Autowired
    private DispatchService service;

    @GetMapping("/screen")
    public String displayScreen(@RequestParam(defaultValue = "1") int no,
                                @RequestParam(defaultValue = "1115") int plantId,
                                Model model) {

        List<java.util.Map<String, Object>> rows = service.fetchMasterRows(no, plantId);
        List<CellData> cells = service.consolidateCells(rows);

        ShiftInfo shiftInfo = service.buildShiftAndSlotsForNow();
        service.populateCumulativeValues(cells, shiftInfo, plantId);

        model.addAttribute("cells", cells);
        model.addAttribute("slots", shiftInfo.slots);
        model.addAttribute("currentSlot", shiftInfo.currentSlotLabel);
        model.addAttribute("screen_no", no);
        model.addAttribute("plnt_id", plantId);
        model.addAttribute("lastUpdate", LocalDateTime.now());

        return "screen";
    }
}
