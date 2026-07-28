package com.neusoft.amos.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeasurePointReadingLogRepository extends JpaRepository<MeasurePointReadingLog, Long> {

    List<MeasurePointReadingLog> findByComponentNoContainingIgnoreCase(String componentNo);

    List<MeasurePointReadingLog> findByFunctionNoContainingIgnoreCase(String functionNo);

    List<MeasurePointReadingLog> findByComponentMeasurePointId(Long componentMeasurePointId);
}
