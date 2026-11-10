# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
- APDU exchange with a card selected without AID (e.g. a SAM selected by its power-on data) failed with
  "Channel is not open": the basic channel is now opened on first transmission.
- The OMAPI session was not closed when closing the physical channel if no channel had been opened.
- R8 build failure of the applications using the plugin ("Missing class
  org.simalliance.openmobileapi..."): the library now provides the required consumer rule.
### Changed
- Android applications written in Kotlin must now use Kotlin `2.2` or later to compile against this library.
### Removed
- Unused `viewBinding` build feature and `kotlin-parcelize` plugin: the `androidx.databinding:viewbinding` dependency is
  no longer propagated to the applications.
### Upgraded
- `keyple-util-java-lib` from `2.4.1` to `2.5.0`
- `kotlin-stdlib` and Kotlin Gradle plugin from `1.7.20` to `2.3.21`
- Android Gradle plugin from `7.4.2` to `9.4.1`
- Android `compileSdk` from `33` to `35`
- Gradle from `8.9` to `9.8.1`
- Dokka from `1.9.20` to `2.2.0`
- Spotless from `7.0.4` to `8.2.1`

## [2.1.1] - 2026-02-20
### Changed
- Normalized logging and error messages using Keyple coding standards.
- Migrated the CI pipeline from Jenkins to GitHub Actions.
### Upgraded
- `keyple-plugin-java-api` from `2.3.1` to `2.3.2`
- `keyple-common-java-api` from `2.0.1` to `2.0.2`
- `keyple-util-java-lib` from `2.4.0` to `2.4.1`
- `slf4j-api` from `1.7.32` to `1.7.36` (compileOnly)

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

[unreleased]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/compare/2.1.1...HEAD
[2.1.1]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/compare/2.1.0...2.1.1
[2.1.0]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/compare/2.0.1...2.1.0
[2.0.1]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/compare/2.0.0...2.0.1
[2.0.0]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/releases/tag/2.0.0

[#3]: https://github.com/eclipse-keyple/keyple-plugin-android-omapi-java-lib/issues/3

[eclipse-keyple/keyple#6]: https://github.com/eclipse-keyple/keyple/issues/6