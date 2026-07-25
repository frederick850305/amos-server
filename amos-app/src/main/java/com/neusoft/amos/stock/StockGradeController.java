package com.neusoft.amos.stock;

import com.neusoft.amos.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock/stock-grades")
public class StockGradeController
        extends AbstractCrudController<StockGrade, Long, StockGradeRepository> {

    public StockGradeController(StockGradeRepository repository) {
        super(repository);
    }
}
