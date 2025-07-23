// main index.js

import { NativeModules } from "react-native";

const { ReactNativeBrotherLabelPrint } = NativeModules;

if (!ReactNativeBrotherLabelPrint) {
  console.error("[BrotherLabelPrint] Native module not found!");
  throw new Error("ReactNativeBrotherLabelPrint module is not available");
}

export function discoverNetworkPrinters() {
  return new Promise((resolve, reject) => {
    ReactNativeBrotherLabelPrint.discoverNetworkPrinters()
      .then((printers) => resolve(printers))
      .catch((error) => {
        console.error(
          "[BrotherLabelPrint] discoverNetworkPrinters error:",
          error
        );
        reject(error);
      });
  });
}

export function printImageViaWifi(uri, ipAddress, modelName) {
  if (!uri) {
    console.error("[BrotherLabelPrint] uri missing");
    throw new Error("image uri missing");
  }

  if (!ipAddress) {
    console.error("[BrotherLabelPrint] ipAddress missing");
    throw new Error("ip address missing");
  }

  if (!modelName) {
    console.error("[BrotherLabelPrint] modelName missing");
    throw new Error("model name missing");
  }

  return new Promise((resolve, reject) => {
    ReactNativeBrotherLabelPrint.printImageViaWifi(uri, ipAddress, modelName)
      .then((result) => {
        resolve(result);
      })
      .catch((error) => {
        console.error("[BrotherLabelPrint] printImageViaWifi error:", error);
        reject(error);
      });
  });
}

export function printImageViaBluetooth(uri, modelName) {
  if (!uri) {
    console.error("[BrotherLabelPrint] uri missing");
    throw new Error("image uri missing");
  }

  if (!modelName) {
    console.error("[BrotherLabelPrint] modelName missing");
    throw new Error("modelName missing");
  }

  return new Promise((resolve, reject) => {
    ReactNativeBrotherLabelPrint.printImageViaBluetooth(uri, modelName)
      .then((result) => {
        resolve(result);
      })
      .catch((error) => {
        console.error(
          "[BrotherLabelPrint] printImageViaBluetooth error:",
          error
        );
        reject(error);
      });
  });
}
