# Component Types Implementation Slice

This is the first recommended implementation slice for the AMOS rewrite.

Manual references:

- Chapter 2 Maintenance
- Component Types, printed page 26
- Defining a Component Type, printed page 26
- Working with Component Types, printed page 27
- Registering a Component Type as a Component, printed page 30
- Component Counters and Measure Points, printed pages 34-35
- Component Type Jobs, printed page 47
- Stock Types in Component Types, printed page 180

Frontend references:

- `src/windows/registry.js`, key `component-types`
- `src/views/ComponentTypesView.vue`
- `src/services/componentService.js`
- `src/services/collectionService.js`
- `src/mock/index.js`, key `componentTypes`

Backend references:

- `amos-app/src/main/java/com/neusoft/amos/maintenance/ComponentType.java`
- `amos-app/src/main/java/com/neusoft/amos/maintenance/ComponentTypeController.java`
- `amos-app/src/main/java/com/neusoft/amos/maintenance/ComponentTypeRepository.java`
- `amos-app/src/main/resources/db/migration/V1__init.sql`

## 1. Current Behavior To Preserve

The frontend Component Types window currently supports:

- Opening the window with a Filter dialog.
- Basic filters:
  - status
  - maker
  - classCode
- Advanced filters:
  - typeNumber
  - name
- List columns:
  - typeNumber
  - name
  - maker
  - classCode
  - status
  - jobs
- Detail tabs:
  - General
  - Jobs
  - Parts
  - Counters
  - Measure Points
  - Related
  - Components
- Options:
  - Register as Component
  - View Job
  - Add Part
  - Copy
  - Copy List
  - Use Component Types

## 2. Frontend Field Inventory

Main fields:

| Field | Meaning | Backend status |
| --- | --- | --- |
| `id` | frontend record id | existing as numeric ID |
| `typeNumber` | component type number | existing |
| `name` | component type name | existing |
| `maker` | maker code | existing |
| `model` | model | existing |
| `type` | type/category | existing |
| `classCode` | component class | existing |
| `preferredVendor` | preferred vendor display/name | existing as string |
| `parentTypeNumber` | parent component type number | existing as string |
| `description` | description | existing |
| `status` | Active/Obsolete/Blocked | existing |
| `dateCreated` | creation date | existing as string |
| `dateModified` | modification date | existing as string |
| `compTypeModel` | manual screenshot field | missing in backend table |
| `jobs` | count of related jobs | frontend-derived, should be DTO field |

Child fields:

Counters:

| Field | Meaning |
| --- | --- |
| `code` | counter code |
| `description` | counter description |
| `unit` | unit |

Measure point definitions:

| Field | Meaning |
| --- | --- |
| `code` | point code |
| `description` | point description |
| `trend` | Up/Down/Stable |
| `unit` | unit |

Related types:

| Field | Meaning |
| --- | --- |
| `typeNumber` | related component type |

Parts:

| Field | Meaning |
| --- | --- |
| `itemNo` | stock item/type number from frontend mock |
| `name` | part name |
| `makersRef` | maker reference |
| `stockTypeNo` | target stock type when normalized |

## 3. Backend Data Model Changes

Add migration `V2__component_type_children.sql`.

### 3.1 component_type

Add missing column:

```sql
alter table component_type add column if not exists comp_type_model varchar(100);
```

Recommended later cleanup:

- Replace `preferred_vendor varchar` with `preferred_vendor_id`.
- Replace `parent_type_number varchar` with `parent_component_type_id`.
- Replace string dates with timestamps.

Do not do that cleanup in the first slice unless the frontend migration needs it.

### 3.2 component_type_counter_def

```sql
create table if not exists component_type_counter_def (
    id bigserial primary key,
    component_type_id bigint not null references component_type(id),
    code varchar(50) not null,
    description varchar(255),
    unit varchar(20),
    sort_order integer default 0,
    unique (component_type_id, code)
);
```

### 3.3 component_type_measure_point_def

```sql
create table if not exists component_type_measure_point_def (
    id bigserial primary key,
    component_type_id bigint not null references component_type(id),
    code varchar(50) not null,
    description varchar(255),
    trend varchar(20),
    unit varchar(20),
    sort_order integer default 0,
    unique (component_type_id, code)
);
```

### 3.4 component_type_related_type

```sql
create table if not exists component_type_related_type (
    id bigserial primary key,
    component_type_id bigint not null references component_type(id),
    related_component_type_id bigint not null references component_type(id),
    unique (component_type_id, related_component_type_id)
);
```

### 3.5 component_type_stock_type

```sql
create table if not exists component_type_stock_type (
    id bigserial primary key,
    component_type_id bigint not null references component_type(id),
    stock_type_id bigint not null references stock_type(id),
    makers_ref varchar(100),
    quantity double precision default 1,
    remarks varchar(500),
    unique (component_type_id, stock_type_id)
);
```

## 4. Backend Code Tasks

### 4.1 Entities

Add entities:

- `ComponentTypeCounterDef`
- `ComponentTypeMeasurePointDef`
- `ComponentTypeRelatedType`
- `ComponentTypeStockType`

Update `ComponentType`:

- Add `compTypeModel`.
- Add `@OneToMany` child collections for counters, measure point definitions, related types, and stock type links.
- Use cascade and orphan removal for child collections in aggregate updates.

### 4.2 DTOs

Add DTOs instead of exposing child JPA details directly:

- `ComponentTypeDto`
- `ComponentTypeCounterDefDto`
- `ComponentTypeMeasurePointDefDto`
- `ComponentTypeRelatedTypeDto`
- `ComponentTypePartDto`
- `RegisterComponentRequest`

The DTO should keep frontend-compatible field names:

- `typeNumber`
- `classCode`
- `preferredVendor`
- `parentTypeNumber`
- `compTypeModel`
- `measurePointDefs`
- `relatedTypes`

### 4.3 Service

Add `ComponentTypeService`.

Responsibilities:

- List with filters.
- Load one aggregate.
- Create aggregate with children.
- Update aggregate with children.
- Validate unique `typeNumber`.
- Validate child counter/measure point codes unique inside the component type.
- Resolve related component types by `typeNumber`.
- Resolve linked parts by `stockTypeNo`.
- Calculate `jobs` count.
- Register a component from a component type.

### 4.4 Controller

Replace pure `AbstractCrudController` inheritance with explicit endpoints:

```text
GET    /api/maintenance/component-types
POST   /api/maintenance/component-types
GET    /api/maintenance/component-types/{id}
PUT    /api/maintenance/component-types/{id}
DELETE /api/maintenance/component-types/{id}
POST   /api/maintenance/component-types/{id}/register-component
```

List filters:

- `status`
- `maker`
- `classCode`
- `typeNumber`
- `name`

Register command body:

```json
{
  "number": "C-10099",
  "name": "Main Engine Cylinder Liner #9",
  "location": "Engine Room",
  "department": "Engine Room",
  "installation": "Traveller",
  "serialNo": "SN-001"
}
```

Expected behavior:

- Creates a `component` row.
- Copies type number, maker, model/type where applicable.
- Creates component counters from type counter definitions.
- Creates component measure points from type measure point definitions once component measure point table exists. If not implemented yet, document as deferred.
- Initial status should be `Available` unless installed on a function.

## 5. Frontend Tasks

### 5.1 API Client

Add a small API client:

- `src/services/apiClient.js`
- default base URL: `http://localhost:8080/api`
- override with `VITE_AMOS_API_BASE_URL`

### 5.2 Component Type Service

Add or extend service methods:

```js
listComponentTypes(filters)
getComponentType(id)
createComponentType(payload)
updateComponentType(id, payload)
deleteComponentType(id)
registerComponentFromType(id, payload)
```

### 5.3 UI Integration

Keep the Component Types page behavior stable:

- Filter dialog still appears.
- List columns stay the same.
- General tab stays editable.
- Counters, Measure Points, Related and Parts child tabs read/write through the aggregate DTO.
- Register as Component calls the command endpoint.

Do not migrate unrelated pages in this slice.

## 6. Tests

Backend tests:

- List component types.
- Filter by status/maker/class/type number/name.
- Create with counters and measure point definitions.
- Update child collections and ensure removed children are deleted.
- Reject duplicate `typeNumber`.
- Reject duplicate child counter code inside same component type.
- Register as component and verify created component fields.
- Register as component creates counters from definitions.

Frontend checks:

- `npm run build`.
- Component Types window opens.
- Filter returns expected rows.
- Editing a component type persists after refresh.
- Register as Component creates a component visible in Components.

## 7. Acceptance Criteria

The slice is complete when:

- Backend migration runs cleanly on H2.
- Backend app starts.
- Component Types API supports aggregate read/write.
- Frontend Component Types no longer depends on direct mock mutation for its primary data.
- Existing AMOS-style UI interaction is preserved.
- No unrelated modules are changed.
- No batch deletion is performed.

## 8. Prompt To Start This Slice

```text
请执行 Component Types 第一个开发切片。

依据：
- /Users/zhenghai/Code/fde-training-lab/prototypes/amos-server/docs/component-types-slice.md
- AMOS 用户手册 Chapter 2：Component Types，Printed pages 26-30
- 前端 /Users/zhenghai/Code/fde-training-lab/prototypes/amos
- 后端 /Users/zhenghai/Code/fde-training-lab/prototypes/amos-server

要求：
1. 先检查当前前后端相关文件。
2. 新增 Flyway V2 migration。
3. 完成 Component Type 聚合实体、DTO、Service、Controller。
4. 增加必要测试。
5. 将前端 Component Types 服务接入 API，保留最小 fallback。
6. 运行后端测试和前端 build。
7. 不执行任何批量删除操作。
```

