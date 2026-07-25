package com.neusoft.amos.register;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册表：供应商（手册 Chapter 3 Stock Type Vendors / Chapter 4 Purchasing）。
 * 被 stock_types、purchase_forms 的 vendor 字段引用。
 */
@Entity
@Table(name = "vendor_register")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String vendorNo;

    private String name;
    private String country;
    private String currency;
    private String paymentTerms;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(length = 1000)
    private String remarks;
}
