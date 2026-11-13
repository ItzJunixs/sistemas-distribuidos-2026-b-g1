# 4 · DORA metrics of OptiView (Corte 2 → MVP 3)

How measured: local git history of the 16 code repos (merge commits of PRs on `develop` and
`main`, author date of the first commit of each PR branch) and the GitHub Actions runs. Period:
2026-10-02 (first merge) → 2026-11-11. "Production" = `main`; "deploy" = a release merged to
`main` (we have no hosted prod environment, see week 12).

| Metric | Value | How it was obtained | DORA band (approx.) |
|---|---|---|---|
| Deployment frequency | **1 release to `main` in 6 weeks** (`release/1.1.0`, 2026-10-07, 17 repos). MVP 3 (`release/3.0.0`) is open, not merged. Images to GHCR from develop: on 2026-10-06 and 2026-10-07 only | `git log origin/main --first-parent --merges` · Actions `Publish image` successes | low (monthly or less) |
| Lead time for changes | PR branch → `develop`: median **< 1 h**, p90 **≈ 116 h** (130 PRs). `develop` → `main`: the MVP 3 changes merged on 2026-11-10 are still not on `main` | first author date of the PR's commits vs merge commit date | the median is not real: see note 1 |
| Change failure rate | **≈ 21 %** proxy: 27 of 130 PRs into develop are `fix/*` branches correcting something already merged | branch names of the merges | medium |
| Time to restore | **not measurable**: no production incidents recorded. The week 11 drill had TTD = no alert and the rollback was never rehearsed (week 12 `rehearse-rollback.sh` closes this) | — | unknown |

## What the numbers say

1. **Big batches.** All 130 merges into develop happened on **5 days**: 2026-10-02 (27),
   10-03 (18), 10-05 (33), 10-06 (43) and 11-10 (9). Nothing between 10-07 and 11-09. MVP 3 came
   as one branch per repo (`feat/pdf-payments-validation`) merged in a single night. That is
   the opposite of "many small changes": a big batch is hard to review and, with CI down, was
   not tested at all.
2. **Note 1 — lead time.** The median < 1 h means most PR commits are created minutes before
   the merge (work done elsewhere and pushed at the end), so the metric does not see the real
   time from "started" to "merged". A more honest measure for the team is from the HU moving to
   *In Progress* on the board to the merge.
3. **Release cadence is tied to the course cuts**, not to readiness. That is expected in an
   academic project, but the pipeline should still let us release any green `develop`.

## Targets for MVP 3 and the final delivery

| Metric | Target | Lever |
|---|---|---|
| Deployment frequency | release to `main` every week of the final sprint, by retag (week 12 `publish.yml`) | promotion without rebuild + smoke |
| Lead time | ≤ 2 days from HU *In Progress* to `develop`, PRs < 400 lines | slice HUs smaller; one PR per HU, not one per release |
| Change failure rate | < 15 % | integration tests in CI (G4), coverage floor (G2) |
| Time to restore | < 5 min for a bad deploy | rehearsed blue-green rollback (week 12, DEP-07) |
