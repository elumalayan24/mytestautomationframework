# Mobile Automation Setup Guide

## Overview
This framework now supports mobile automation using Appium for both Android and iOS applications.

## Prerequisites

### 1. Appium Server
```bash
# Install Appium via npm
npm install -g appium

# Install Appium Doctor (optional, for checking setup)
npm install -g appium-doctor

# Install Appium Inspector (optional, for debugging)
# Download from: https://github.com/appium/appium-inspector/releases
```

### 2. Android Setup
```bash
# Install Android Studio
# Download from: https://developer.android.com/studio

# Set ANDROID_HOME environment variable
export ANDROID_HOME=/path/to/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools

# Verify setup
adb devices
```

### 3. iOS Setup (macOS only)
```bash
# Install Xcode from App Store
# Install iOS WebKit Debug Proxy
brew install ios-webkit-debug-proxy

# Verify setup
xcrun simctl list devices
```

## Configuration

### 1. Mobile Configuration
Edit `src/test/resources/mobile-config.properties`:

```properties
# Engine selection (selenium, playwright, mobile)
engine=mobile

# Mobile Platform (android, ios)
mobile.platform=android

# Appium Server Configuration
appium.server.url=http://localhost:4723/wd/hub

# Android Configuration
android.device.name=Android Device
android.app.package=com.example.app
android.app.activity=.MainActivity
android.app.path=apps/sample-app.apk

# iOS Configuration
ios.device.name=iPhone
ios.bundle.id=com.example.app
ios.app.path=apps/sample-app.ipa
```

### 2. Running Mobile Tests

#### Start Appium Server
```bash
appium --port 4723
```

#### Run Mobile Tests
```bash
# Run all mobile tests
mvn test -Dcucumber.filter.tags=@mobile

# Run specific mobile feature
mvn test -Dcucumber.features="src/test/resources/features/mobile-sample.feature"
```

## Mobile Steps Available

### Basic Actions
- `Given I launch the mobile app`
- `When I tap on element with id {string}`
- `When I tap on element with xpath {string}`
- `When I tap on element with accessibility id {string}`

### Input Actions
- `When I enter text {string} in field with id {string}`
- `When I enter text {string} in field with xpath {string}`

### Gesture Actions
- `When I swipe {string}` (up, down, left, right)
- `When I scroll down until I see text {string}`

### Verification
- `Then I should see element with id {string}`
- `Then I should see element with text {string}`
- `Then I should see text {string} on the screen`
- `Then the element with id {string} should be enabled/disabled`

### Utilities
- `And I wait for {int} seconds`
- `And I take a mobile screenshot`

## Example Feature File

```gherkin
@mobile
Scenario: Mobile app login test
  Given I launch the mobile app
  When I tap on element with id "login_button"
  And I enter text "testuser" in field with id "username_field"
  And I enter text "password123" in field with id "password_field"
  And I tap on element with id "submit_button"
  Then I should see element with id "welcome_message"
  And I take a mobile screenshot
```

## Device Setup

### Real Android Device
1. Enable Developer Options
2. Enable USB Debugging
3. Connect device via USB
4. Verify with `adb devices`

### Android Emulator
1. Open Android Studio
2. AVD Manager → Create Virtual Device
3. Start emulator
4. Verify with `adb devices`

### Real iOS Device (macOS only)
1. Enable Developer Mode on device
2. Trust developer certificate
3. Connect device via USB
4. Verify with `xcrun simctl list devices`

### iOS Simulator (macOS only)
1. Open Xcode
2. Window → Devices and Simulators
3. Create and start simulator
4. Verify with `xcrun simctl list devices`

## Troubleshooting

### Common Issues
1. **Appium server not running**: Start Appium server with `appium --port 4723`
2. **Device not found**: Check device connection and USB debugging
3. **App not found**: Verify app path/package/bundle ID
4. **Element not found**: Use Appium Inspector to locate elements

### Debug Commands
```bash
# Check Appium server status
curl http://localhost:4723/status

# Check connected devices
adb devices
xcrun simctl list devices

# Appium doctor (check setup)
appium-doctor --android
appium-doctor --ios
```

## Best Practices

1. **Use descriptive element locators**: Prefer accessibility IDs over XPath
2. **Add explicit waits**: Use WebDriverWait for element synchronization
3. **Take screenshots**: Use automatic and manual screenshots for debugging
4. **Handle different screen sizes**: Use relative positioning for gestures
5. **Clean up after tests**: Ensure proper driver cleanup

## Integration with Existing Framework

The mobile automation integrates seamlessly with:
- **ExtentReports**: Mobile screenshots are automatically included
- **Cucumber Reports**: Mobile test results and screenshots
- **Hooks**: Automatic mobile driver cleanup
- **Configuration**: Centralized configuration management

## Next Steps

1. Set up Appium server and mobile devices/emulators
2. Configure mobile properties for your application
3. Create mobile feature files for your app
4. Run your first mobile test
5. Extend with custom mobile steps as needed
