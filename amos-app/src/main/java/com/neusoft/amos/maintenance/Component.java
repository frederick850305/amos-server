package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 实际安装的设备实例。对应 mock 的 components。
 * componentCounters 为子表（手册 P44-45：活塞 dependsOn 主机读数级联）。
 */
@Entity
@Table(name = "component")
@Data
@NoArgsConstructor
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String number;

    private String typeNumber;
    private String name;
    private String status;
    private String maker;
    private String type;
    private String serialNo;
    private String location;
    private String department;
    private String vendor;
    private String functionNo;
    private String installDate;
    private String installation;

    private String parentComponent;
    private String componentTypeModel;
    private String dateCreated;
    private String dateModified;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComponentCounter> componentCounters = new ArrayList<>();

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComponentMeasurePoint> componentMeasurePoints = new ArrayList<>();
}
