package com.DispatchScreen.LiveDispatch.repository;

import com.DispatchScreen.LiveDispatch.model.DispatchScreenMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DispatchRepository extends JpaRepository<DispatchScreenMaster, Integer> {
    List<DispatchScreenMaster> findByScreenNo(int screenNo);
}

