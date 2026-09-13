# Rule: Mandatory Version Bumping, Dedicated Release Folders & Standards

1. **Mandatory Version Increment**:
   - Every time code modifications are made and a build is generated, ALWAYS bump `versionCode` (+1) and `versionName` in `app/build.gradle.kts`.
   - Never release a new APK with the same version number as the previous build.

2. **Dedicated Version Directory (`releases/v<versionName>/`)**:
   - Starting from v1.1.4, EVERY new release MUST be saved into its own dedicated directory under `releases/v<versionName>/`.
   - The directory must contain:
     - `Kboard_v<versionName>.apk`
     - `Kboard.apk`
     - `Kaius_Receiver.exe`
     - `RELEASE_NOTES.md`
   - Also maintain sync with root `Kboard.apk` and `Kaius_Keyboard.apk`.

3. **Display Version in UI**:
   - App version MUST be visible in `LandscapeUnifiedHeader` as `v<versionName>`.
   - App version MUST be visible in `KaiusImeService` banner as `v<versionName>`.

4. **No Emoji Icons Rule**:
   - Do NOT use colorful emojis in the user interface, status text, buttons, logs, or documentation.
   - Maintain a minimalist, technical industrial dark-mode aesthetic with clean typography and Material vector icons.

5. **Git Commits**:
   - Every step or task completed must be committed to local git with an informative commit message.
