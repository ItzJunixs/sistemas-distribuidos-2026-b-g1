<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
    Your weekly grade is read AUTOMATICALLY from this file:
      02-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 02

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Ximena del Pilar Zambrano Chala
- GITHUB_USER: XimenaChala
- TEAM: G1
- SPRINT_GOAL: Set up the MVP 1 Scrum board, repo, branch strategy (develop/qa/main), branch protection, and labels so the 3-person team can start their HU branches
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| N/A | Sprint 1 setup (repo, branches, rulesets, labels, Project board) | done | [ruleset-protect-develop-main.json](./ruleset-protect-develop-main.json), [git-branching-strategy.md](./git-branching-strategy.md) |

## 2. My individual contribution
- Created the `edutrack` repository and configured `develop`, `qa`, `main` branches
- Set up GitHub ruleset ID 22364620 protecting `develop`, `qa`, and `main` (1 mandatory peer review required, conversation resolution enabled, and 0 author bypass actors) -> see local evidence file `ruleset-protect-develop-main.json`
- Defined Git branching conventions and quality gates -> see `git-branching-strategy.md`
- Created the 14 custom labels (hu-001–hu-005, module:*, type:*) -> see `labels-setup.json`
- Built the GitHub Project board (Backlog, To Do, In Progress, In Review, Done) with Módulo and Historia fields -> see `scrum-board-setup.md`
- Prepared repository setup for team distribution across HU-001 to HU-005

## 3. Blockers and risks
- Team has not yet confirmed who owns each HU / module among the 3 members

## 4. Plan for next week
- Confirm HU/module assignment per team member
- Open the first `feat/HU-XXX` branches and start implementation

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment (feat/HU-XX -> develop, HU-XX-qa -> qa, release/x.x.x -> main)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence files & links
- Local Evidence Files:
  - Ruleset Configuration: [`ruleset-protect-develop-main.json`](./ruleset-protect-develop-main.json)
  - Git Strategy: [`git-branching-strategy.md`](./git-branching-strategy.md)
  - Scrum Board: [`scrum-board-setup.md`](./scrum-board-setup.md)
  - Labels Setup: [`labels-setup.json`](./labels-setup.json)
- Remote Repositories:
  - Backend Repo: https://github.com/code-corhuila/edutrack
  - Docs Repo: https://github.com/code-corhuila/educk-docs