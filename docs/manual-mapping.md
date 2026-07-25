# AMOS User Guide Mapping

This document maps the AMOS M&P Vrs.10.0.30 User Guide to rewrite modules.

The page numbers below are the manual's printed topic page numbers from the table of contents, not necessarily PDF viewer page indexes.

## Chapter 1 - Getting Started With AMOS

| Manual topic | Printed page | Target module | Frontend status | Backend status |
| --- | ---: | --- | --- | --- |
| How to Start AMOS Business Suite | 2 | auth/system | partial | login scaffold |
| Dashboard | 2 | dashboard | implemented with mock data | planned |
| Alerts Overview | 3 | dashboard/alerts | implemented with mock data | planned |
| Notifications | 4 | workflow | entry implemented | planned |
| Task, List and Icon Bar | 5 | shell/options | implemented | planned user options |
| Webpages and Images on the Dashboard | 7 | dashboard/options | implemented | planned user options |
| The Windows in AMOS | 8 | shell/windowing | implemented | frontend-only |
| Setting Default Window Opening Mode | 8 | options | implemented | planned |
| Setting Tab Behaviour | 8 | options | partial | planned |
| Splitting Scrollbars | 9 | shell/windowing | deferred | deferred |
| Toolbars | 10 | shell/actions | implemented | frontend-owned |
| Active Window Refresh | 11 | shell/actions | implemented | API refresh per module |
| Searching in a Window | 11 | filters/search | implemented locally | planned server search |
| Multi-Selecting | 15 | shell/list | partial | frontend-owned |
| Setting Dates with the Calendar | 16 | shell/forms | partial | frontend-owned |
| Audit Trails and Change Reasons | 16 | audit | not implemented | planned |
| Selecting or Changing the View | 18 | user views | implemented as mock option | planned |
| Printing from AMOS | 19 | print/export | partial | planned |
| Locking the Application | 19 | auth/session | implemented visual only | planned |
| Saving, Closing and Exiting | 19 | shell/actions | implemented | API save per module |
| Installations and Departments | 20 | system/scope | implemented locally | tenant/scope scaffold |
| Switching Departments | 20 | system/scope | implemented locally | planned |
| Global Search | 21 | search | implemented locally | planned |
| Updating System License | 24 | license | dialog/placeholder | deferred |

## Chapter 2 - Maintenance

| Manual topic | Printed page | Target module | Frontend page key | Priority |
| --- | ---: | --- | --- | --- |
| Component Types | 26 | maintenance/component-types | `component-types` | P0 |
| Defining a Component Type | 26 | maintenance/component-types | `component-types` | P0 |
| Working with Component Types | 27 | maintenance/component-types | `component-types` | P0 |
| Registering a Component Type as a Component | 30 | maintenance/components | `component-types`, `components` | P0 |
| Components | 32 | maintenance/components | `components` | P0 |
| Creating Components | 32 | maintenance/components | `components` | P0 |
| Component Counters | 34 | maintenance/counters | `components`, `update-counters` | P1 |
| Component Measure Points | 35 | maintenance/measure-points | `components`, `update-measure-points` | P1 |
| Functions | 36 | maintenance/functions | `functions` | P0 |
| Working with Functions | 36 | maintenance/functions | `functions`, `functions-hierarchy` | P0 |
| Installing a Component on a Function | 40 | maintenance/component-installation | `functions`, `components` | P0 |
| Removing a Component from a Function | 42 | maintenance/component-installation | `functions`, `components` | P0 |
| Function Criticality | 44 | register/function-criticality | `function-criticalities` | P1 |
| Function Counters | 46 | maintenance/counters | `functions`, `update-counters` | P1 |
| Maintenance Jobs | 47 | maintenance/jobs | `component-type-jobs`, `component-jobs` | P0 |
| Component Type Jobs | 47 | maintenance/jobs | `component-type-jobs` | P0 |
| Component Jobs | 50 | maintenance/jobs | `component-jobs` | P0 |
| Mandatory History and Reporting Options | 61 | maintenance/reporting | `report-work` | P1 |
| Risk Management on Jobs | 62 | maintenance/jobs/risk | `component-jobs` | P2 |
| Spare Booking | 63 | maintenance/jobs/parts | `component-jobs`, `stock-items` | P1 |
| Required Disciplines | 65 | maintenance/jobs/disciplines | `component-jobs` | P1 |
| Updating Parts and Disciplines | 67 | maintenance/jobs | `component-jobs` | P1 |
| Scheduling Jobs | 69 | maintenance/scheduling | `job-planning` | P0 |
| Periodic Frequencies and Planning Methods | 69 | maintenance/scheduling | `job-planning` | P0 |
| Scheduling by Counters | 71 | maintenance/scheduling | `job-planning`, `update-counters` | P1 |
| Scheduling by Measure Points | 74 | maintenance/scheduling | `job-planning`, `update-measure-points` | P1 |
| Scheduling by Triggers | 76 | maintenance/scheduling | `job-planning` | P1 |
| Job Planning Window | 77 | maintenance/job-planning | `job-planning` | P0 |
| Initial Work Orders | 78 | maintenance/work-orders | `work-orders` | P0 |
| Maintenance Rounds | 80 | maintenance/rounds | `rounds` | P1 |
| Defining/Scheduling Rounds | 81 | maintenance/rounds | `rounds` | P1 |
| Allocating Jobs to a Round | 82 | maintenance/rounds | `rounds` | P1 |
| Deactivating and Reactivating Jobs | 87 | maintenance/jobs | `component-jobs` | P1 |
| Function Driven Jobs | 89 | maintenance/jobs | `component-jobs`, `functions` | P1 |
| Round Work Orders | 92 | maintenance/work-orders | `work-orders`, `rounds` | P1 |
| Handling Maintenance | 92 | maintenance/work-orders | `work-orders` | P0 |
| Maintenance Work Orders | 93 | maintenance/work-orders | `work-orders` | P0 |
| One-Off Work Orders | 94 | maintenance/work-orders | `work-orders` | P1 |
| Requisition Work | 95 | maintenance/work-orders | `work-orders`, `forms` | P1 |
| Planning Work Orders | 101 | maintenance/work-planning | `work-planning` | P0 |
| Work Orders Window | 101 | maintenance/work-orders | `work-orders` | P0 |
| Work Planning Window | 112 | maintenance/work-planning | `work-planning` | P1 |
| Maintenance Tasks Window | 116 | maintenance/tasks | `work-orders` | P2 |
| Maintenance Projects | 120 | maintenance/projects | `projects` | P1 |
| Project Process / Setup / Sections | 120 | maintenance/projects | `projects` | P1 |
| Project Jobs / Work Orders | 123 | maintenance/projects | `projects`, `work-orders` | P1 |
| Sub-Contracting Project Jobs | 136 | maintenance/projects/purchase | `projects`, `forms` | P2 |
| Project Costs | 146 | maintenance/projects/financial | `projects`, `budgets` | P2 |
| Updating Counters and Measure Points | 150 | maintenance/counters | `update-counters`, `update-measure-points` | P1 |
| Counter/Measure Overview, Logs | 154 | maintenance/counters | `counters-overview` | P1 |
| Reporting Work | 155 | maintenance/report-work | `report-work` | P0 |
| Reporting Resources Used | 157 | maintenance/report-work | `report-work` | P1 |
| Reporting Stock Used | 160 | maintenance/report-work/stock | `report-work`, `stock-items` | P1 |
| Reporting History | 161 | maintenance/history | `maintenance-history` | P1 |
| Reporting Measure Points | 162 | maintenance/report-work | `report-work` | P1 |
| Reporting Work with Permit | 162 | maintenance/work-permit | `report-work` | P2 |
| Reporting Overdue/Unplanned/Related Jobs | 163 | maintenance/report-work | `report-work` | P1 |
| Maintenance History | 170 | maintenance/history | `maintenance-history` | P1 |
| Maintenance Log | 170 | maintenance/log | `maintenance-log` | P1 |

## Chapter 3 - Stock Management

| Manual topic | Printed page | Target module | Frontend page key | Priority |
| --- | ---: | --- | --- | --- |
| Stock Types | 172 | stock/stock-types | `stock-types` | P0 |
| Defining a Stock Type | 172 | stock/stock-types | `stock-types` | P0 |
| Stock Type Makers | 173 | stock/stock-types | `stock-types` | P1 |
| Stock Type Vendors | 174 | stock/stock-types | `stock-types` | P1 |
| Stock Grades | 176 | stock/stock-types | `stock-types` | P1 |
| Stock Types in Component Types | 180 | maintenance/parts | `component-types`, `stock-types` | P1 |
| Registering Stock Type as Stock Item | 182 | stock/stock-items | `stock-items` | P0 |
| Stock Items | 183 | stock/stock-items | `stock-items` | P0 |
| Stock Item Makers and Vendors | 184 | stock/stock-items | `stock-items` | P1 |
| Stock Items in Components/Types | 184 | maintenance/parts | `components`, `component-types` | P1 |
| Stock Depreciation | 184 | stock/financial | `stock-items`, `budgets` | P2 |
| Stock Item Locations | 185 | stock/locations | `stock-items` | P0 |
| Moving Stock Items | 186 | stock/transactions | `stock-items`, `transactions` | P0 |
| Perishable Stock | 187 | stock/perishable | `stock-items` | P2 |
| Alternative/Replacement/Obsolete Parts | 188 | stock/parts | `stock-types` | P2 |
| Setting Status on Stock | 191 | stock/status | `stock-types`, `stock-items` | P1 |
| Stock Purchase History | 197 | stock/purchase-history | `stock-items`, `purchaseForms` | P1 |
| Stock Wanted | 201 | stock/wanted | `wanted` | P0 |
| Calculate Wanted Quantities | 202 | stock/wanted | `wanted` | P0 |
| Purchasing from Stock Wanted | 203 | stock/wanted/purchase | `wanted`, `forms` | P0 |
| In/Out of Stock | 204 | stock/transactions | `transactions` | P0 |
| Stock Control | 205 | stock-control | `stock-control` | P1 |
| Location Inventory | 206 | stock-control | `stock-control` | P1 |
| Stock Transactions | 207 | stock/transactions | `transactions` | P0 |
| Altering/Reversing Stock Transactions | 207 | stock/transactions | `transactions` | P1 |
| Transfer Documents | 208 | stock/transfers | `transfer-documents` | P1 |
| Transfer Submit/Approve/Transfer/Receive | 211 | stock/transfers | `transfer-documents` | P1 |
| Forecasting Stock Requirements | 218 | stock/forecast | deferred | P2 |

## Chapter 4 - Purchasing

| Manual topic | Printed page | Target module | Frontend page key | Priority |
| --- | ---: | --- | --- | --- |
| Purchasing with AMOS | 220 | purchase/core | `forms` | P0 |
| Forms Headers and Lines | 220 | purchase/forms | `forms` | P0 |
| Form Origin Identification | 223 | purchase/forms | `forms` | P1 |
| Order Form Contents | 224 | purchase/forms | `forms` | P1 |
| Delivery Destinations | 226 | purchase/forms | `forms` | P1 |
| Additional Costs | 227 | purchase/forms | `forms` | P1 |
| Line Items | 228 | purchase/form-lines | `forms` | P0 |
| Line Item Purchase History | 231 | purchase/history | `forms`, `stock-items` | P1 |
| Requisition Forms | 233 | purchase/requisitions | `forms` | P0 |
| Query Forms | 235 | purchase/queries | `forms` | P0 |
| Quotations | 236 | purchase/quotations | `quotations` | P0 |
| Printing and Sending Quotation | 240 | print/export | `quotations` | P2 |
| Quotation Notes and Attachments | 242 | purchase/quotations | `quotations` | P1 |
| Updating Quotations | 243 | purchase/quotations | `quotations` | P0 |
| Updating Prices and Discounts | 244 | purchase/quotations | `quotations` | P0 |
| Quotation Additional Costs | 248 | purchase/quotations | `quotations` | P1 |
| Alternative/Additional/Related Quotation Lines | 248 | purchase/quotations | `quotations` | P1 |
| Comparing Quotations | 251 | purchase/comparison | `quotation-comparison` | P0 |
| Configuring Matrix | 252 | purchase/comparison | `quotation-comparison` | P1 |
| Best Price or Delivery Time | 266 | purchase/comparison | `quotation-comparison` | P0 |
| Propose/Approve/Select Scenarios | 268 | purchase/comparison | `quotation-comparison` | P0 |
| Splitting Line Items | 269 | purchase/forms | `forms` | P1 |
| Selecting a Quotation | 272 | purchase/comparison | `forms`, `quotations` | P0 |
| Purchase Orders | 274 | purchase/orders | `forms` | P0 |
| Approving an Order | 275 | purchase/orders | `forms` | P0 |
| Printing and Sending Purchase Orders | 277 | print/export | `forms` | P2 |
| Deliveries | 278 | purchase/deliveries | `deliveries` | P1 |
| Delivery Line Items | 279 | purchase/deliveries | `deliveries` | P1 |
| Intermediate Locations | 284 | purchase/deliveries | `deliveries`, `transport-documents` | P1 |
| Transport Documents | 287 | purchase/transport | `transport-documents` | P2 |
| Receiving a Delivery | 294 | purchase/deliveries/stock | `deliveries`, `stock-items` | P1 |
| Quality Checks and Claims | 296 | purchase/quality | `quality-checks` | P1 |
| Escalating Check to Claim | 300 | purchase/claims | `quality-checks` | P1 |
| Returning Rejected Items | 302 | purchase/returns | `quality-checks`, `stock-items` | P1 |
| Purchase Contracts | 305 | purchase/contracts | `contracts` | P1 |
| Discounts and Contract Pricing | 305 | purchase/contracts | `contracts` | P1 |
| Creating Purchase Contract | 311 | purchase/contracts | `contracts` | P1 |
| Delivery Zones | 315 | purchase/contracts | `contracts` | P2 |
| Product Groups | 317 | purchase/contracts | `contracts` | P2 |
| Price Matrix | 320 | purchase/contracts | `contracts` | P1 |
| Approving and Issuing Contract | 330 | purchase/contracts | `contracts` | P1 |
| Applying Contract to Purchase Form | 331 | purchase/contracts/forms | `forms`, `contracts` | P0 |
| Custom Clearance | 341 | purchase/custom-clearance | `custom-clearance-forms` | P2 |
| Typical Purchasing Questions | 344 | help/reference | deferred | P3 |

## Chapter 5 - Financials

| Manual topic | Printed page | Target module | Frontend page key | Priority |
| --- | ---: | --- | --- | --- |
| Creating a Budget | 348 | financial/budgets | `budgets` | P0 |
| Defining a Budget Code | 348 | financial/budgets | `budgets` | P0 |
| Adding Budget Details | 348 | financial/budgets | `budgets` | P0 |
| Copying a Budget | 351 | financial/budgets | `budgets` | P1 |
| Budget Specifications | 351 | financial/budgets | `budgets` | P1 |
| Vouchers | 352 | financial/vouchers | `vouchers` | P0 |
| Invoice for Purchase Form | 352 | financial/vouchers | `vouchers`, `forms` | P0 |
| Invoice Without Form Reference | 353 | financial/vouchers | `vouchers` | P1 |
| Voucher Amounts, Discounts, Budget Codes | 354 | financial/vouchers | `vouchers`, `budgets` | P0 |
| Voucher Additional Costs | 354 | financial/vouchers | `vouchers` | P1 |
| Voucher Line Items | 354 | financial/vouchers | `vouchers` | P1 |
| Receivable Transactions | 354 | financial/receivables | deferred | P2 |
| Credit Notes | 355 | financial/vouchers | `vouchers` | P1 |
| Budget Breakdowns and Progress | 355 | financial/budgets | `budgets` | P0 |
| Budget Prognosis | 356 | financial/budgets | `budgets` | P1 |
| Budget Commitment Control | 356 | financial/budgets | `budgets` | P0 |
| Budget Warnings and Limits | 357 | financial/budgets | `budgets` | P1 |
| Budget Commitment Formula | 358 | financial/budgets | `budgets` | P1 |
| Budget Hierarchies | 359 | financial/budgets | `budgets` | P1 |
| Purchase Order Budget Impact | 360 | financial/budgets/purchase | `budgets`, `forms` | P0 |
| Stock Transaction Budget Impact | 360 | financial/budgets/stock | `budgets`, `transactions` | P1 |
| Maintenance Log Budget Impact | 360 | financial/budgets/maintenance | `budgets`, `maintenance-log` | P1 |
| Voucher Budget Impact | 361 | financial/budgets/vouchers | `budgets`, `vouchers` | P0 |

## Appendix A - Contracts XML File Formats

| Manual topic | Printed page | Target module | Priority |
| --- | ---: | --- | --- |
| Contract XML Introduction | 364 | purchase/contracts/import-export | P3 |
| Header | 367 | purchase/contracts/import-export | P3 |
| Contract | 367 | purchase/contracts/import-export | P3 |
| AMOS | 367 | purchase/contracts/import-export | P3 |
| Contract Details | 367 | purchase/contracts/import-export | P3 |
| Contract Item Details | 368 | purchase/contracts/import-export | P3 |
| Spare Type Details | 368 | purchase/contracts/import-export | P3 |
| Spare Type Delivery Place Details | 369 | purchase/contracts/import-export | P3 |
| Discount Details | 370 | purchase/contracts/import-export | P3 |

