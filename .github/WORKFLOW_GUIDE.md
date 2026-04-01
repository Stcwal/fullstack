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

### Viewing Results

1. **On the PR Page**:
   - Go to your pull request on GitHub
   - Scroll down to the "Checks" section
   - You'll see "Backend Test Results" with a summary
   - Click "Details" to view individual test results

2. **On GitHub Actions Tab**:
   - Go to the "Actions" tab in the repository
   - Find the workflow run titled "Run Tests"
   - Click to view detailed logs and artifacts

3. **Test Reports**:
   - If tests fail, XML reports are saved as artifacts
   - Download the `test-reports` artifact to view detailed failure information

### What Happens If Tests Fail

- The PR check will show as **Failed** (red X)
- The workflow provides detailed error messages and stack traces
- Test reports are archived for debugging
- You must fix failing tests before merging (if branch protection is enabled)

### Troubleshooting

**Workflow doesn't run:**
- Verify the PR targets `main` or `develop` branch
- Check the "Actions" tab for error logs

**Tests pass locally but fail in CI:**
- Ensure Java 21 is installed locally: `java -version`
- Clear Maven cache: `mvn clean`
- Check for environment-specific issues (file paths, line endings on Windows)

**Need to skip a test temporarily:**
- Use `@Disabled` annotation on the test method
- Document why it's skipped with a comment

## Future Enhancements

- [ ] Frontend unit tests with Vitest
- [ ] Frontend E2E tests with Cypress/Playwright
- [ ] Code coverage reporting
- [ ] SonarQube integration for code quality
- [ ] Performance/load testing
