package com.neusoft.amos.register;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册表：预算代码（手册 Chapter 5 Budget Codes, printed page 348）。被 budgets.budget_code 引用。
 */
@Entity
@Table(name = "budget_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String name;
    private String parentBudgetCode;
    private String description;

    @Column(nullable = false)
    private String status = "ACTIVE";
}
