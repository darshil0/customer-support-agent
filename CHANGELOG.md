# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2024-07-25

### Fixed
- **Project Organization**: Relocated all Java files to their correct directories.
- **Code Cleanup**: Removed unnecessary markdown files.
- **Naming Conventions**: Renamed `configuration.java` to `Configuration.java`.
- **Spring Integration**: Added `@Component` annotation to `CustomerSupportAgent` and updated `App.java` to use dependency injection.
- **Build Configuration**: Updated `pom.xml` to version `1.0.2`.
- **Test Isolation**: Added a `@BeforeEach` setup method to reset the mock data before each test.

### Added
- **CHANGELOG.md**: Created this changelog to track future updates.
