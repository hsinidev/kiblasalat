# kiblasalat

> Modern, high-performance Android application for kiblasalat.

**Android Application** built with Kotlin and modern Android development standards.

---

## 📖 How It Works

This application is built following **Clean Architecture**, **MVVM / MVI pattern**, and **Offline-First** principles.

### Architecture & System Modules
- **Presentation Layer**: Built with Jetpack Compose (Material Design 3) and MVI/MVVM architecture for reactive UI state management.
- **Domain Layer**: Clean Architecture use cases managing core business logic and state transitions.
- **Data Layer**: Room Database & DataStore providing fast offline persistence and continuous StateFlow updates.
- **Background Processing**: WorkManager managing background updates efficiently.

### Required Android Permissions
- Standard Android Internet & Storage permissions

---

## 📱 How to Use

### 1. Launch & Permissions
Open the application and grant required permissions (e.g. notifications or storage) when prompted.
### 2. Main Dashboard
Navigate between main features using the primary navigation bar.
### 3. Record & Manage
Use the primary action buttons (+) to create new logs, start tracking sessions, or view analytics.
### 4. Customization & Settings
Access settings to toggle theme options, manage data backup, or adjust app preferences.

---

## 🚀 Key Features

- **Core Functionality**: Streamlined tools and workflows for kiblasalat.
- **Modern UI**: Clean Material Design 3 interface with dark mode support.
- **Offline First**: Fast local storage with Room database and DataStore.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: Clean Architecture + MVVM / MVI
- **Local Storage**: Room Database & DataStore
- **Async Operations**: Kotlin Coroutines & StateFlow
- **Build System**: Gradle Kotlin DSL
- **Min SDK**: 26 | **Target SDK**: 34

---

## 💻 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17+
- Android SDK 34+

### Building & Running
1. Clone the repository:
   ```bash
   git clone https://github.com/hsinidev/kiblasalat.git
   cd kiblasalat
   ```
2. Open the project in Android Studio.
3. Sync Gradle dependencies and run on an Android device or emulator.

---

## 📬 Contact & Support

Created and maintained by **Hsini**.

- **Website**: [hsini.dev](https://hsini.dev)
- **Email**: [contact@hsini.dev](mailto:contact@hsini.dev)
- **GitHub**: [@hsinidev](https://github.com/hsinidev)

---

© 2026 [hsini.dev](https://hsini.dev). All rights reserved.
