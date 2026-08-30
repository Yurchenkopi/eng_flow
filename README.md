# EngFlow

EngFlow — внутреннее веб-приложение для инженерной работы. Первая запускаемая версия позволяет вести организации и проекты установок, выбирать заказчика, просматривать карточки проектов и видеть сводку по статусам.

Согласованные требования находятся в [`docs/requirements.md`](docs/requirements.md), текущая модель предметной области — в [`docs/domain-model.md`](docs/domain-model.md).

## Стек

- Java 21
- Spring Boot 4.1.1
- Spring MVC, Spring Data JPA, Thymeleaf и Bean Validation
- PostgreSQL
- Liquibase с YAML changelog'ами
- Bootstrap 5
- Maven

## Требования для локального запуска

- JDK 21
- Maven 3.6.3 или новее
- PostgreSQL с доступной базой `eng_flow`

## Настройка PostgreSQL

Создайте базу и отдельного локального пользователя любым удобным административным способом. Например, из `psql` под администратором PostgreSQL:

```sql
CREATE USER eng_flow_app WITH PASSWORD 'replace_with_local_password';
CREATE DATABASE eng_flow OWNER eng_flow_app;
```

Пароль из примера замените локальным и не добавляйте его в Git. Таблицы `organizations` и `projects` автоматически создаются Liquibase при первом запуске. Hibernate работает в режиме `validate` и не создает схему самостоятельно.

Корневой changelog: `src/main/resources/db/changelog/db.changelog-master.yaml`.

## Переменные окружения

| Переменная | Значение по умолчанию | Назначение |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/eng_flow` | JDBC URL PostgreSQL |
| `DB_USERNAME` | `postgres` | Пользователь БД |
| `DB_PASSWORD` | пустая строка | Пароль БД |
| `SERVER_PORT` | `8080` | HTTP-порт приложения |

Пример для PowerShell в текущей сессии:

```powershell
$env:DB_USERNAME = 'eng_flow_app'
$env:DB_PASSWORD = 'replace_with_local_password'
mvn spring-boot:run
```

## Команды

Запуск приложения:

```shell
mvn spring-boot:run
```

Тесты:

```shell
mvn test
```

Сборка исполняемого JAR:

```shell
mvn package
```

После запуска приложение доступно по адресу [http://localhost:8080](http://localhost:8080).
