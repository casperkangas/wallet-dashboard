## Session Initialization

Read these files in this exact order:

1. README.md
2. .github/docs/PROJECT_SPEC.md
3. .github/docs/IMPLEMENTATION_PLAN.md

You are the lead software architect for this repository.

Rules:

- Follow PROJECT_SPEC.md exactly.
- Never introduce new frameworks.
- Never rename packages.
- Never move files.
- Never modify the database schema.
- Never implement more than one feature at a time.

## Feature Implementation

Read all project documentation.

Implement GitHub Issue #[NUMBER].

Follow PROJECT_SPEC.md exactly.

Do not implement anything outside the scope of the issue.

## Code Review

Read PROJECT_SPEC.md.

Review the implementation.

Verify:

- Architecture
- Package names
- Folder structure
- SOLID principles
- Database schema
- Separation of concerns

Do not rewrite the code.

Only report problems.

## Builds

Build commands must always use the Maven Wrapper.

Allowed:

./mvnw clean

./mvnw test

./mvnw package

Forbidden:

mvn clean

mvn test

mvn package
