package com.neusoft.amos.stock;

import com.neusoft.amos.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock/stock-type-vendors")
public class StockTypeVendorController
        extends AbstractCrudController<StockTypeVendor, Long, StockTypeVendorRepository> {

    public StockTypeVendorController(StockTypeVendorRepository repository) {
        super(repository);
    }
}
