# CityRoam
### *Every step tells a story.*

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Map](https://img.shields.io/badge/Map-OSMDroid-7EBC6F?style=for-the-badge)
![GPS](https://img.shields.io/badge/GPS-Live%20Tracking-FF6B35?style=for-the-badge)

---

## Demo

> *Watch CityRoam come to life — a trail drawn one step at a time.*



https://github.com/user-attachments/assets/6eaddce3-2376-4aa2-a217-384fa8670cad



---

## Background

CityRoam began as a learning project and grew into something more. It started with a single map pin in **Marrakesh** and evolved into a real-time explorer that draws your path behind you, measures how far you have come, and rotates to face wherever you are headed next.

Every feature was earned. Every bug was fought. The orange trail on screen is not just a polyline — it is a record of movement. Of being somewhere.

---

## Features

| Feature | Description |
|---|---|
| Live OSMDroid Map | Open-source map, no Google API key required |
| Real-time GPS Marker | Single marker that moves with you — no duplicates |
| Breadcrumb Trail | An orange polyline drawn behind every step you take |
| Accuracy Circle | A live blue circle showing your real GPS precision in metres |
| Distance Counter | Tracks total distance explored, shown in the marker title |
| Marker Rotation | The pin rotates to face your direction of travel |
| Permission Handling | Graceful runtime permission requests with user-friendly dialogs |
| GPS Provider | Uses `GPS_PROVIDER` for precise, real-world location updates |


## Tech Stack

- **Language:** Java
- **Platform:** Android (API 24+)
- **Map Library:** [OSMDroid](https://github.com/osmdroid/osmdroid)
- **Location:** Android `LocationManager` — `GPS_PROVIDER`
- **Build:** Gradle (Kotlin DSL)

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- Android device or emulator (API 24+)
- Internet connection (for map tile loading)

### Installation

```bash
git clone https://github.com/lailaelamiri/GPS-et-Map.git
cd GPS-et-Map
```

Open the project in **Android Studio**, let Gradle sync, then hit **Run**.

### Permissions

Add these to your `AndroidManifest.xml` if not already present:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## Project Structure

```
CityRoam/
├── app/
│   └── src/main/
│       ├── java/com/example/cityroam/
│       │   └── MainActivity.java       # Core logic — map, GPS, overlays
│       ├── res/
│       │   └── layout/
│       │       └── activity_main.xml   # MapView layout
│       └── AndroidManifest.xml
└── build.gradle.kts
```

---

## How It Works

```
App Starts
    └── Map loads centered on Marrakesh
    └── Permission check
            ├── Granted → startTrackingLocation()
            └── Denied  → request dialog

GPS Update received (onLocationChanged)
    ├── Accuracy circle redraws around new position
    ├── Distance calculated from last known point
    ├── Trail polyline extended with new GeoPoint
    ├── Marker moved + title updated ("243m explored")
    ├── Marker rotated to bearing direction
    └── Camera animates smoothly to new position
```

---

## Origin

This app started with a single `GeoPoint(31.6258, -7.9892)` — the heart of Marrakesh.

It was built, broken, debugged, and rebuilt in the same city it was designed to explore.

---

## License

```
MIT License — free to use, learn from, and build upon.
```

---




