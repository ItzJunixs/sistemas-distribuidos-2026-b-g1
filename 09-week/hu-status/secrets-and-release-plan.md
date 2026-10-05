# Secrets Plan, Feature-Flag Policy, and Canary/Rollback Plan

> Week 9 activity deliverable (configuration, secrets, feature flags, progressive delivery),
> applied to OptiView's actual Corte 1 monolith (`optiview-platform`) ahead of Corte 2's
> MVP 2 work. Kept here as personal weekly evidence, not in the team's shared docs repo.

---

## 1. Why this exists

Reviewing `optiview-platform`'s actual config while doing this week's activity turned up
exactly the mistake the material warns about: `backend/src/main/resources/application.yml`
and `docker-compose.yml` both hard-code a fallback secret for `app.jwt.secret` and the
database password (`clave-de-desarrollo-optiview-cambiar-min-32-bytes`, `optiview`). If the
real environment variable is ever missing, the service doesn't fail — it silently starts
with a known, committed secret. That's "silent startup with missing config" and "secrets in
git" at the same time, both from Session 1's "common mistakes" list.

Actually changing that code is out of scope for this activity (a real behavior change to a
repo the whole team runs locally should go through its own review, not land as a side effect
of a weekly exercise) — so this plan documents the target state and lists it as hardening
story #1 for whoever picks it up.

---

## 2. Secrets plan

| Secret | Owner | Where it lives | How it's injected | Who can read it | Rotation |
|--------|-------|----------------|--------------------|-----------------|---------|
| `SPRING_DATASOURCE_PASSWORD` | ⚠️ Pendiente de definición por el equipo | Local: `.env` (gitignored in `optiview-platform`). Target: a secrets manager once a shared environment exists | Environment variable, read by `application.yml`'s `${SPRING_DATASOURCE_PASSWORD}` | Whoever has shell access to the container / `.env` file | Every 6 months, or immediately on suspected compromise |
| `APP_JWT_SECRET` | ⚠️ Pendiente de definición por el equipo | Same as above | Environment variable, read by `application.yml`'s `${APP_JWT_SECRET}` | Same as above | Every 90 days once a shared environment exists; immediately if ever committed or logged |
| `POSTGRES_PASSWORD` (docker-compose's own DB bootstrap) | ⚠️ Pendiente de definición por el equipo | `.env`, consumed by `docker-compose.yml` | Environment variable | Same as above | Same as `SPRING_DATASOURCE_PASSWORD` — they must always match |

**What's already right:** `.env` is already in `optiview-platform/.gitignore`, so a real
`.env` was never one accidental `git add .` away from being committed.

**What's still open:** nobody owns rotation yet — assigning that is a team decision, not
something to settle unilaterally in a weekly exercise.

---

## 3. Feature-flag policy

OptiView's Corte 1 monolith has no feature flags today — every HU ships directly. Before
Corte 2 adds flags (starting with the HU-08 work below), a policy to agree on as a team:

| Rule | Value |
|------|-------|
| Naming | `ff_<area>_<capability>`, e.g. `ff_orders_lens_treatments` |
| Default | Always **off** for a new flag — nobody opts in by accident |
| Owner | Whoever opens the PR that introduces the flag; recorded in the flag's own comment in code |
| Removal | A flag is deleted in the same Sprint it reaches 100% rollout with no incidents — it does not become permanent configuration (Week 9 Session 2's "flag debt" warning) |
| Storage (Corte 1 scale) | An application property (`app.features.<name>: false`), overridable by environment variable — no flag-management service is justified yet at single-store scale |

---

## 4. Canary + rollback plan — HU-08 completion (lenses, treatments, formula validation)

Per `opti-docs/05-architecture/overview.md` §9 ("Planned evolution"), Corte 2's MVP 2 work is
**completing HU-08** on the same monolith: adding lens selection, treatments, and
prescription-validation to work-order creation (today's Corte 1 walking skeleton only covers
the frame + happy path).

| Stage | What happens | Exit criteria to move to the next stage |
|-------|--------------|------------------------------------------|
| Deploy (flag `ff_orders_lens_treatments` off) | Code ships to `qa`/`staging` dark — work orders behave exactly as in Corte 1 | Smoke tests pass with the flag off; no behavior change observed |
| Canary (flag on for the `ADMIN_OPTICA` role only — the smallest real user group) | A single trusted role can create work orders with lenses/treatments in staging | No validation errors on well-formed requests; invariants (e.g. lens type enum) hold for a full day of use |
| Ramp (flag on for `VENDEDOR` too, still staging) | The role that will actually use this in production gets it next | No regression in the existing frame-only flow for either role |
| 100% (flag on for everyone) | Full rollout | Monitored per the team's deploy checklist |
| Rollback | Flip `ff_orders_lens_treatments` back to `false` via environment variable — no redeploy | Used if lens/treatment validation produces an invalid work order or breaks the existing frame-only path |

This follows Week 9 Session 2's own example story shape ("Feature X ships behind a flag
defaulted off, with a canary plan") applied to a real, already-planned OptiView feature
instead of a hypothetical one.

---

## 5. Hardening stories (sliced, with acceptance criteria)

Following Session 2's "Slice the hardening stories" guidance:

1. **"All required config fails fast on startup."** Given `APP_JWT_SECRET` or
   `SPRING_DATASOURCE_PASSWORD` is unset, when the backend starts, then it refuses to start
   and logs which variable is missing — not a silent fallback to the current hard-coded
   default.
2. **"`.env.example` stays the single source of truth for required variables."** Given a new
   required variable is added to `application.yml`, when a PR introduces it, then
   `.env.example` is updated in the same PR.
3. **"HU-08's lens/treatment capability ships behind `ff_orders_lens_treatments`."** Given the
   flag is off, when a work order is created, then behavior is identical to today's Corte 1
   flow — see the canary plan in §4 for the rollout itself.
