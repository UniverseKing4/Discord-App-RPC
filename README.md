<br>

<div align="center">
<img src="https://img.shields.io/badge/Minimum%20SDK-27-%23?&style=flat-square&color=5b5ef7">
<a href="https://github.com/UniverseKing4/Discord-App-RPC/releases/latest">
<img alt="Release" src="https://img.shields.io/github/v/release/UniverseKing4/Discord-App-RPC?&style=flat-square&color=5b5ef7&display_name=release">
</a>
<img src="https://img.shields.io/badge/kotlin-5b5ef7.svg?logo=kotlin&logoColor=white&style=flat-square">
<img src="https://img.shields.io/badge/Android_Studio-5b5ef7?logo=android-studio&logoColor=white&style=flat-square">
</div>

<div align="center">
<h1>Discord App RPC</h1>
<h4>The absolute peak, ultra-optimized, and highly efficient Discord Rich Presence manager for Android. Written purely in Kotlin.</h4>
<p>
<img src="https://user-images.githubusercontent.com/68665948/207303492-c537af75-0d63-49e9-91c5-97114d974883.png" width=60%/>
</p>
</div>

## 🌟 About The App
**Discord App RPC** is the definitive, perfectly streamlined Android application that connects flawlessly to the Discord Gateway using your user token. It completely automates the process of displaying your Android activity as an elegant Rich Presence. 

Whether you are enjoying music on your favorite media player or interacting with apps on your phone, this manager silently operates in the background to update your Discord status instantly—showcasing exactly what you are doing, complete with high-resolution app icons, beautiful album cover art, rich song metadata, and live precision timestamps!

We have completely stripped away all unnecessary, bloated, and experimental logic from previous iterations to bring you the absolute pinnacle of performance: a hyper-optimized, perfectly lag-free, battery-saving experience that works brilliantly.

## ✨ Key Features
- **🎵 Dynamic Media Detection**: Automatically detects and fetches your currently playing song, artist, and album, seamlessly displaying the high-res album cover art right on your Discord profile.
- **📱 Foreground App Detection**: Elegantly showcases the currently running foreground app (e.g., YouTube, Spotify, or any custom app) along with its sleek launcher icon.
- **⏱️ Precision Live Timestamps**: Accurately counts down/up your current media timeline. Intelligently hides timestamps when media is paused for an ultra-clean profile aesthetic.
- **⚡ Absolute Peak Optimization**: The background service has been entirely rewritten and perfected with advanced coroutine lifecycle management. Features a highly efficient in-memory JSON cache, smart debouncing, and strict garbage collection to guarantee **0% lag**, **no memory leaks**, and **zero battery drain**.
- **🎨 Material You Design**: A beautiful, pristine Android UI that perfectly adapts and blends with your system theme.
- **🔒 Direct WebSocket Integration**: Connects flawlessly to the Discord Gateway, featuring instant, perfectly stable auto-reconnect capabilities.

## 🛠️ System Requirements
- **OS**: Android 8.1 through 14+ 
- **RAM**: 2GB minimum (Achieves an incredibly optimized memory footprint!)

## 🚀 Installation & Setup

1. **Download the latest APK** straight from our [Releases](https://github.com/UniverseKing4/Discord-App-RPC/releases/latest) page.
2. **Install the app** securely on your Android device.
3. **Grant Permissions**: The app requires `Usage Access` (to reliably detect the foreground app) and `Notification Access` (to smoothly read currently playing media).
4. **Enter your Discord Token**: You will need your Discord user token to allow the app to securely connect to the Gateway on your behalf. *(Note: Using a user token for self-RPC is widely used but done at your own risk).*
5. **Start the Service!** Sit back and enjoy your perfectly automated Rich Presence.

## 🏗️ Building From Source
For developers who want to compile this masterfully optimized app locally:

> **Prerequisites:**
- Android Studio Ladybug (or the absolute latest version)
- Familiarity with Gradle, Kotlin, and Jetpack Compose

```console
# Clone the pristine repository
git clone https://github.com/UniverseKing4/Discord-App-RPC.git

# Open the project in Android Studio
# Sync Gradle and click Build > Make Project
```

## ⚖️ Disclaimer & License
> **Warning**
> This app uses the Discord Gateway connection with a user token. Use this at your own risk. Custom rich presence has been widely used by the community for years with no known account issues, but we are not responsible for any problems that may arise with your account.

**Discord App RPC** is an open-source project provided under the [GNU GPL 3.0 Open Source License](https://github.com/UniverseKing4/Discord-App-RPC/blob/main/LICENSE). You may use, reference, and modify the source code freely, but you may not distribute the modified and derived code as closed-source commercial software.
