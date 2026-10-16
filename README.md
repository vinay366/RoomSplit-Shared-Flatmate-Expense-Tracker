# RoomSplit 🏠💰

**RoomSplit** is an Android application built with modern Jetpack Compose and Firebase Firestore designed for roommates, flats, and PG shared-living situations. It eliminates the friction of shared expense tracking with instant logging, automatic equal splits, real-time balance calculations, UPI settlement shortcuts, and month-end summaries.

---

## ✨ Features

- **⚡ Fast Logging (<10 Seconds)**:
  - Quick amount input with smart quick-pick chips (+₹50, +₹100, +₹200, +₹500).
  - 5 Fixed essential flat categories:
    - 🛒 **Groceries**
    - ⚡ **WiFi / Electricity / Utilities**
    - 🧹 **Maid / Cook**
    - 🧴 **Cleaning & Household Supplies**
    - 📦 **Other Shared Essentials**
  - Paid-by member selection and optional notes.
- **🔄 Real-Time Shared Feed**:
  - Live synchronized feed powered by Cloud Firestore snapshot listeners.
  - Formatted timestamps, item descriptions, and user badge colors.
  - Delete authorization for entry creator or flat admin.
- **⚖️ Real-Time Balance Matrix**:
  - Automatically calculates equal shares across all room members.
  - Color-coded net balances: green for "Owed to you" (+₹X) and red for "You owe" (-₹X).
  - Clear simplified bilateral debt suggestions ("A owes B ₹X").
- **📲 UPI Deep-Link Settlement**:
  - One-tap settlement triggers device UPI apps (GPay, PhonePe, Paytm, BHIM) with pre-filled recipient VPA, recipient name, note, and exact settlement amount.
  - "Mark as Settled" records an offsetting settlement entry in the shared room ledger.
- **📊 Monthly & Category Summaries**:
  - Month-by-month spending totals and breakdown by category with visual progress bars.
  - Per-roommate contribution charts to see who covered what.
  - Single-tap export summary as formatted text for sharing directly to WhatsApp or messaging groups.
- **🔑 Room Invite Codes**:
  - 6-character alphanumeric room code (e.g. `FLAT4B`).
  - Shareable with one-tap copy and WhatsApp direct share.
  - Easy join flow for new flatmates.
- **🔒 Firebase Authentication & Security**:
  - Google Sign-In with Jetpack Credential Manager.
  - Strict zero-trust Firestore security rules validating room membership and input schemas.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) with StateFlow and Kotlin Coroutines
- **Database & Sync**: Firebase Cloud Firestore with real-time listeners
- **Authentication**: Firebase Auth with Google Identity (`CredentialManager`)
- **UPI Integration**: Native Android `upi://pay` deep-link intents
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala / Ladybug or newer
- Android SDK 35 (compileSdk 35, minSdk 26)
- Java 17 or Java 21 JDK
- A Firebase project with Firestore and Authentication enabled

### Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone <repo-url>
   cd roomsplit
   ```

2. **Firebase Configuration**:
   - Ensure `google-services.json` is present in the `app/` directory.
   - Configure SHA-1 and SHA-256 fingerprints in your Firebase Console for Google Sign-In.

3. **Deploy Firestore Rules**:
   ```bash
   firebase deploy --only firestore:rules
   ```

4. **Build and Run**:
   - Open the project in Android Studio.
   - Sync Gradle.
   - Run on an Android device or emulator with Google Play Services enabled.

---

## 📄 License

Licensed under the [Apache License 2.0](LICENSE).
