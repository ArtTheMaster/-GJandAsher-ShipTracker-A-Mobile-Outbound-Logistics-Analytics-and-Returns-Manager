# Quick Guide: Running GJandAsher ShipTracker in Android Studio

This guide is specifically for groupmates running this Flutter/Dart project in **Android Studio**.

---

## ⚡ 3-Step Quick Start

### 1. Open Project in Android Studio
1. Open **Android Studio**.
2. Select **Open** (or `File > Open...`).
3. Browse to this directory (`gjandasher_shiptracker`) and click **OK**.
4. Android Studio will open the project and read the pre-configured `.idea/` workspace.

### 2. Verify Plugins & SDK
- **Plugins**: Verify that the **Flutter** and **Dart** plugins are installed (`File > Settings > Plugins` on Windows/Linux or `Android Studio > Settings > Plugins` on macOS).
- **Pub Get**: In `pubspec.yaml`, click **"Flutter pub get"** in the top action banner (or press `Alt + F12` and run `flutter pub get`).

### 3. Run the App
- In the top toolbar, ensure **`main.dart`** is selected in the Run/Debug configurations dropdown (pre-configured in `.idea/runConfigurations/main_dart.xml`).
- Choose your target in the device dropdown:
  - **Android Emulator** (Pixel 8, etc.)
  - **Physical Android Phone/Tablet** via USB
  - **Chrome (web)** for fast instant rendering
- Click the **Green Play button (Run)** (`Shift + F10`) or **Debug button** (`Shift + F9`).

---

## 🛠️ Flutter Features Available in Android Studio

- **⚡ Hot Reload**: Press `Ctrl + \` (Windows/Linux) or `Cmd + \` (macOS), or click the yellow lightning bolt button on the toolbar to instantly apply code changes without restarting the app.
- **🔄 Hot Restart**: Press `Ctrl + Shift + \` (Windows/Linux) or `Cmd + Shift + \` (macOS) to reset app state and reload.
- **🔍 Flutter Inspector**: On the right sidebar, click **Flutter Inspector** to visualize the widget hierarchy and tweak properties in real-time.
- **📊 Flutter Outline**: View and refactor your widget tree hierarchy directly.

---

## 🤝 Cross-IDE Team Workflow (Android Studio + VS Code)

- Both team members work directly on the shared `lib/` directory.
- Color architecture and spatial layout rules are centralized in `lib/theme/rule_60_30_10.dart` and `lib/theme/app_theme.dart`.
- Changes pushed by the VS Code member or Android Studio member sync effortlessly without configuration collisions.

---

## 🔧 Common Windows / Android Studio Troubleshooting

### Error: `Waiting for another flutter command to release the startup lock...`
If Android Studio shows this error and red squiggly lines under `package:flutter/...`:
1. **Kill hanging Dart processes**: Open Task Manager (`Ctrl + Shift + Esc`) and terminate any `dart.exe` or `flutter` processes.
2. **Delete the lockfile**: Go to your Flutter SDK installation directory:
   `...\flutter\bin\cache\`
   Delete the file named **`lockfile`**.
3. **Run Pub Get**: Return to Android Studio, open `pubspec.yaml`, and click **Flutter pub get** (or in terminal: `flutter pub get`).
4. *(Recommendation)*: If your Flutter SDK is installed in a OneDrive-synced folder (`OneDrive/Desktop/...`), move Flutter to `C:\src\flutter` or `C:\flutter` to prevent OneDrive from file-locking active cache files.

