# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Logging improvement.
- Migrated the CI pipeline from Jenkins to GitHub Actions.

## [2.1.0] - 2024-04-12
### Changed
- Java source and target levels `1.6` -> `1.8`
- Kotlin version `1.4.20` -> `1.7.20`
### Upgraded
- Keyple Plugin API `2.0.0` -> `2.3.1`
- Keyple Util Lib `2.1.0` -> `2.4.0`
- Gradle `6.8.3` -> `7.6.4`
### Added
- Added project status badges on `README.md` file.
### Fixed
- CI: code coverage report when releasing.
### Removed
- Dependency to logger implementation.

## [2.0.1] - 2022-06-09
### Added
- "CHANGELOG.md" file (issue [eclipse-keyple/keyple#6]).
- CI: Forbid the publication of a version already released (issue [#3]).
### Fixed
- Removal of the unused Jacoco plugin for compiling Android applications that had an unwanted side effect when the application was launched (stacktrace with warnings).
### Upgraded
- "Keyple Util Library" to version `2.1.0` by removing the use of deprecated methods.

## [2.0.0] - 2021-10-06
This is the initial release.
It follows the extraction of Keyple 1.0 components contained in the `eclipse-keyple/keyple-java` repository to dedicated repositories.
It also brings many major API changes.

[unreleased]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/compare/2.1.0...HEAD
[2.1.0]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/compare/2.0.1...2.1.0
[2.0.1]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/compare/2.0.0...2.0.1
[2.0.0]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/releases/tag/2.0.0

[#3]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/issues/3

[eclipse-keyple/keyple#6]: https://github.com/eclipse-keyple/keyple/issues/6