# reGTA Android: Custom APK for GTA III, VC, and SA
This project allows you to build and modify a custom APK for the Android versions of Grand Theft Auto III, Vice City, and San Andreas (GTA III, VC, and SA). The project involves the renaming and replacement of core game libraries, as well as the addition of a custom gesture-based cheats menu and memory hacks. Also works with others port by <b>War Drum Studios.</b>

# Features
Modded APK for GTA III, VC, and SA.
Renamed and replaced game libraries (libGTAVC.so, libGame.so, libGTASA.so) with a new, unified library (libREGTA.so).
Custom gesture detection (swipe-down from top of the screen) to trigger the cheats menu.
A fully customizable cheat system that can be expanded with custom cheats.
Memory hacks to enable various cheats or enhancements within the game.

# Installation & Build Instructions
Open the project in Android Studio.
Ensure you have the necessary SDK and NDK for building Android applications.
Modify Game Libraries:

Replace the original game libraries (libGTAVC.so, libGame.so, libGTASA.so) with the modified libreGTA.so.
Place libreGTA.so inside the jniLibs/libs/arm... directory.

# Customize Cheat System:
Keyboard Dispatch Event: Detects keypresses and maps them to cheat strings, allowing cheat codes to be entered via keyboard or custom inputs.
Cheat Strings: Trigger specific cheats (e.g., health, weapons, money) using mapped keys.
Expandability: Easily add or modify key bindings and cheats in the app.

# Notes
This is an unofficial mod for personal use.
Make sure to have backups of your original game data.
This project is open-source, so feel free to contribute and extend the cheat system or add new features.
