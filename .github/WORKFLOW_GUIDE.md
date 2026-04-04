# GitHub Actions CI Workflow Guide

## Overview

This document describes the CI workflow that automatically runs tests on pull requests to ensure code quality and prevent regressions.

## Workflow: Run Tests

**File**: `.github/workflows/test.yml`

### When It Runs

The workflow automatically triggers on:
- **Pull Request Creation**: When a PR is opened against `main` or `develop` branches
- **Pull Request Updates**: When new commits are pushed to an open PR

### What It Does

1. **Checks out the code** from the PR branch
2. **Sets up Java 21** (using Eclipse Temurin distribution with Maven caching)
3. **Runs backend tests**: Executes `mvn clean test` from the `backend/` directory
4. **Publishes results**: Test results are displayed directly on the PR as a check
5. **Archives reports**: XML test reports are stored as artifacts for 30 days

### Test Framework

- **Backend**: JUnit 5 + Spring Test + Spring Security Test
- **Testing Tool**: Maven Surefire
- **Database**: H2 in-memory (no external services required)
- **Test Location**: `backend/src/test/java/backend/fullstack/`

### Running Tests Locally

To reproduce the same tests locally:

```bash
cd backend
mvn clean test
```

Or from the project root:

```bash
mvn clean test --file backend/pom.xml
```
