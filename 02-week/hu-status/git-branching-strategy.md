# Git Branching Strategy & Workflow Specification

## 1. Branch Hierarchy
- `main`: Production-ready releases. Protected by ruleset ID 22364620 (Bypass enabled for Ximena Zambrano / Repository Admin).
- `qa`: Integration and quality assurance staging environment.
- `develop`: Primary integration branch for ongoing sprint work. Default branch. Protected by ruleset ID 22364620 (Bypass enabled for Ximena Zambrano / Repository Admin).
- `feat/HU-XXX-<slug>`: Feature branches branched from `develop` and merged via Pull Request or direct push by Ximena Zambrano without requiring peer reviews or approvals.

## 2. Commit Conventions (Conventional Commits)
Format: `type(scope): imperative summary`
- `feat`: New feature or user story implementation
- `fix`: Bug fix
- `docs`: Documentation updates
- `test`: Unit or integration tests
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `chore`: Build tasks, package updates, configuration

## 3. Pull Request & Quality Gates
- Autonomous workflow for Ximena Zambrano:
  1. No peer approvals required: Ximena Zambrano pushes and creates PRs autonomously without depending on peer reviews from other students (Celeste or Camilo Penagos).
  2. Professor & Automation Evaluation: Final review, evaluation and merging into protected branches is handled exclusively by the Professor and automated GitHub Actions / bot checks.
  3. Bypass list enabled: Repository admins / Ximena Zambrano have bypass privileges on required reviews.
  4. Linear commit history (no merge conflicts).
  5. Passing automated unit tests and CI checks.
  6. Zero exposed secrets (GitGuardian verified).
