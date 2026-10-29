# CP4 — Security gates and negative authorization test

Proposal for the pipeline of `opti-customers-api` (`ms-pacientes`). Nothing here changes the
team's code repo today; it lives in my fork as the workshop asks.

| File | Goes to (in `opti-customers-api`) |
|---|---|
| `security.yml` | `.github/workflows/security.yml` |
| `PatientFormulasAuthorizationTest.java` | `customers-app/src/test/java/co/edu/corhuila/opti/customers/app/` |

## Story and criterion covered

- Story: `[security] Patients can only read their own record and formulas` — https://github.com/code-corhuila/opti-docs/issues/33
- STRIDE row E, risk 9 (P0), PR `Threat model: api-gateway → ms-pacientes` in opti-docs
- Criterion: "a `PACIENTE` token reading `GET /api/v1/patients/{id}/formulas` of another
  patient returns `403 FORBIDDEN` and no formula data"

## Run it locally

Requirements: JDK 21 and Maven, same as `mvn -B verify` in the repo README.

```bash
cd opti-customers-api
cp <fork>/13-week/workshop/04-security-ci/PatientFormulasAuthorizationTest.java \
   customers-app/src/test/java/co/edu/corhuila/opti/customers/app/
mvn -B -pl customers-app -am test -Dtest=PatientFormulasAuthorizationTest \
    -Dsurefire.failIfNoSpecifiedTests=false
```

## Expected result today

The test is **red** on purpose. `PatientController` has no role or ownership check on
`GET /{id}`, `GET /{id}/formulas` and `GET /{id}/formulas/current`, so any valid token gets
`200`. The gate blocks the merge until the story is done; it turns green when the use case
compares the `patientId` claim with `{id}` and answers `403`.

Real console output: pending (CP8).
