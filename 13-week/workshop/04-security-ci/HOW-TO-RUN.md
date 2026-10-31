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

## CP8 — Real run (2026-10-30, my machine)

Run from IntelliJ IDEA 2026.2.1 on Windows (JDK 26.0.2, Spring Boot 3.5.6), class
`PatientFormulasAuthorizationTest` copied into `opti-customers-api/customers-app/src/test/...`
(not committed to the team repo). First run failed in the setup with
`formulaDate must not be in the future` because the tests use the fixed `TestClock`
(2026-09-29); the formula date was changed to `2026-09-20`, the same one `CustomersHttpTest` uses.

Console output (trimmed, tokens removed):

```text
POST /api/v1/patients -> 201 (152 ms)
POST /api/v1/patients/00000000-0000-0000-0000-000000000001/formulas -> 201 (19 ms)
GET /api/v1/patients/00000000-0000-0000-0000-000000000001/formulas/current -> 200 (14 ms)
  Handler = PatientController#currentFormula(String)
  Status  = 200
  Body    = {"id":"...0002","patientId":"...0001","od":{"sphere":-2.0,"cylinder":-0.5,"axis":180,...},
             "pupillaryDistance":63.0,"lensType":"MONOFOCAL","optometristName":"Ana Torres",...}
java.lang.AssertionError: Status expected:<403> but was:<200>
  at ...PatientFormulasAuthorizationTest.patientTokenCannotReadCurrentFormulaOfAnotherPatient(PatientFormulasAuthorizationTest.java:53)

POST /api/v1/patients -> 201 (2 ms)
POST /api/v1/patients/00000000-0000-0000-0000-000000000003/formulas -> 201 (1 ms)
GET /api/v1/patients/00000000-0000-0000-0000-000000000003/formulas -> 200 (14 ms)
  Handler = PatientController#formulas(HttpServletRequest, String, String, String)
  Status  = 200
  Body    = {"data":[{"id":"...0004","patientId":"...0003",...,"pupillaryDistance":63.0,...}],"meta":{"total":1}}
java.lang.AssertionError: Status expected:<403> but was:<200>
  at ...PatientFormulasAuthorizationTest.patientTokenCannotListFormulasOfAnotherPatient(PatientFormulasAuthorizationTest.java:44)

Process finished with exit code -1
```

Reading: both tests are red. A token with role `PACIENTE` (not in `Roles`, no `patientId`)
gets **200 and the full optical formula** of another patient, because `currentFormula` and
`formulas` have no `requireRole` and no ownership check. This confirms STRIDE row E (risk 9)
and keeps story #33 open; the test turns green when the fix answers 403.

## CP8 — Workflow validation

`actionlint 1.7.12` on `security.yml`: no errors (exit code 0).

