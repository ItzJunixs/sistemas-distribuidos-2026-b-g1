# Git Branching Strategy & Workflow Specification

## 1. Branch Hierarchy
- `main`: Production-ready releases. Protected by ruleset ID 22364620 (Requires passing CI checks and formal release sign-off by Course Evaluator/Teaching Team; strictly 0 author bypass actors).
- `qa`: Integration and quality assurance staging environment. Fed only by child `qa/` branches.
- `develop`: Primary integration branch for ongoing sprint work. Protected by ruleset ID 22364620 (Requires 1 mandatory approving peer review from Team G1 members; strictly 0 bypass actors).
- `feat/HU-XXX-<slug>`: Feature branches branched from `develop` and merged strictly via Pull Request with passing CI checks and mandatory peer review.

## 2. Promotion Model (Re-application via cherry-pick -x)
In accordance with course governance (`00-governance/branching-policy.md`):
- `merge develop -> qa` and `merge qa -> main` are strictly prohibited.
- Promotion from `develop` to `qa` is done via dedicated child branches (`qa/HU-XXX-<slug>`) using `git cherry-pick -x <sha>` to preserve full commit provenance.
- Promotion to `main` is performed via release branches (`release/vX.Y.Z`) requiring mandatory evaluation sign-off.

## 3. Commit Conventions (Conventional Commits)
Format: `type(scope): imperative summary`
- `feat`: New feature or user story implementation
- `fix`: Bug fix
- `docs`: Documentation updates
- `test`: Unit or integration tests
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `chore`: Build tasks, package updates, configuration

## 4. Pull Request & Quality Gates
1. **Mandatory Approvals:** At least 1 approving peer/lead review required; direct push and author bypass are strictly disabled.
2. **Conversation Resolution:** All review comment threads must be resolved before merge.
3. **Stale Review Dismissal:** New commits automatically invalidate previous approvals.
4. **Linear Commit History:** Fast-forward or rebase squash required (no non-fast-forward pushes).
5. **Passing Automated Tests:** CI test pipelines must pass (100% green).
6. **Zero Exposed Secrets:** Automated GitGuardian scan verification without default fallback credentials.
