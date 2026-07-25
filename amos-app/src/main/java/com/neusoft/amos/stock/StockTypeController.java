package com.neusoft.amos.stock;

import com.neusoft.amos.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock/stock-types")
public class StockTypeController
        extends AbstractCrudController<StockType, Long, StockTypeRepository> {

    public StockTypeController(StockTypeRepository repository) {
        super(repository);
    }
}
