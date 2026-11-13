# 4 · Deployment stories for MVP 3

Format of the team board (`opti-docs` issues): title, story, acceptance criteria in
Given/When/Then, priority. Priorities follow the risk of each finding in `01` and `02`.

### DEP-01 · CI publishes a versioned, traceable image (P0)

As the team, we want every merge to `develop` to publish an image tagged with the commit SHA,
so that what runs in any environment can be traced to a commit and promoted without rebuilding.

- Given a PR merged to `develop` of `opti-customers-api`, when `publish.yml` finishes, then GHCR
  has `opti-customers-api:sha-<7 chars>` and `:develop` pointing to the same digest.
- Given that image, when I run `docker inspect`, then the label
  `org.opencontainers.image.revision` equals the merge commit SHA.
- Given a push of tag `v3.0.0`, when the workflow finishes, then `:v3.0.0` exists and has the
  **same digest** as the `sha-*` image of that commit (no new build step ran).

### DEP-02 · Promotion is a retag, never a rebuild (P0)

- Given `:sha-abc1234` passed qa, when it is promoted to `main`, then the workflow log shows
  `imagetools create` and no `docker build`.
- Given the `main` host, when it deploys, then `docker compose ... up -d --no-build` is used and
  `docker images` on that host shows no `opti/*:local` image.

### DEP-03 · Environments are created from IaC only (P1)

- Given a clean host with Docker, when I follow `opti-infra/README.md` with
  `.env.qa.example` and the qa secrets, then the platform is up and every service is `healthy`
  with no manual step outside the README.
- Given `deploy/compose.release.yml`, when `OPTI_VERSION=v3.0.0`, then every service uses
  `ghcr.io/code-corhuila/<repo>:v3.0.0`.

### DEP-04 · Blue-green deploy of a stateless API with health checks (P1)

- Given blue serves `/api/v1/patients`, when `blue-green.sh deploy customers-api` runs, then
  green is `healthy` before the gateway is switched, and a client polling every 200 ms during
  the switch gets **0 responses with status `5xx`**.
- Given green is not healthy within 90 s, when the script times out, then the gateway still
  points to blue and the script exits non-zero.
- Given the switch is done, when `opti-workflow` calls customers-api, then it reaches green
  within 60 s (network alias step).

### DEP-05 · `opti-infra` CI fails on an invalid composition (P1)

- Given a PR with a typo in a `compose.yml`, when CI runs, then the job fails (remove `|| true`
  and resolve `include:` by checking out the siblings at the PR's target branch).

### DEP-06 · Deployment docs describe what really runs (P2)

- Given `05-architecture/deployment.md` and `10-devops/environments.md`, when a reviewer
  compares them with `opti-infra`, then there is no mention of Kubernetes rollback commands,
  `ms-*` names or one shared PostgreSQL instance.

### DEP-07 · Rehearsed rollback reverts prod in < 5 min (P0)

- Given a bad version is deployed with blue-green, when the runbook level 1 is applied, then
  `/api/v1/patients` answers `200` again in under 5 min from the decision, measured by
  `rehearse-rollback.sh`, and the result is recorded in `03-rollback-runbook.md`.
- Given the rehearsal, when it runs, then the number of failed client requests during the
  switch and the rollback is recorded.
