# AGENTS.md

## Project status

This repository is intended for a Java project. No application framework, package manager, build system, or test framework has been selected yet, and application source code has not been created.

## Project identity

- Project name: `EngFlow`.
- GitHub repository name: `eng_flow`.
- Maven coordinates: `ru.yurch:eng_flow`.
- Root Java package: `ru.yurch.engflow`.
- All Java classes must be inside the `ru.yurch.engflow` package namespace.

Typical package names include `ru.yurch.engflow.controller`, `ru.yurch.engflow.service`, `ru.yurch.engflow.repository`, `ru.yurch.engflow.model`, `ru.yurch.engflow.dto`, and `ru.yurch.engflow.config`.

## Naming conventions

### Java

- Classes and enums use `PascalCase`, for example `Project`, `ProjectItem`, and `PurchaseOrderLine`.
- Fields and methods use `camelCase`, for example `modificationName`, `completionDate`, and `requiredQuantity`.
- Constants and enum values use `UPPER_SNAKE_CASE`, for example `DESIGN`, `PRODUCTION`, and `COMPLETED`.

### PostgreSQL

- Database name: `eng_flow`.
- Table and column names use lowercase `snake_case`; never use camelCase.
- Table names use plural form.
- Examples of table names: `organizations`, `contacts`, `projects`, `project_images`, `catalog_items`, `item_suppliers`, `project_assemblies`, `project_items`, `purchase_orders`, `purchase_order_lines`, `receipts`, `transfer_acts`, `transfer_act_items`, `official_letters`, and `work_logs`.
- Examples of column names: `modification_name`, `completion_date`, `based_on_project_id`, `required_quantity`, and `purchase_order_id`.

### HTTP URLs

- Use lowercase kebab-case and plural form where natural, for example `/projects`, `/organizations`, `/purchase-orders`, `/transfer-acts`, and `/official-letters`.
- Java naming is independent from URL naming.

### Flyway

- Future migration files use standard Flyway naming, for example `V1__create_organizations.sql` and `V2__create_projects.sql`.
- Do not create migrations until the implementation stage explicitly requires them.

## Primary project documents

- `docs/requirements.md` contains the agreed requirements, scope boundaries, future scope, and open questions.
- `docs/domain-model.md` contains the current draft domain model, relationships, cardinalities, and business rules.
- Treat items marked `TODO / Open Question` as undecided; do not resolve them without explicit user direction.

## Working guidelines

- Inspect the repository before making changes; its structure may evolve after this file is created.
- Do not assume a language, framework, package manager, or architecture without evidence in the repository or explicit user direction.
- Keep changes focused on the requested task and preserve unrelated user work.
- Prefer small, reviewable edits and follow conventions established by existing files.
- Never add dependencies, generated artifacts, or configuration files unless they are required by the task.

## Verification

- Use the project's own documented build, lint, and test commands once they exist.
- Until tooling is added, verify changes with the narrowest relevant checks available and report any verification that could not be performed.

## Documentation maintenance

- Update this file when the project gains stable conventions, architecture, or standard development commands.
