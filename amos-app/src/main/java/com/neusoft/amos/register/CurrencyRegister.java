package com.neusoft.amos.register;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册表：币种（手册 Chapter 4 Purchasing / Financials）。被 vendors / purchase_forms / budgets 引用。
 */
@Entity
@Table(name = "currency_register")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String name;
    private String symbol;

    @Column(nullable = false)
    private String status = "ACTIVE";
}
