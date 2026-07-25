# AMOS Data Model Draft

This is the first-pass logical data model for the AMOS rewrite. It is intentionally modular and should be refined module by module before each Flyway migration is created.

## 1. Modeling Rules

- Use numeric surrogate primary keys for database joins.
- Keep original AMOS-like business numbers as unique or scoped unique fields.
- Use explicit enums for states and document types.
- Keep audit/history records immutable.
- Do not physically delete business records once workflows depend on them; prefer status changes.
- Include `installation` for vessel-scoped records.
- Include `department` for work-scope records.
- Use command tables/history tables for important transitions where needed.

## 2. System And Scope

### 2.1 installation

Represents a vessel or installation.

| Field | Notes |
| --- | --- |
| `id` | PK |
| `code` | unique business code, e.g. `Traveller` |
| `name` | display name |
| `status` | active/inactive |

### 2.2 department

Represents an AMOS working department under an installation.

| Field | Notes |
| --- | --- |
| `id` | PK |
| `installation_id` | FK |
| `code` | department code |
| `name` | display name |
| `status` | active/inactive |

Unique:

- `(installation_id, code)`

### 2.3 amos_user / role / user_department_access

Supports login, permissions, and department switching.

Minimum fields:

- user: username, display name, password hash, status.
- role: code, name.
- user-role: user id, role id.
- user-department-access: user id, installation id, department id.

## 3. Registers

### 3.1 maker_register

Already exists.

Recommended fields:

- id
- code
- name
- status
- remarks

### 3.2 vendor_register

Needed by stock and purchasing.

Recommended fields:

- id
- vendor_no
- name
- country
- currency
- payment_terms
- status

### 3.3 location_register

Needed by components, stock items, deliveries, and transport.

Recommended fields:

- id
- installation_id
- code
- name
- parent_location_id
- location_type
- status

### 3.4 function_criticality

Manual Chapter 2 Function Criticality.

Recommended fields:

- id
- degree
- description
- color
- sort_order
- active

## 4. Maintenance

### 4.1 component_type

Already exists as starter.

Recommended fields:

- id
- type_number
- name
- maker
- model
- type
- class_code
- preferred_vendor_id
- parent_component_type_id
- description
- status
- date_created
- date_modified
- comp_type_model

Child tables:

- `component_type_counter_def`
- `component_type_measure_point_def`
- `component_type_related_type`
- `component_type_stock_type`
- `component_type_job`

### 4.2 component

Already exists as starter.

Recommended fields:

- id
- component_no
- component_type_id
- name
- status
- maker
- type
- serial_no
- location_id
- department_id
- vendor_id
- current_function_id
- install_date
- installation_id

Derived status rules:

- Installed on a function: `IN_USE`.
- Not installed and available: `AVAILABLE`.
- Transferred out: `TRANSFERRED`.
- Scrapped: `SCRAPPED`.

### 4.3 function

Represents fixed functional positions.

Recommended fields:

- id
- installation_id
- department_id
- function_no
- description
- reference
- parent_function_id
- status
- location_id
- criticality_id
- installed_component_id
- sfi_code
- system
- sub_system
- remarks
- asset_value
- acquisition_date
- currency
- depreciation

Unique:

- `(installation_id, function_no)`

### 4.4 component_function_rotation

Immutable install/remove history.

Recommended fields:

- id
- function_id
- component_id
- installed_at
- installed_by
- removed_at
- removed_by
- install_notes
- remove_notes

### 4.5 component_status_log

Immutable component status history.

Recommended fields:

- id
- component_id
- old_status
- new_status
- reason
- changed_by
- changed_at

### 4.6 counters and measure points

Definition tables:

- `component_type_counter_def`
- `component_counter`
- `function_counter`
- `component_type_measure_point_def`
- `component_measure_point`

Log tables:

- `counter_reading_log`
- `measure_point_reading_log`

Key rule:

- When a component counter reading changes and the component is installed on a function, matching function counters may be incremented by the same delta based on configured dependency/matching rules.

### 4.7 jobs

Use one normalized job table with a target type, or separate type/component/function job tables. Recommended first implementation: one table.

Recommended fields:

- id
- job_no
- target_type: `COMPONENT_TYPE`, `COMPONENT`, `FUNCTION`, `ROUND`, `PROJECT`
- target_id
- description
- status
- frequency
- planning_method: `PERIODIC`, `COUNTER`, `MEASURE_POINT`, `TRIGGER`
- counter_id
- measure_point_id
- due_date
- class_code
- trade
- inherited_from_job_id
- active

Child tables:

- `job_required_part`
- `job_required_discipline`
- `job_dependency`
- `job_description`

### 4.8 work_order

Recommended fields:

- id
- work_order_no
- source_job_id
- source_round_id
- source_project_id
- component_id
- function_id
- title
- description
- status: `REQUESTED`, `PLANNED`, `ISSUED`, `COMPLETED`, `CANCELLED`
- requested_date
- planned_date
- issued_date
- completed_date
- responsible_department_id
- priority

Child tables:

- `work_order_resource`
- `work_order_part`
- `work_order_report`

### 4.9 maintenance_history and maintenance_log

History is the factual completion record. Log is the operational/financial trace.

Recommended fields:

- id
- work_order_id
- job_id
- component_id
- function_id
- completed_at
- completed_by
- report_text
- cost_amount
- budget_code_id

## 5. Stock

### 5.1 stock_type

Already exists as starter.

Recommended fields:

- id
- stock_type_no
- description
- maker
- makers_ref
- preferred_vendor_id
- grade
- unit
- best_price
- status
- installation_id nullable for shared/fleet records

Child tables already started:

- `stock_type_maker`
- `stock_type_vendor`
- `stock_grade`

Additional child tables:

- `stock_alternative_part`
- `stock_replacement_part`

### 5.2 stock_item

Recommended fields:

- id
- stock_item_no
- stock_type_id
- installation_id
- department_id
- location_id
- quantity
- unit
- unit_cost
- currency
- status
- expiry_date
- component_id nullable
- function_id nullable

### 5.3 stock_transaction

Immutable stock movement record.

Recommended fields:

- id
- transaction_no
- stock_item_id
- transaction_type: `IN`, `OUT`, `MOVE_OUT`, `MOVE_IN`, `ADJUSTMENT`, `REVERSAL`
- quantity
- from_location_id
- to_location_id
- reference_type
- reference_id
- transaction_date
- reversed_transaction_id
- created_by
- created_at

### 5.4 stock_wanted

Can be persisted snapshots or generated on demand. Recommended first implementation: persist calculation results.

Recommended fields:

- id
- stock_type_id
- installation_id
- department_id
- current_qty
- reorder_level
- wanted_qty
- for_component_id
- for_function_id
- calculation_run_id

### 5.5 transfer_document

Recommended fields:

- id
- transfer_no
- from_installation_id
- to_installation_id
- from_location_id
- to_location_id
- status: `DRAFT`, `SUBMITTED`, `APPROVED`, `TRANSFERRED`, `RECEIVED`, `CANCELLED`
- submitted_at
- approved_at
- transferred_at
- received_at

Child:

- `transfer_document_line`

## 6. Purchasing

### 6.1 purchase_form

One table for requisitions, queries, and purchase orders.

Recommended fields:

- id
- form_no
- form_type: `REQUISITION`, `QUERY`, `PURCHASE_ORDER`
- status
- origin_type
- origin_id
- vendor_id
- installation_id
- department_id
- delivery_location_id
- currency
- net_amount
- discount_amount
- additional_cost_amount
- tax_amount
- total_amount
- contract_id
- created_at
- approved_at

Child tables:

- `purchase_form_line`
- `purchase_form_additional_cost`
- `purchase_form_delivery_destination`

### 6.2 quotation

Recommended fields:

- id
- quotation_no
- query_form_id
- vendor_id
- status
- currency
- net_amount
- discount_amount
- delivery_days
- notes

Child:

- `quotation_line`
- `quotation_additional_cost`
- `quotation_attachment`

### 6.3 quotation_comparison

Recommended fields:

- id
- query_form_id
- scenario_name
- status: `DRAFT`, `PROPOSED`, `APPROVED`, `SELECTED`
- ranking_method: `BEST_PRICE`, `BEST_DELIVERY`, `MANUAL`
- selected_quotation_id

Child:

- `quotation_comparison_line_decision`

### 6.4 delivery

Recommended fields:

- id
- delivery_no
- purchase_order_id
- status
- delivery_location_id
- intermediate_location_id
- received_at
- received_by

Child:

- `delivery_line`

### 6.5 quality_check and claim

Recommended fields:

- id
- delivery_id
- purchase_form_id
- status
- check_scope: `FORM`, `LINE`
- result: `ACCEPTED`, `REJECTED`, `PARTIAL`
- claim_id

### 6.6 contract

Recommended fields:

- id
- contract_no
- vendor_id
- status
- valid_from
- valid_to
- currency
- discount_type
- approved_at
- issued_at

Child tables:

- `contract_line`
- `contract_delivery_zone`
- `contract_product_group`
- `contract_price_matrix`
- `contract_surcharge`

## 7. Financial

### 7.1 budget

Recommended fields:

- id
- budget_code
- name
- parent_budget_id
- installation_id
- department_id
- fiscal_year
- currency
- limit_amount
- committed_amount
- actual_amount
- warning_threshold
- hard_limit
- status

### 7.2 budget_transaction

Immutable budget impact record.

Recommended fields:

- id
- budget_id
- source_type: `PURCHASE_ORDER`, `STOCK_TRANSACTION`, `MAINTENANCE_LOG`, `VOUCHER`, `CREDIT_NOTE`
- source_id
- transaction_type: `COMMITMENT`, `ACTUAL`, `REVERSAL`, `ADJUSTMENT`
- amount
- currency
- occurred_at

### 7.3 voucher

Recommended fields:

- id
- voucher_no
- voucher_type: `INVOICE`, `CREDIT_NOTE`
- purchase_form_id nullable
- vendor_id
- budget_id
- currency
- net_amount
- discount_amount
- vat_amount
- total_amount
- status
- invoice_date
- approved_at

Child:

- `voucher_line`
- `voucher_additional_cost`

## 8. Workflow And Audit

### 8.1 workflow_notification

Recommended fields:

- id
- module
- business_type
- business_id
- assigned_user_id
- status
- message
- created_at
- acknowledged_at

### 8.2 audit_log

Recommended fields:

- id
- entity_type
- entity_id
- action
- old_value_json
- new_value_json
- reason
- user_id
- occurred_at

## 9. First Migration Target After V1

Recommended next Flyway migration: `V2__component_type_children.sql`.

Tables:

- `component_type_counter_def`
- `component_type_measure_point_def`
- `component_type_related_type`
- `component_type_stock_type`

Then upgrade:

- `ComponentType` aggregate DTO.
- `ComponentTypeService`.
- `ComponentTypeController`.
- Tests for child create/update.

