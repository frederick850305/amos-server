package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组件档案（手册 Component Archives）。三种 kind：component / transfer / status。
 */
@Entity
@Table(name = "component_archive")
@Data
@NoArgsConstructor
public class ComponentArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String componentNo;
    private String kind;
    private String fromDepartment;
    private String toDepartment;
    private String archiveDate;
    private String data;
}
