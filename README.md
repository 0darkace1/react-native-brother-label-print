# react-native-brother-label-print

A React Native module for printing labels on Brother printers via WiFi and Bluetooth. Supports QL series and other Brother label printers.

## Installation

```bash
npm install react-native-brother-label-print
```

or using yarn:

```bash
yarn add react-native-brother-label-print
```

### iOS Setup

Run pod install:

```bash
cd ios && pod install
```

### Required Permissions

#### iOS

Add the following permissions to your `Info.plist`:

```xml
<!-- Bluetooth permissions -->
<key>NSBluetoothPeripheralUsageDescription</key>
<string>Find paired Brother printers</string>

<key>NSBluetoothAlwaysUsageDescription</key>
<string>Find paired Brother printers</string>

<!-- WiFi permissions -->
<key>NSLocalNetworkUsageDescription</key>
<string>Find Brother printers installed in the local network</string>
<key>NSBonjourServices</key>
<array>
    <string>_pdl-datastream._tcp</string>
    <string>_printer._tcp</string>
    <string>_ipp._tcp</string>
</array>
```

#### Android

Add the following permissions to your `AndroidManifest.xml`:

```xml
<!-- Bluetooth permissions -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />

<!-- WiFi permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Usage

### Print via WiFi

```javascript
import { printImageViaWifi } from "react-native-brother-label-print";

try {
  const ipAddress = "192.168.1.100"; // Your printer's IP address
  const modelName = "QL-820NWB"; // Your printer's model name
  const imageUri = "file:///path/to/your/image.png"; // Local file URI

  await printImageViaWifi(imageUri, ipAddress, modelName);
  console.log("Print successful");
} catch (error) {
  console.error("Print failed:", error);
}
```

### Print via Bluetooth

```javascript
import { printImageViaBluetooth } from "react-native-brother-label-print";

try {
  const modelName = "QL-820NWB"; // Your printer's model name
  const imageUri = "file:///path/to/your/image.png"; // Local file URI

  await printImageViaBluetooth(imageUri, modelName);
  console.log("Print successful");
} catch (error) {
  console.error("Print failed:", error);
}
```

## Supported Printer Models

This module supports the following Brother printer models:

- **QL Series**: QL-820NWB, QL-820NWBc, QL-1110NWB, QL-1110NWBc
- **PJ Series**: PJ-763MFi, PJ-862, PJ-863, PJ-883
- **MW Series**: MW-145MFi, MW-260MFi
- **RJ Series**: RJ-2035B, RJ-2050, RJ-2150, RJ-3035B, RJ-3050Ai, RJ-3150Ai, RJ-3230B, RJ-3250WB, RJ-4030Ai, RJ-4230B, RJ-4250WB
- **TD Series**: TD-2125NWB, TD-2135NWB, TD-4550DNWB
- **PT Series**: PT-P910BT

## Notes

- This module is a wrapper around the official Brother Print SDK
- For more information about the Brother SDK, visit [Brother's Developer Program](https://developerprogram.brother-usa.com/sdk-download)
- Based on [react-native-brother-print](https://github.com/dalisalvador/react-native-brother-print)

## License

MIT
