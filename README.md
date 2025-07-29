# Ruler-Scale-Compose

[![Android](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-blue)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A Jetpack Compose component that displays a vertical ruler with selectable **cm** and **ft** units,
complete with a needle indicator and haptic feedback.

## Features

- Scrollable vertical scale with major and minor tick marks.
- Switch between **cm** and **ft/inch** units.
- Real-time value updates via callback.
- Supports dark and light themes.
- Haptic feedback on value change.

## Screenshots

### Light Mode

<img src="screenshots/light_mode.png" width="300" />

### Dark Mode

<img src="screenshots/dark_mode.png" width="300" />

**GIF Preview:**

![Demo GIF](screenshots/demo.gif)

## Usage

```kotlin
RulerScaleWithNeedle(
    onValueChange = { value, unit ->
        // Handle selected value here
    }
)
