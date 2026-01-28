# CardSpendTracker

A modern Android application for tracking credit card spending and managing spending limits. Built with Jetpack Compose and Material Design 3, CardSpendTracker helps you stay on top of your card usage with visual progress indicators and detailed transaction tracking.

## Features

- **Multi-Card Management**: Add and manage multiple credit cards, each with customizable spending limits
- **Transaction Tracking**: Record and track individual transactions for each card with amounts and descriptions
- **Visual Progress Indicators**: See your spending progress at a glance with color-coded progress bars that change based on usage percentage
- **Spending Periods**: Set end dates for spending periods and track days remaining until expiration
- **Real-time Calculations**: Automatically calculates total spent, remaining budget, and spending progress for each card
- **Clean Material Design**: Modern UI built with Jetpack Compose and Material Design 3

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room Database for local data persistence
- **Navigation**: Jetpack Navigation Compose
- **Material Design**: Material 3

## Requirements

- Android SDK 26 (Android 8.0) or higher
- Target SDK 35

## Building the Project

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run the app

## Project Structure

```
app/src/main/java/dev/ktown/cardspendtracker/
├── data/              # Data models and database
│   ├── Card.kt       # Card entity
│   ├── Transaction.kt # Transaction entity
│   ├── AppDatabase.kt # Room database
│   └── CardRepository.kt # Data repository
├── ui/
│   ├── screens/       # Compose screens
│   ├── navigation/    # Navigation graph
│   ├── theme/         # Material theme
│   └── viewmodel/     # ViewModels
└── MainActivity.kt    # Main entry point
```

## License

[Add your license here]