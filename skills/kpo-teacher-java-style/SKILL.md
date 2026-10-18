---
name: kpo-teacher-java-style
description: Write or review Java/Gradle coursework in the style of the kpo-2026 instructor repository, with emphasis on SOLID, code organization, Checkstyle, and meaningful tests. Use when the user asks to match this instructor's style; do not apply to unrelated Java projects.
---

# KPO instructor Java style

Use the instructor's `origin/master` in `kpo-2026` as a reference for new coursework. The observed reference is `https://github.com/vecnick/kpo-2026` at commit `75b37eb`; inspect the current instructor branch when available because conventions may evolve. In the user's checkout, distinguish instructor code from the student's branches and changes before drawing conclusions. Follow the current assignment and explicit user choices when they differ from an older practice.

## Design

- Keep each class responsible for one part of the workflow. In the examples, services coordinate actions, factories construct channel-specific objects, validators check input and state, repositories store data, and observers consume events. Add a class or interface only when it gives a real boundary in the current assignment.
- Depend on narrow contracts, such as `ReportSaver`, `ReportSender`, `RetryPolicy`, and `DeliveryObserver`. Supply collaborators through constructors; use `private final` fields. Use Spring configuration or component annotations when the assignment uses Spring, and Lombok `@RequiredArgsConstructor` where it fits the existing module.
- Prefer immutable domain data (`record`, enums, immutable lists) when state does not need to change. Keep the content of an object separate from workflow state, as with notification content and delivery status.
- Use Builder, Factory, Strategy, Observer, or other patterns to express a concrete requirement. Avoid patterns added only to make the architecture look elaborate.
- Make failure cases explicit with a domain exception and a machine-readable error code when the module already follows that convention. Preserve useful causes and messages.

## Writing and formatting

- Use descriptive English names for packages, types, methods, and variables. Group packages by responsibility within the relevant domain, as in `notification.factory` and `delivery.retry`.
- Keep methods focused and expose dependencies and control flow clearly. Add short Javadoc to public types and operations; document parameters and return values when useful. Match the current module's language and level of documentation rather than copying prose mechanically.
- Follow the module's Checkstyle configuration, based on Sun Code Conventions. Run the existing Gradle checks; do not silently weaken rules to make new code pass. Match the project's Java, Spring, Lombok, JUnit, and Gradle versions instead of copying versions from the reference.

## Tests

- Use JUnit 5 with descriptive Russian `@DisplayName` text and clear English test method names, as in the instructor's recent practices.
- Cover the main behavior, validation, boundaries, and failure paths. For coordinators, verify interactions and that a failed step prevents later actions. Use Mockito where behavior between collaborators matters.
- Keep focused unit tests separate from integration tests that prove the assembled Spring or application flow. Prefer fixed time and data, in-memory collaborators, and no sleeps or external services unless the assignment requires them.
- Extract small test helpers for repeated setup or assertions. Check meaningful behavior and error codes, not just construction or implementation details. Do not invent a required coverage percentage: the reference config generates JaCoCo reports but sets no visible threshold.

## Before delivery

Review the new code against the assignment and nearby instructor examples, then run the relevant module's tests and Checkstyle tasks. Report what passed and any remaining failure. Treat a defect in the instructor's sample as a defect to recognize, not a behavior to imitate.
