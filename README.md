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

**One thing does NOT derive automatically:** if you change `app.package`, you must also create/move `app/src/main/java/...` to match the new package path, and set the matching `package ...` line at the top of your Kotlin files. The `namespace`/`applicationId` in Gradle and the folder structure of your Kotlin sources are two different things — Gradle doesn't create or rename folders for you.

## No source, no launcher activity — by design

`app/src/main/java` ships empty. Because of that, `AndroidManifest.xml` deliberately declares **no `<activity>`**. The app builds and installs fine, it just has nothing to launch — no crash, no app-drawer entry, until you add your own Activity.

When you add one (e.g. `MainActivity.kt`), declare it in the manifest yourself:

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

(A commented-out copy of this is already sitting in the manifest as a reminder.)

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
