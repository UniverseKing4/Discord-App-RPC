<br>

<div align="center">
<img src="https://img.shields.io/badge/Minimum%20SDK-27-%23?&style=flat-square&color=5b5ef7">
<img src="https://img.shields.io/github/downloads/UniverseKing4/Discord-App-RPC/total?&style=flat-square&color=5b5ef7">
<a href="https://github.com/UniverseKing4/Discord-App-RPC/releases/latest">
<img alt="Release" src="https://img.shields.io/github/v/release/UniverseKing4/Discord-App-RPC?&style=flat-square&color=5b5ef7&display_name=release">
</a>
<img src="https://img.shields.io/github/actions/workflow/status/UniverseKing4/Discord-App-RPC/build.yml?branch=main?&style=flat-square&color=5b5ef7">
<img src="https://img.shields.io/badge/kotlin-5b5ef7.svg?logo=kotlin&logoColor=white&style=flat-square">
<img src="https://img.shields.io/badge/Android_Studio-5b5ef7?logo=android-studio&logoColor=white&style=flat-square">
</div>

<div align="center">
<h1>Discord App RPC</h1>
<h4>An ultra-optimized, highly efficient Discord Rich Presence manager for Android, written in Kotlin.</h4>
</div>

## 🌟 About The App
**Discord App RPC** is a streamlined Android application that connects directly to the Discord Gateway using your user token to automatically display your Android activity as Rich Presence. 

Whether you are listening to music on your favorite media player or playing a game on your phone, this app silently runs in the background and updates your Discord status—showing exactly what you are doing, complete with app icons, album cover art, song metadata, and live timestamps!

We have stripped away all the unnecessary experimental bloat to bring you a hyper-optimized, lag-free, battery-saving experience.

## ✨ Key Features
- **🎵 Dynamic Media Detection**: Automatically fetches the currently playing song, artist, album, and displays the high-res album cover art right on your Discord profile.
- **📱 Current App Detection**: Showcases the currently running foreground app (e.g., YouTube, Spotify, Games) along with its launcher icon.
- **⏱️ Live Timestamps**: Accurately counts down/up the current media timeline. Automatically hides when media is paused for a cleaner profile look.
- **⚡ Absolute Peak Optimization**: The background service is entirely rebuilt with advanced coroutine lifecycle management. It uses an in-memory JSON cache, smart debouncing, and strict garbage collection to ensure **0% lag**, **no memory leaks**, and **zero battery drain**.
- **🎨 Material You**: Beautiful Android UI that seamlessly blends with your system theme.
- **🔒 Direct WebSocket**: Connects flawlessly to the Discord Gateway with instant auto-reconnect capabilities if the connection drops.

## 🛠️ System Requirements
- **OS**: Android 8.1 through 14+ 
- **RAM**: 2GB minimum (Highly optimized memory footprint!)

## 🚀 Installation & Setup

1. **Download the latest APK** from our [Releases](https://github.com/UniverseKing4/Discord-App-RPC/releases/latest) page.
2. **Install the app** on your Android device.
3. **Grant Permissions**: The app requires `Usage Access` (to detect the foreground app) and `Notification Access` (to read the currently playing media).
4. **Enter your Discord Token**: You will need your Discord user token to allow the app to connect to the Gateway on your behalf. *(Note: Using a user token for self-RPC is generally safe, but done at your own risk).*
5. **Start the Service!** Enjoy your automated Rich Presence.

## 🏗️ Building From Source
For developers who want to build the app locally:

> **Prerequisites:**
- Android Studio Ladybug (or latest)
- Familiarity with Gradle, Kotlin, and Jetpack Compose

```console
# Clone the repository
git clone https://github.com/UniverseKing4/Discord-App-RPC.git

# Open the project in Android Studio
# Sync Gradle and click Build > Make Project
```

## ⚖️ Disclaimer & License
> **Warning**
> This app uses the Discord Gateway connection with a user token. Use this at your own risk. Custom rich presence has been widely used by the community for years with no known account issues, but we are not responsible for any problems that may arise with your account.

**Discord App RPC** is an open-source project under the [GNU GPL 3.0 Open Source License](https://github.com/UniverseKing4/Discord-App-RPC/blob/main/LICENSE). You may use, reference, and modify the source code freely, but you may not distribute the modified and derived code as closed-source commercial software.
