# PerfumeDex

A native Kotlin / Jetpack Compose / Material 3 Android app. Android 8.0+ (API 26). The catalog, copy, and illustrated scent cards are original fictional samples inspired by Filipino notes. No shops, sellers, partnerships, or external artwork.

## Personal experience

Discover searches fragrance name, house, and notes. Open a profile or log directly from the shelf. Choose one of four occasions and tester/decant/bottle, optionally add a 280-character note, then save. First wear unlocks a card and removes the fragrance from the wishlist. My Dex contains cards, wishlist, and dated wear history. Add missing fragrances to your personal catalog. History can make a recommendation private again.

Personal data is saved in app-private SharedPreferences as JSON. No account, network permission, analytics, or automatic cloud backup. Data survives process restarts; uninstalling or clearing app data removes it.

## Community status

This build is explicitly a **local demo**, with no authentication or shared backend. Original sample recommendations have zero fabricated votes. Local recommendations and vouches persist on one device. A wear of the fragrance is required before a demo vouch; one set entry per fragrance/occasion is shared across all matching posts and can be undone. These are not real signed-in votes and are never displayed as live rankings. Discover deliberately has no occasion rankings until real votes exist.

Multi-user voting has **not** been verified. A production backend must authenticate users, store synchronized wear records and recommendations, enforce a unique `(user_id, fragrance_id, occasion)` vouch constraint, check ownership of a prior wear server-side, and authorize undo to the voter only. Rankings must aggregate distinct real users by fragrance/occasion, exclude fixtures, and handle private/deleted posts without tying a vouch to a post. Client checks alone are insufficient. No deployed backend or credentials were present in the empty workspace.

## Build

Use Java 17 and Android SDK platform 35. Set `ANDROID_HOME` or `sdk.dir` in local.properties, then run:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk` after a successful build.

Unit tests cover wear eligibility across tester/decant/bottle, untried fragrances, shared vouch identity, occasion isolation, and undo. Manual device checks: search a note; save a scent; restart; log a wear; find its unlocked card/history; add a missing fragrance; publish locally; attempt a vouch before and after a wear; verify matching posts share the same vote; undo; restart again. Device UI verification is separate from compilation and unit tests.

Deferred: barcode scanning, photo identification, advanced note matching, comparison, real authentication/posting/voting, cross-device sync.

## Verified build — 2026-11-20

Clean `assembleDebug testDebugUnitTest lintDebug` completed successfully with Java 17 / Gradle 8.13 / SDK 35. All 3 unit tests passed. Lint reports 0 errors and 4 warnings (two dependency updates, a legacy backup configuration suggestion, and a Kotlin convenience-extension suggestion). APK signature verification passed. APK manifest inspection confirms package `ph.perfumedex`, launcher `MainActivity`, minimum SDK 26, and target SDK 35. No Android device was connected, so installation and interactive device testing were not performed. No multi-user voting verification was possible without a shared backend.
