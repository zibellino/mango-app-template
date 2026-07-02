# Mango app template

Android app template, builds with Gradle on GitHub.

## Using this template for a new app

Edit `app.properties` at the repo root:

```
app.name=YourAppName
app.package=com.yourorg.yourapp
```

That's the only file you need to touch. Everything else derives from it at build time:
- `settings.gradle.kts` reads `app.name` for `rootProject.name`
- `app/build.gradle.kts` reads `app.package` for `namespace`/`applicationId`, and injects `app.name` as the `app_name` string resource
- `AndroidManifest.xml` reads `@string/app_name` for the label — never hardcoded
- CI (`.github/workflows/build.yml`) reads `app.properties` directly (via `grep`) to name build artifacts and release APKs

Renaming later is the same process — edit `app.properties`, rebuild. No script to run, nothing to delete, no repo-wide find-and-replace.

Add your source under `app/src/main/java/<package path>/`, starting with `MainActivity.kt`. Add a launcher icon under `app/src/main/res/` (the manifest currently has no `android:icon` since none is checked in yet).

## Building

```
gradle assembleDebug
```

Requires JDK 17 and Android SDK. CI via GitHub Actions on every push and release; the Gradle wrapper is generated fresh in CI rather than committed.
