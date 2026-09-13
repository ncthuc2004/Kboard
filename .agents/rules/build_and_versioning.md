# Rule: Mandatory Version Bumping, Clean Design & Release Standards

1. **Mandatory Version Increment**:
   - Every time code modifications are made and a build is generated, ALWAYS bump `versionCode` (+1) and `versionName` in `app/build.gradle.kts`.
   - Never release a new APK with the same version number as the previous build.

2. **Display Version in UI**:
   - App version MUST be visible in `LandscapeUnifiedHeader` as `v<versionName>`.
   - App version MUST be visible in `KaiusImeService` banner as `v<versionName>`.

3. **No Emoji Icons Rule**:
   - Do NOT use colorful emojis (such as 🔍, ⌨️, 🖥️, ⚡, 🇻🇳, 🇺🇸, 🔗, 📋, 📡, ⚙️, etc.) in the user interface, status text, buttons, logs, or documentation.
   - Maintain a minimalist, technical industrial dark-mode aesthetic with clean typography and Material vector icons.

4. **Artifact Deployment**:
   - The compiled debug APK must always be copied to `t:\Kaius_Inc\K_Keyboard\Kaius_Keyboard.apk` after `./gradlew assembleDebug`.

5. **Git Commits**:
   - Every step or task completed must be committed to local git with an informative commit message.
