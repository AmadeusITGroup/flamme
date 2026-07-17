# Contributing to Flamme

Thank you for your interest in contributing. The following guidelines will help you get started.

## Table of contents

- [Code of conduct](#code-of-conduct)
- [Getting started](#getting-started)
- [Development workflow](#development-workflow)
- [Coding standards](#coding-standards)
- [Submitting changes](#submitting-changes)
- [Reporting issues](#reporting-issues)

---

## Code of conduct

Please be respectful and constructive in all interactions. We follow the standard open-source community norms — harassment or discriminatory language of any kind is not tolerated.

---

## Getting started

### Prerequisites

| Tool  | Minimum version |
|-------|----------------|
| Java  | 21             |
| Maven | 3.9            |
| NATS  | 2.x |

### Building the project

```bash
git clone https://github.com/amadeusitgroup/flamme.git
cd flamme
mvn install -DskipTests
```

### Running the example

```bash
cd flamme-example
mvn quarkus:dev
```

See the [Getting Started](./README.md#getting-started) section in the README for a walkthrough of the example application.

---

## Development workflow

1. **Fork** the repository and create your branch from `main`:

   ```bash
   git checkout -b feat/my-feature
   ```

2. **Make your changes** — keep commits small and focused.

3. **Run the tests** before pushing:

   ```bash
   mvn verify
   ```

4. **Check formatting** (the project uses [Spotless](https://github.com/diffplug/spotless)):

   ```bash
   mvn spotless:check
   # auto-fix with:
   mvn spotless:apply
   ```

5. **Open a pull request** against `main`. Fill in the PR template.

---

## Coding standards

- Java 21, targeting compatibility with Quarkus 3.x.
- All public APIs must have Javadoc.
- New features should come with unit or integration tests.
- Do not disable or weaken existing tests.

---

## Submitting changes

- One logical change per PR.
- Reference the related issue in the PR description (`Closes #<number>`).
- Keep the PR description up to date if the scope changes during review.
- A maintainer will review and may request changes before merging.

---

## Reporting issues

Use the GitHub issue templates:

- **[Bug report](.github/ISSUE_TEMPLATE/bug_report.md)** — for reproducible defects.
- **[Feature request](.github/ISSUE_TEMPLATE/feature_request.md)** — for new functionality or improvements.

Include as much detail as possible — a minimal reproducer is the fastest way to get a fix.
