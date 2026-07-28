package com.neusoft.amos.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounterReadingLogRepository extends JpaRepository<CounterReadingLog, Long> {

    List<CounterReadingLog> findByComponentNoContainingIgnoreCase(String componentNo);

    List<CounterReadingLog> findByFunctionNoContainingIgnoreCase(String functionNo);

    List<CounterReadingLog> findByComponentCounterId(Long componentCounterId);
}
