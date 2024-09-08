# Motiv8AI App

## Description

This Android application, built with Kotlin and Jetpack Compose, provides a user-friendly interface for accessing device information, media metadata, and contact details. The app features a main screen with three tabs:

* **Device Data:** Displays relevant information about the user's device, such as model, manufacturer, OS version, and screen resolution.
* **Media:** Presents a list of images and videos stored on the device, along with their associated metadata (e.g., file size, dimensions, duration).
* **Contacts:** Shows a list of contacts from the user's address book, including names and phone numbers.

The project utilizes a separate module named `motiv8sdk` to handle data fetching operations. This module follows Android development best practices and ensures efficient and reliable data retrieval.

## Features

* Device information retrieval
* Media metadata extraction (images and videos)
* Contact list access
* Tabbed interface for easy navigation
* Modern Android development practices(Kotlin, Compose)
* Modular architecture (`motiv8sdk` module)

## Getting Started

To build and run this application, follow these steps:

### Prerequisites

* Android Studio (latest stable version recommended)
* Android SDK (minimum SDK version: 26)
* Kotlin plugin enabled in Android Studio

### Installation

1. Clone the repository: `git clone [<repository_url>](https://github.com/itzikpich/Motiv8App.git)`
2. Open the project in Android Studio.
3. Build the project: **Build > Make Project**
4. Run the application on an emulator or physical device: **Run > Run 'app'**

## Usage

1. Launch the application.
2. Navigate between the tabs ("Device Data", "Media", "Contacts") to access different information.
3. Explore the data presented in each tab.
