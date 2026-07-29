# Workday Counter

Android app (Kotlin + Jetpack Compose, Material 3) that counts the working days left
between two dates, skipping Saturdays, Sundays and Indian public holidays, with an
optional leave adjustment — plus a to-do list.

**Compatibility:** `minSdk 26` → runs on **Android 8.0 Oreo through Android 16** (~9 generations,
about 99% of active devices). `targetSdk 34`.

---

## Screens

**1. Counter**
- Start date and end date pickers. Saved to device storage — they stay put until you change them.
- Big bold number in the middle of the screen.
- `Adjust leaves` switch. Off → counter = working days. On → a "Number of leaves" field appears and counter = working days − leaves (floored at 0).
- `Exclude Indian holidays` switch.
- Breakdown chips: total days, weekends, holidays, working days, leaves.
- Counts from **today** (or the start date, whichever is later), so "remaining" actually decreases each day.

**2. To-Do**
- Add tasks, tick them off, delete individually, or clear all completed. Persisted across restarts.

**3. Holidays**
- All built-in Indian holidays for 2026–2027 with tick boxes — untick anything your office doesn't observe.
- Add your own dates with a name.
- Republic Day, Independence Day and Gandhi Jayanti are fixed; the lunar/religious dates are best-estimate defaults, so correct them here if your company calendar differs.

## Animations
- **Celebration pop** — confetti burst every time the app is brought to the foreground (`ON_START`).
- **Counter roll** — the number starts on a random value and rolls to the final figure in exactly **300 ms** with an ease-out curve.

---

## Getting an APK

### Option A — build it in the cloud (no Android Studio needed)
1. Create a new repository on github.com.
2. Upload this whole folder to it (drag and drop works: *Add file → Upload files*).
3. Open the **Actions** tab. The `Build APK` workflow runs automatically on push.
4. When it finishes (~3–4 min), open the run and download the **WorkdayCounter-debug-apk** artifact.
5. Unzip it, copy `app-debug.apk` to your phone, and install (allow "install from unknown sources").

### Option B — Android Studio
1. Open Android Studio → *Open* → select this folder. Let it sync (it downloads Gradle and the SDK bits automatically).
2. `Build → Build Bundle(s) / APK(s) → Build APK(s)`.
3. APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

### Option C — command line
```bash
gradle wrapper          # generates gradlew (only needed once)
./gradlew assembleDebug
```
Requires JDK 17 and the Android SDK, with `sdk.dir` set in `local.properties`
(Android Studio writes this for you).

> The Gradle wrapper JAR is not included (binary files can't be shipped in this bundle).
> Android Studio and the GitHub Actions workflow both handle this for you.

---

## Project layout

```
app/src/main/java/com/bk/workdaycounter/
├── MainActivity.kt          bottom nav, lifecycle trigger for the celebration
├── data/
│   ├── AppStore.kt          SharedPreferences persistence (dates, leaves, todos, holidays)
│   ├── Holidays.kt          built-in Indian holiday list
│   └── WorkdayCalc.kt       date maths
└── ui/
    ├── CounterScreen.kt     page 1
    ├── TodoScreen.kt        page 2
    ├── HolidaysScreen.kt    page 3
    ├── RollingNumber.kt     0.3 s roll animation
    ├── Confetti.kt          celebration burst
    └── theme/Theme.kt       follows system light/dark; Material You on Android 12+
```

No third-party libraries beyond AndroidX/Compose.
