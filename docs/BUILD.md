# Build the project

This pages describes how to download and build the app from the source code. If you are having trouble building the project, please do not hesitate to open a new issue.

## Contents

* [Build using Android Studio](#build-using-android-studio)
* [Build from the command line (Linux)](#build-from-the-command-line)
* [Build from the command line (Windows)](#build-from-the-command-line-windows)

## Build using Android Studio

### Step 1: Install git

The package `git` is required for downloading the source code of the app and submitting changes GitHub. Please see [the git book](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) for further instructions. If you are planning to submit pull requests in the future, it is recommended to [generate and configure your SSH keys](https://help.github.com/en/github/authenticating-to-github/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent).

### Step 2: Download and install Android Studio

Although Android Studio can be downloaded [from their official website](https://developer.android.com/studio/), a much better option is to install it through [JetBrains Toolbox](https://www.jetbrains.com/toolbox-app/). This tool, developed by the same developers of Android Studio, allows you to easily upgrade and downgrade the IDE, or switch between stable, beta and canary versions. After downloading and installing JetBrains Toolbox, simply click the install button near Android Studio to install the newest stable version of IDE. Beta and canary versions have not been tested and may not work correctly.

After installation, launch Android Studio. If this is the first time you launch it, you will need to go through a wizard to setup the IDE. The default options should work fine. The wizard will download all additional components necessary for development, including the emulator, so it may take a while.

### Step 3: Download the source code

To create a complete copy of the source code repository, open the terminal (Linux/macOS) or Git Bash (Windows), navigate to the desired folder, then run:
```bash
git clone https://github.com/iSoron/uhabits.git
```
The repository will be downloaded to the directory `uhabits`.

### Step 4: Open and run the project on Android Studio

1. Launch Android Studio and select "Open an existing Android Studio project".
2. When the IDE asks you for the project location, select `uhabits` and click "Ok".
3. Android Studio will spend some time indexing the project. When this is complete, click the toolbar icon "Sync Project with Gradle File", located near the right corner of the top toolbar.
4. The operation will likely fail several times due to missing Android SDK components. Each time it fails, click the link "Install missing platforms", "Install build tools", etc, and try again.
5. To test the application, create a virtual Android device using the menu "Tools" and "AVD Manager". The default options should work fine, but feel free to customize the device.
6. Click the menu "Run" and "uhabits-android". The application should launch.


## Build from the command line (Linux)

The following instructions were tested on **Ubuntu Linux 18.04 LTS** and may need to be modified for other operating systems.

### Step 1: Install basic packages

To build the application, some basic packages are required. The package `git` is required to download the source code, while `openjdk-8-jdk-headless` is required for compiling Java and Kotlin files.

```bash
sudo apt-get update
sudo apt-get install -y git openjdk-8-jdk-headless
```

**IMPORTANT:** Newer JDK versions have not been tested and may not work correctly.


### Step 2: Install Android SDK tools

The Android SDK tools contains many necessary tools for developing and debugging Android applications. It can be obtained as part of Android Studio, but, for simple command line usage, it can also be downloaded individually.

1. Download the file `sdk-tools-linux-4333796.zip` (or a newer version) from <https://developer.android.com/studio/#downloads>, and extract it somewhere. In this guide, we assume that it was extracted to `/opt/android-sdk/tools`; that is, the script `/opt/android-sdk/tools/bin/sdkmanager` should exist.

2. Append the following lines to `~/.profile`, so that other tools can locate your Android SDK installation. It is necessary to restart your terminal for these changes to take effect.
```bash
export PATH="$PATH:/opt/android-sdk/tools/bin"
export PATH="$PATH:/opt/android-sdk/platform-tools"
export ANDROID_HOME="/opt/android-sdk"
```

3. Accept all Android SDK licenses, by running
```bash
yes | sdkmanager --licenses
```

### Step 3: Download the source code

To create a complete copy of the source code repository, navigate to your home directory and run:
```bash
git clone https://github.com/iSoron/uhabits.git
```
The repository will be downloaded to the directory `uhabits`.

### Step 4: Compile the source code

1. Navigate to the directory `uhabits`
2. Run `./gradlew assembleDebug --stacktrace`

If the compilation is successful, a debug APK will be generated somewhere inside the folder `uhabits-android/build/`. Currently, the full path is `./uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk`, but it may change in the future.

The APK can be installed using the tool `adb`, which should have been automatically installed at `/opt/android-sdk/platform-tools/adb` during compilation of the project. With your phone connected via USB and USB debugging enabled, run:

```bash
adb install -r ./uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk
```

---

## Build from the command line (Windows)

The following instructions were tested on **Windows 11** with **Android Studio** already installed. Android Studio's bundled JDK is used, so no separate Java installation is required.

### Step 1: Configure JAVA_HOME (one-time setup)

Android Studio ships with a JDK. Add `JAVA_HOME` to your PowerShell profile so it is set in every new terminal automatically:

```powershell
Add-Content $PROFILE "`n`$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'"
```

Then apply it to your current session without restarting:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
```

> **Note:** Using `[System.Environment]::SetEnvironmentVariable(...)` writes to the Windows registry but will **not** take effect in VS Code's integrated terminal until VS Code itself is fully restarted. The PowerShell profile approach above is more reliable.

### Step 2: Configure the Android SDK location (one-time setup)

Create a file named `local.properties` in the root of the project with the following content:

```
sdk.dir=C\:\\Users\\<your-username>\\AppData\\Local\\Android\\Sdk
```

Replace `<your-username>` with your Windows username. For example, if your username is `micow`:

```
sdk.dir=C\:\\Users\\micow\\AppData\\Local\\Android\\Sdk
```

### Step 3: Enable USB debugging on your phone (one-time setup)

1. On your Android phone, go to **Settings → About phone** and tap **Build number** 7 times to enable Developer Options.
2. Go to **Settings → Developer options** and turn on **USB debugging**.
3. Connect your phone via USB and tap **Allow** on the "Allow USB debugging?" popup.

### Step 4: Build the APK

Navigate to the project root in PowerShell and run:

```powershell
.\gradlew.bat assembleDebug
```

On success, the APK is generated at:
```
uhabits-android\build\outputs\apk\debug\uhabits-android-debug.apk
```

### Step 5: Install the APK on your phone

With your phone connected via USB:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "uhabits-android\build\outputs\apk\debug\uhabits-android-debug.apk"
```

The `-r` flag reinstalls over any existing version.

### Rebuilding after code changes

After making changes, just repeat Step 4 and Step 5. You can chain them into one command:

```powershell
.\gradlew.bat assembleDebug ; & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "uhabits-android\build\outputs\apk\debug\uhabits-android-debug.apk"
```

### Troubleshooting

| Problem | Fix |
|---|---|
| `JAVA_HOME is not set` | Re-run the Step 1 command and restart your terminal |
| `SDK location not found` | Check that `local.properties` exists in the project root with the correct path |
| `adb.exe: no devices/emulators found` | Check USB cable, switch USB mode to "File Transfer" on your phone, and accept the USB debugging prompt |
| Device not listed by `adb devices` | Install the USB driver for your phone model (e.g. [Samsung USB Driver](https://developer.samsung.com/android-usb-driver.html)) |

