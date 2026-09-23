# Third-party components

The MIT license at the repository root covers the original application code and documentation. It does not replace the licenses of third-party components.

- Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`): Apache License 2.0. The original script notices are preserved, and the JAR contains `META-INF/LICENSE`.
- Android Studio-generated starter files are retained in this project. Existing source notices, where present, are preserved.
- Runtime dependencies for version 1.0.1 are listed with their resolved versions in [docs/runtime-dependencies.txt](docs/runtime-dependencies.txt). These 71 components declare Apache License 2.0. This list is derived from the release packaging dependency graph, not only the direct version catalog.
- [app/src/main/assets/THIRD_PARTY_LICENSES.txt](app/src/main/assets/THIRD_PARTY_LICENSES.txt) is packaged in the APK and can be read offline through the clock's five-tap settings menu → ライセンス・利用条件. It contains the original application's MIT license, Apache 2.0, Kotlin's incorporated ThreeTenBP BSD-3-Clause and Boost license, GWT/Guava attributions, and the upstream kotlinx.coroutines / kotlinx.serialization notices. The same file accompanies the GitHub release.
- Original AndroidX license files remain in the APK's META-INF directory. The consolidated notice supplements them; it does not replace their terms.
- Build/test-only dependencies (including Android Gradle Plugin and JUnit) are not part of the runtime list. Their own licenses apply when distributing those tools. Gradle Wrapper's Apache license is also reproduced in the consolidated notice.

When changing dependencies, regenerate the resolved runtime list and review the upstream licenses and NOTICE files, including third-party code within each dependency. The Kotlin source for this review is the [version 2.2.10 license inventory](https://github.com/JetBrains/kotlin/blob/v2.2.10/license/README.md). A top-level Apache declaration alone is not sufficient to rule out additional terms.

LINE, Google Gallery, camera, contacts and dialer applications are not distributed with this repository. References to those products describe integration targets and do not imply endorsement.
