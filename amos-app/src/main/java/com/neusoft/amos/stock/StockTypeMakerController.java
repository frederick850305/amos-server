package com.neusoft.amos.stock;

import com.neusoft.amos.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock/stock-type-makers")
public class StockTypeMakerController
        extends AbstractCrudController<StockTypeMaker, Long, StockTypeMakerRepository> {

    public StockTypeMakerController(StockTypeMakerRepository repository) {
        super(repository);
    }
}
