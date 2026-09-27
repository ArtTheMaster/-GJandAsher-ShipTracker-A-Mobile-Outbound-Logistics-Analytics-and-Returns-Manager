# GJandAsher ShipTracker (Flutter / Dart)

Outbound Logistics, Analytics, and Returns Manager built with **Flutter & Dart** for Visual Studio Code.

## 🚀 How to Run in Visual Studio Code

1. **Open the Project in VS Code**:
   - Open Visual Studio Code.
   - Choose **File > Open Folder...** and select this directory.
   - If not already installed, install the official **Flutter** and **Dart** extensions from the VS Code Marketplace.

2. **Install Dependencies**:
   Open a terminal in VS Code (`Ctrl + \`` or `Cmd + \``) and run:
   ```bash
   flutter pub get
   ```

3. **Run / Debug the Application**:
   - Press `F5` or go to **Run and Debug** (`Ctrl + Shift + D` / `Cmd + Shift + D`) and select **"GJandAsher ShipTracker (Flutter)"**.
   - Choose your target device (Chrome web, macOS/Windows desktop, or connected Android/iOS device/emulator).
   - Alternatively, execute from the terminal:
     ```bash
     flutter run
     ```
     or run in Chrome browser:
     ```bash
     flutter run -d chrome
     ```

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

- **Sign Up / Account Creation (Per User Request)**:
  - The operational role picker has been **completely removed** from self-registration.
  - All new account creations automatically default to **Staff** accounts.
  - Only **Owner Nolan** has permissions to assign or adjust operational capabilities (scanning, dispatching, returns, shipment editing) via the **Staff & Roles Manager**.

---

## 📦 Project Structure

```
├── lib/
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
│   │   ├── scan_screen.dart               # Barcode Scanner & Auto-Classifier
│   │   ├── shipments_screen.dart          # Filterable Outbound Parcel Registry
│   │   ├── splash_screen.dart             # Logistics Terminal Welcome
│   │   └── staff_management_dialog.dart   # Owner Staff & Permissions Manager
│   ├── theme/
│   │   └── app_theme.dart                 # Material 3 Theme (Light/Dark)
│   └── utils/
│       └── classification_helper.dart     # Barcode heuristics (SPX, J&T, LZD, etc.)
├── pubspec.yaml                           # Flutter dependencies
├── analysis_options.yaml                  # Dart analysis rules
└── .vscode/
    └── launch.json                        # VS Code Run/Debug Configuration
```
