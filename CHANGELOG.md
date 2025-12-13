# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2025-12-12

### Fixed
- **State Management**: Restored agent state management by re-enabling `ToolContext` interactions that were previously commented out.
- **ADK API Compatibility**: Corrected all calls to the `ToolContext` state API (`toolContext.state()`) to align with version `0.3.0` of the ADK, resolving critical compilation errors.
- **Test Suite**: Repaired the entire test suite by properly mocking the `ToolContext` and `State` objects, fixing `NullPointerException`s and compilation failures. Re-enabled all previously disabled tests.
- **Build Configuration**: Adjusted the `maven-surefire-plugin` version for improved build stability.
- **Project Organization**: Relocated all Java files to their correct directories.
- **Naming Conventions**: Renamed `configuration.java` to `Configuration.java`.
- **Spring Integration**: Added `@Component` annotation to `CustomerSupportAgent` and updated `App.java` to use dependency injection.
- **Test Isolation**: Added a `@BeforeEach` setup method to reset the mock data before each test.

### Changed
- **Documentation**: Overhauled `README.md` for clarity, conciseness, and accuracy. Removed misleading "production-ready" claims and updated the development status.
- **Code Cleanup**: Removed unnecessary markdown files.

### Added
- **CHANGELOG.md**: Created this changelog to track future updates.
