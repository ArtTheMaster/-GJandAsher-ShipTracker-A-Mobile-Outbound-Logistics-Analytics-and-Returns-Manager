# GJandAsher ShipTracker (Flutter / Dart)

Outbound Logistics, Analytics, and Returns Manager built with **Flutter & Dart**, fully configured for dual-team development in **Android Studio** and **Visual Studio Code**.

---

## 👥 Dual-IDE Setup: Android Studio & Visual Studio Code

Whether you develop in **Android Studio** (Groupmate) or **Visual Studio Code** (User), both IDEs share the exact same Flutter/Dart business logic, models, controllers, and 60-30-10 design system without conflicts.

| Feature | Android Studio (Groupmate) | Visual Studio Code (You) |
|---|---|---|
| **Entry Point** | `lib/main.dart` | `lib/main.dart` |
| **Pre-Configured Run Profile** | `.idea/runConfigurations/main_dart.xml` | `.vscode/launch.json` |
| **Run Command** | Green Play Button (`Shift + F10`) | `F5` / "Run and Debug" |
| **Debug Command** | Debug Bug Button (`Shift + F9`) | `F5` Debug Mode |
| **Hot Reload** | `Ctrl + \` / `Cmd + \` or ⚡ button | `r` in terminal or Hot Reload icon |
| **Hot Restart** | `Ctrl + Shift + \` / `Cmd + Shift + \` | `R` in terminal or Restart icon |
| **Target Devices** | Android Emulator, Physical Phone, Chrome Web | Chrome Web, Android Emulator, Desktop |
| **State & Inspections** | Flutter Inspector & Flutter Outline | Flutter Widget Inspector / DevTools |

---

## 🤖 How to Run in Android Studio (For Your Groupmate)

1. **Prerequisites in Android Studio**:
   - Ensure you have the **Flutter** and **Dart** plugins installed:
     - Go to **Settings/Preferences > Plugins > Marketplace**
     - Search for `Flutter`, click **Install** (Dart is bundled automatically), and restart Android Studio if prompted.
   - If prompted for the **Flutter SDK path**:
     - Go to **Settings > Languages & Frameworks > Flutter** and point it to your local Flutter SDK folder (e.g. `C:\src\flutter` or `/Users/.../flutter`).

2. **Open the Project**:
   - Open Android Studio.
   - Click **Open** (or **File > Open...**).
   - Select the root folder of this project (`gjandasher_shiptracker`).
   - Android Studio will detect the `.idea/` folder and `pubspec.yaml`.

3. **Fetch Dependencies**:
   - When `pubspec.yaml` opens, click the blue **"Flutter pub get"** banner at the top, or run in the built-in terminal (`Alt + F12`):
     ```bash
     flutter pub get
     ```

4. **Select Target Device**:
   - In the top toolbar device dropdown, select your preferred target:
     - Any running **Android Emulator** (from Device Manager)
     - A connected **Physical Android device** (with USB debugging enabled)
     - Or **Chrome (web)** for fast instant rendering without launching an emulator.

5. **Run the App**:
   - The top run configuration dropdown will automatically show **`main.dart`** (configured via `.idea/runConfigurations/main_dart.xml`).
   - Click the green **Run (Play)** button (`Shift + F10`) or **Debug** button (`Shift + F9`).
   - Enjoy instant **Hot Reload** and full Flutter DevTools integration!

---

## 💻 How to Run in Visual Studio Code (For You)

1. **Prerequisites in VS Code**:
   - Install the official **Flutter** extension from the Extensions view (`Ctrl + Shift + X`).

2. **Open & Run**:
   - Open the root folder in VS Code (`File > Open Folder...`).
   - Open terminal (`Ctrl + \``) and run:
     ```bash
     flutter pub get
     ```
   - Press **`F5`** or open the **Run & Debug** tab (`Ctrl + Shift + D`) and select **"GJandAsher ShipTracker (Flutter)"**.
   - Pick **Chrome** or your target Android device.

3. **Terminal Run Option**:
   ```bash
   # Run in Chrome browser
   flutter run -d chrome

   # Run on default connected Android device/emulator
   flutter run
   ```

---

## 🎨 60-30-10 Design Architecture (Color & Spatial Rule)

This project strictly implements the **60-30-10 Rule** across its Flutter & Dart design system (`lib/theme/rule_60_30_10.dart` and `lib/theme/app_theme.dart`):

### 1. Color Architecture Rule (60-30-10)
- **60% Dominant (Base Canvas & Neutral Surfaces)**:
  - *Light Mode*: Slate 50 (`#F8FAFC`) canvas and Slate 100 (`#F1F5F9`) scaffold background.
  - *Dark Mode*: Deep Obsidian (`#090D16`) canvas and Slate 900 (`#0F172A`) scaffold background.
  - *Purpose*: Provides visual tranquility, high text contrast, and eliminates visual fatigue by occupying the foundational canvas.
- **30% Secondary / Structural (Containers & Architecture)**:
  - *Light Mode*: Pure white (`#FFFFFF`) cards, `#E2E8F0` structural outlines, NavigationBar background, input containers, and primary text (`#0F172A`).
  - *Dark Mode*: Slate 800 (`#1E293B`) cards, `#334155` outlines, and light text (`#F8FAFC`).
  - *Purpose*: Defines information hierarchy, card grouping, visual elevation, and reading structure.
- **10% Accent (Focal Points & Critical Actions)**:
  - *Logistics Royal Blue* (`#2563EB` / `#1E3A8A` / `#3B82F6`), *Amber Alert* (`#F59E0B`), and *Emerald Success* (`#10B981`).
  - *Purpose*: Reserved strictly for primary action buttons (e.g. "Log Scanned Parcel", "Generate Batch Handover"), active navigation pills, critical status badges, and focal interactive triggers.

### 2. Spatial & Layout Rule (8pt Grid Hierarchy)
- **60% Macro Canvas & Breathing Room**:
  - `16.0–24.0dp` screen gutters, `20.0–28.0dp` section gaps, and `12.0–16.0dp` card gutters to ensure screens never feel cramped.
- **30% Structural Component Dimensions**:
  - `50.0dp` standard button & input heights, `16.0–20.0dp` card internal padding, `72.0dp` navigation bar height.
- **10% Micro Precision Details**:
  - `4.0–8.0dp` icon-to-label gaps, `10x4dp` badge padding, and strict geometric radius hierarchy (`6dp` micro, `12dp` elements, `16dp` macro cards).

> 💡 **Live Inspector**: Tap the **Palette icon** (`🎨`) in the app bar on any screen to open the interactive **60-30-10 Architecture Inspector** dialog.

---

## 👥 Accounts & Role Architecture

- **Canonical Accounts**:
  - **Nolan Caparros (Owner)**:
    - Username: `nolancaparros`
    - PIN: `1234`
    - Access: Full access to Analytics Dashboard, Staff & Roles Manager, Permission assignment, Deactivate/Reactivate staff, and Refund approvals.
  - **GJ Caparros (Staff)**:
    - Username: `gjcaparros`
    - PIN: `1111`
    - Access: Warehouse operations (Scan & Validate, Dispatch Handover, Returns Logging).

- **Sign Up / Account Creation**:
  - The operational role picker has been **completely removed** from self-registration.
  - All new account creations automatically default to **Staff** accounts.
  - Only **Owner Nolan** has permissions to assign or adjust operational capabilities (scanning, dispatching, returns, shipment editing) via the **Staff & Roles Manager**.

---

## 📦 Project Structure

```
├── .idea/                                 # Android Studio IDE Project Configuration
│   ├── runConfigurations/
│   │   └── main_dart.xml                  # 1-Click Run configuration for Android Studio
│   ├── libraries/                         # Dart & Flutter SDK indexing rules
│   ├── misc.xml                           # JDK & project config
│   ├── modules.xml                        # Module mappings
│   └── vcs.xml                            # Git version control mapping
├── .vscode/                               # VS Code Project Configuration
│   ├── launch.json                        # VS Code Run & Debug configuration
│   ├── settings.json                      # Flutter format & UI guide rules
│   └── extensions.json                    # Dart & Flutter extensions
├── android/                               # Flutter Android Host Runner
│   ├── app/
│   │   ├── build.gradle                   # Flutter Android App build settings
│   │   └── src/main/AndroidManifest.xml   # Android permissions (Camera, Internet, Vibrate)
│   ├── build.gradle                       # Gradle root runner
│   └── settings.gradle                    # Gradle plugin management
├── web/                                   # Flutter Web Host Runner (Chrome testing)
│   ├── index.html                         # Web entrypoint
│   └── manifest.json                      # Web app manifest
├── lib/                                   # Shared Dart / Flutter Codebase
│   ├── main.dart                          # App Entrypoint
│   ├── controllers/
│   │   └── ship_tracker_controller.dart  # Business logic, deduplication, roles & state
│   ├── models/
│   │   └── models.dart                    # Data structures & Enums
│   ├── screens/
│   │   ├── auth_screen.dart               # PIN Auth & Staff Sign-Up (No role selector)
│   │   ├── dashboard_screen.dart          # Owner-exclusive Analytics
│   │   ├── dispatch_screen.dart           # Batch Handover to Courier Riders
│   │   ├── main_scaffold.dart             # App Bar & Tab Navigation
│   │   ├── returns_screen.dart            # RTS & Return Logging with Owner Approval
│   │   ├── rule_60_30_10_dialog.dart      # Live 60-30-10 Rule Inspector
│   │   ├── scan_screen.dart               # Barcode Scanner & Auto-Classifier
│   │   ├── shipments_screen.dart          # Filterable Outbound Parcel Registry
│   │   ├── splash_screen.dart             # Logistics Terminal Welcome
│   │   └── staff_management_dialog.dart   # Owner Staff & Permissions Manager
│   ├── theme/
│   │   ├── app_theme.dart                 # Material 3 Theme (Light/Dark)
│   │   └── rule_60_30_10.dart             # 60-30-10 Color & Spatial System tokens
│   └── utils/
│       └── classification_helper.dart     # Barcode heuristics (SPX, J&T, LZD, etc.)
├── pubspec.yaml                           # Flutter dependencies
├── analysis_options.yaml                  # Dart analysis rules
└── metadata.json                          # AI Studio Platform Metadata
```
