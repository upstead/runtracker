Step 1: Install required tools on Fedora

Install Java 17 and basic utilities.
Run:
sudo dnf install -y java-17-openjdk java-17-openjdk-devel git unzip zip wget

Verify Java.
Run:
java -version
javac -version

Install Android Studio (recommended even if you use VSCode/IntelliJ CE for editing).
Why: Android SDK, emulator, adb, and signing/build tools are easiest to manage there.
Options:

JetBrains Toolbox: install Android Studio from Toolbox
Manual tarball from developer.android.com/studio
Flatpak (community package): flatpak install flathub com.google.AndroidStudio
Open Android Studio once and install:

Android SDK Platform (API 35)
Android SDK Build-Tools
Android SDK Platform-Tools
Android Emulator
Command-line Tools
Set environment variables in your shell profile (for bash or zsh). Example:
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator

Reload shell and verify:
source ~/.bashrc
adb version
sdkmanager --version

Step 2: Prepare the project for local builds

Go to your project folder.
cd /home/sen/workspace/upstead/github/runtracker

If Gradle wrapper is missing, generate it once.
gradle wrapper --gradle-version 8.10.2

Verify wrapper exists:
ls
You should see gradlew and gradlew.bat.

Step 3: Check the app in system first (as requested)

Option A: Emulator check (recommended first)

Accept SDK licenses:
sdkmanager --licenses

Install a system image (example: Android 15 / API 35 with Google APIs x86_64):
sdkmanager "system-images;android-35;google_apis;x86_64"

Create an emulator device:
avdmanager create avd -n runtracker_api35 -k "system-images;android-35;google_apis;x86_64"

Start emulator:
emulator -avd runtracker_api35

In another terminal, confirm device is visible:
adb devices

Build and install debug app to emulator:
./gradlew installDebug

Launch app manually if needed:
adb shell am start -n com.upstead.runtracker/.MainActivity

Option B: Real Android phone check

Enable Developer options + USB debugging on phone.
Connect USB and verify:
adb devices
Install debug build:
./gradlew installDebug
Step 4: Build installable files

A. Debug APK (quick internal install)

Build:
./gradlew assembleDebug
Output:
app/build/outputs/apk/debug/app-debug.apk
Install:
adb install -r app/build/outputs/apk/debug/app-debug.apk
B. Release APK (signed, share/install manually)

Create signing key (one-time):
keytool -genkeypair -v -keystore runtracker-release.jks -alias runtracker -keyalg RSA -keysize 2048 -validity 10000

The app already reads release signing values from local.properties or environment variables.
Set all 4 keys below in local.properties:

RELEASE_STORE_FILE=/absolute/path/to/runtracker-release.jks
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=runtracker
RELEASE_KEY_PASSWORD=your_key_password

Alternative: export the same names as environment variables.

Build release:
./gradlew assembleRelease

Output:
Signed when signing values are set:
app/build/outputs/apk/release/app-release.apk

Unsigned when signing values are missing:
app/build/outputs/apk/release/app-release-unsigned.apk

If unsigned in your setup, sign manually:
apksigner sign --ks runtracker-release.jks --out app-release-signed.apk app/build/outputs/apk/release/app-release-unsigned.apk

Verify signature:
apksigner verify --verbose app-release-signed.apk

C. Android App Bundle (for Play Store)

Build:
./gradlew bundleRelease
Output:
app/build/outputs/bundle/release/app-release.aab

Note: The AAB must also be signed for Play upload, so set the same 4 release keys first.
Step 5: Install release APK on device

Via adb:
adb install -r app-release-signed.apk

Or copy APK to phone and open it.
You may need to allow Install unknown apps for the file manager/browser.

Step 6: Recommended IDE workflow with your setup

Use VSCode or IntelliJ CE for editing.
Use Android Studio for:
SDK/AVD management
Emulator run/debug
Signing wizard (if preferred)
Use terminal for reproducible commands:
./gradlew installDebug
./gradlew assembleRelease
./gradlew bundleRelease
Common Fedora issues and fixes

Emulator very slow or not starting:

Ensure virtualization enabled in BIOS
Install KVM stack:
sudo dnf install -y qemu-kvm libvirt virt-install
Add user to kvm group if needed:
sudo usermod -aG kvm $USER
Then log out and back in
adb device not detected:

Try:
adb kill-server
adb start-server
Reconnect cable, accept RSA prompt on phone
Java mismatch errors:

Ensure Java 17 is active:
sudo alternatives --config java
If you want, I can give you a copy-paste release signing block next (with placeholders) so you can produce signed APK and AAB in one pass.