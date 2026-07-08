# Module 12 Completion Workflow

## Purpose

Finish the remaining submission blockers after the main REST API vertical slices are implemented.
This workflow focuses on making the automated test suite green and ensuring the required project
deliverables are present.

## Scope

- Replace the unfinished bad-credentials authentication test with a real MockMvc assertion.
- Align `GET /api/orders` with the API success response convention used by the other order
  endpoints: `{ "message": "Success", "data": [...] }`.
- Provide a project README with setup, run, test, and API usage notes.
- Complete `CONCEPTS.md` with three project-specific learning concepts.
- Add `PostmanCollection.json` at the repo root for manual API verification.

## Acceptance Criteria

- `./mvnw test` completes with all tests passing in a seeded local MySQL environment.
- No `fail("todo: Implement test")` remains in the active test suite.
- Required submission files exist at the repo root: `README.md`, `CONCEPTS.md`, and
  `PostmanCollection.json`.
- The main tracked `application.properties` can still be used for local testing, but credentials
  should be reviewed before commit or push.
