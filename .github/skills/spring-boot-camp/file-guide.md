# PRSDB file types

Use this for the **"What it is"** part of each tour stop: open with the PRSDB name in bold, then the standard industry
name so the learner can search for it outside this codebase. The standard name follows the [Language](SKILL.md#language)
rules like any other term: at L0 give it as "search for: …" rather than explaining every word in it. Paths are relative to `src/main/kotlin/uk/gov/communities/prsdb/webapp/`
unless they start with `src/` or `resources/` (`src/main/resources/`). Always confirm against the real file. If a file
doesn't fit a row, describe it from its annotations and package.

| Where | PRSDB name | Standard name (what to search for) | Usually connects to |
|-------|------------|------------------------------------|---------------------|
| `controllers/*Controller.kt` | Controller | Spring MVC controller (`@Controller` bean, request handler) | services, templates, URL routes |
| `controllers/controllerAdvice/` | Controller advice | `@ControllerAdvice`: global exception or model handler | every controller |
| `services/*Service.kt` | Service | Service layer: business logic in a Spring bean | repositories, clients, other services |
| `database/repository/*Repository.kt` | Repository | Spring Data JPA repository (an interface, implemented by Spring at runtime) | entities |
| `database/entity/*.kt` | Entity | JPA entity: a class mapped to a database table by the ORM (Hibernate) | repositories, migrations |
| `resources/db/migrations/V*.sql` | Migration | Flyway database migration (versioned schema change) | entities |
| `journeys/<journey>/*JourneyFactory.kt` | Journey factory | Factory bean that builds the journey with a builder DSL | steps, states |
| `journeys/<journey>/steps/*StepConfig.kt` + `*Step` | Step config + step | PRSDB journey framework. A Spring bean per form page (behaviour) and its route wrapper | form model, template, state |
| `journeys/**/states/*State.kt` | Journey state | Session-backed state object (answers saved between pages) | steps, services |
| `models/requestModels/formModels/*FormModel.kt` | Form model | Form-backing object or DTO that Spring fills from the POST body, with validation | validators, step config, template field names |
| `models/viewModels/**` | View model | Presentation model or DTO shaped for a template | templates |
| `models/dataModels/**` | Data model | DTO moving data between layers | services, view models |
| `validation/**` | Validator | Custom constraint validator (PRSDB's prioritised validation) | form models, messages |
| `config/**`, `config/security/**` | Config | `@Configuration` class: bean definitions, security filter chain, interceptors | the whole app |
| `constants/*.kt`, `constants/enums/` | Constants / enums | Kotlin top-level `const val`s and enum classes | everywhere |
| `clients/*Client.kt` | Client | HTTP client for an external API | services |
| `application/**` | Task runner | `ApplicationRunner` for a scheduled ECS task | services |
| `local/api/**` | Local stub | `@Profile("local")` fake of a third-party API | clients |
| `resources/templates/**/*.html` | Template / page | Thymeleaf template (the "view" in MVC) | step config or controller (model values), fragments, messages |
| `resources/templates/fragments/**` | Fragment | Thymeleaf fragment: reusable partial, like a component with parameters | templates that `th:replace` it |
| `resources/messages/*.yml` | Messages file | i18n message bundle (resource bundle) of text keyed for `#{...}` | templates, validation |
| `resources/application*.yml` | App config | Spring Boot externalised configuration (per profile) | `@Value` and `@ConfigurationProperties` beans |
| `src/test/**/*ControllerTests.kt` | Controller test | `@WebMvcTest` slice test with MockMvc | the controller |
| `src/test/**/*ServiceTests.kt` etc. | Unit test | JUnit 5 + Mockito unit test | the class under test |
| `src/test/**/integration/*Tests.kt` | Integration test | End-to-end browser test (Playwright + Testcontainers) | page objects |
| `src/test/**/integration/pageObjects/**` | Page object | Page Object pattern: test helper wrapping one page | integration tests, templates |
