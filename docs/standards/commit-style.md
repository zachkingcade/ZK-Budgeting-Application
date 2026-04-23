# Commit style

**Purpose**: Define the repository commit message format and categories.
<br>
Last updated: 2026-04-22

## Template

Commits should follow the below format:

```
[Category of work] [#ticket number if applicable] v[version.numbers.] Short Summary of work done
```

Example: `fix #213 v2.3.2 Corrected an issue where the favicon did not display correctly on Linux machines`

## Categories of work

- `feat`: Introduces a new feature.
- `fix`: Fixes a bug.
- `refactor`: Refactors code without changing functionality.
- `style`: Updates style (formatting only; no behavior change).
- `docs`: Updates documentation.
- `test`: Adds or updates tests.
- `perf`: Improves performance.
- `ci`: Updates CI/CD configuration.
- `build`: Changes related to the build system or dependencies.
- `revert`: Reverts a previous commit.

