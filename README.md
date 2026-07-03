# Mango app template

Android app template, builds with Gradle on GitHub.

## Using this template for a new app

Edit `app.properties` at the repo root:

```
app.name=YourAppName
app.package=com.yourorg.yourapp
app.version=0.1.0
```

Most of this derives automatically at build time:
- `settings.gradle.kts` reads `app.name` for `rootProject.name`
- `app/build.gradle.kts` reads `app.package` for `namespace`/`applicationId`, and injects `app.name` as the `app_name` string resource
- `AndroidManifest.xml` reads `@string/app_name` for the label — never hardcoded
- `app.version` is the fallback `versionName` for local/dev builds (see Versioning below)

## MainActivity — flat, no package, nothing to rename

`app/src/main/kotlin/MainActivity.kt` is a black-screen placeholder, and it's genuinely your **real** MainActivity — not a throwaway to delete later. Build your actual UI directly into this file.

It has no `package` declaration and sits with no folder nesting under `kotlin/`. Kotlin doesn't require source layout to mirror package structure (that's an IDE/Java convention, not a compiler rule), so this is legal and compiles fine. It also means the file is completely independent of `app.package` in `app.properties` — rename your app all you want, this file never moves and never needs editing for that reason.

The manifest reflects this: `android:name="MainActivity"` has no leading dot, because that's the actual fully-qualified name of a default-package class (a leading dot is Android's shorthand for "prepend the namespace").

`src/main/kotlin` (rather than the more common `src/main/java`) is used here since AGP recognizes both as default source roots, and this is a pure-Kotlin project with no Java files.

If you eventually want it under a real package, add a `package ...` line as the first line of the file and move it wherever you like — nothing about the build depends on it staying flat.

## Versioning

`versionCode`/`versionName` are computed in CI, not hardcoded:
- **Release** (GitHub release created): `versionName` = the release tag name, `versionCode` = the CI run number
- **Push** (branch, e.g. `master`): `versionName` = the branch name (slashes replaced with `-`)
- **Pull request**: `versionName` = the PR's source branch name
- Every build artifact also gets a short commit SHA appended to its filename for disambiguation

Locally (`gradle assembleDebug` with no CI flags), it falls back to `app.version` from `app.properties` and `versionCode = 1` — fine since local builds are never uploaded anywhere.

## Building

```
gradle assembleDebug
```

Requires JDK 17 and Android SDK. CI via GitHub Actions on every push, PR, and release; the Gradle wrapper is generated fresh in CI rather than committed.
