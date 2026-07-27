package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能位置（Functions / SFI）。对应 mock 的 functions。
 * 父子关系、关键性、位置、安装的组件均以字符串业务编码表达，
 * 与前端以编码（functionNo / criticality degree / 组件编号）建模的语义一致。
 */
@Entity
@Table(name = "maintenance_function")
@Data
@NoArgsConstructor
public class MaintenanceFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "function_no", nullable = false)
    private String functionNo;

    private String installation;
    private String department;
    private String description;
    private String reference;
    private String parentFunctionNo;
    private String status;
    private String location;
    private String criticality;
    private String installedComponentNo;

    @Column(name = "sfi_code")
    private String sfiCode;
    private String system;
    @Column(name = "sub_system")
    private String subSystem;
    private String remarks;
    @Column(name = "serial_no")
    private String serialNo;
    private String maker;
    private String model;
    @Column(name = "tag_no")
    private String tagNo;
    @Column(name = "asset_value")
    private BigDecimal assetValue;
    @Column(name = "acquisition_date")
    private String acquisitionDate;
    private String currency;
    private BigDecimal depreciation;

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FunctionCounter> functionCounters = new ArrayList<>();
}
