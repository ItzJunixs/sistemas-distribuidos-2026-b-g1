# Git Branching Strategy & Workflow Specification

## 1. Branch Hierarchy
- `main`: Production-ready releases. Direct pushes forbidden. Protected by ruleset ID 22364620.
- `qa`: Integration and quality assurance staging environment.
- `develop`: Primary integration branch for ongoing sprint work. Default branch. Protected by ruleset ID 22364620.
- `feat/HU-XXX-<slug>`: Feature branches branched from `develop` and merged via Pull Request with required peer review.

## 2. Commit Conventions (Conventional Commits)
Format: `type(scope): imperative summary`
- `feat`: New feature or user story implementation
- `fix`: Bug fix
- `docs`: Documentation updates
- `test`: Unit or integration tests
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `chore`: Build tasks, package updates, configuration

## 3. Pull Request & Quality Gates
- Every PR targeting `develop` or `main` requires:
  1. Minimum 1 approving review from a peer reviewer.
  2. All conversation threads resolved.
  3. Linear commit history (no merge conflicts).
  4. Passing automated unit tests.
  5. Zero exposed secrets (GitGuardian verified).
