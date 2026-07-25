package com.neusoft.amos.register;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册表：合格制造商（Address Register）。
 * 对应 mock/index.js 的 makerRegistry；被 stockTypes / components 的 maker 字段引用。
 */
@Entity
@Table(name = "maker_register")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakerRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String name;
}
