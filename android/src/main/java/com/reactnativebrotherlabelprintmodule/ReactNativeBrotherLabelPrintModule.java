package com.reactnativebrotherlabelprintmodule;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import android.util.Log;
import android.net.Uri;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.core.content.ContextCompat;

// Brother SDK imports
import com.brother.sdk.lmprinter.Channel;
import com.brother.sdk.lmprinter.OpenChannelError;
import com.brother.sdk.lmprinter.PrinterDriver;
import com.brother.sdk.lmprinter.PrinterDriverGenerateResult;
import com.brother.sdk.lmprinter.PrinterDriverGenerator;
import com.brother.sdk.lmprinter.PrintError;
import com.brother.sdk.lmprinter.setting.PrintImageSettings;
import com.brother.sdk.lmprinter.setting.QLPrintSettings;
import com.brother.sdk.lmprinter.PrinterModel;
import com.brother.sdk.lmprinter.PrinterSearcher;
import com.brother.sdk.lmprinter.PrinterSearchResult;
import com.brother.sdk.lmprinter.PrinterSearchError;
import com.brother.sdk.lmprinter.GetStatusResult;
import com.brother.sdk.lmprinter.GetStatusError;
import com.brother.sdk.lmprinter.MediaInfo;
import com.brother.sdk.lmprinter.PrinterStatus;

public class ReactNativeBrotherLabelPrintModule extends ReactContextBaseJavaModule {
    private static final String TAG = "BrotherLabelPrint";
    private final ReactApplicationContext reactContext;
    private HandlerThread printerThread;
    private Handler printerHandler;

    ReactNativeBrotherLabelPrintModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        
        printerThread = new HandlerThread("PrinterThread");
        printerThread.start();
        printerHandler = new Handler(printerThread.getLooper());
    }

    @Override
    public void onCatalystInstanceDestroy() {
        if (printerThread != null) {
            printerThread.quitSafely();
            printerThread = null;
            printerHandler = null;
        }
        super.onCatalystInstanceDestroy();
    }

    @Override
    public String getName() {
        return "ReactNativeBrotherLabelPrint";
    }

    private boolean checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(reactContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(reactContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private QLPrintSettings getPrinterSettings(String modelName, PrinterDriver printerDriver) {
        try {
            String cleanModelName = modelName.replace("Brother ", "").trim();
            PrinterModel printerModel;
            
            switch (cleanModelName) {
                case "QL-1100":
                    printerModel = PrinterModel.QL_1100;
                    break;
                case "QL-1110NWB":
                    printerModel = PrinterModel.QL_1110NWB;
                    break;
                case "QL-580N":
                    printerModel = PrinterModel.QL_580N;
                    break;
                case "QL-710W":
                    printerModel = PrinterModel.QL_710W;
                    break;
                case "QL-720NW":
                    printerModel = PrinterModel.QL_720NW;
                    break;
                case "QL-800":
                    printerModel = PrinterModel.QL_800;
                    break;
                case "QL-810W":
                    printerModel = PrinterModel.QL_810W;
                    break;
                case "QL-820NWB":
                    printerModel = PrinterModel.QL_820NWB;
                    break;
                case "QL-1115NWB":
                    printerModel = PrinterModel.QL_1115NWB;
                    break;
                default:
                    Log.e(TAG, "Unsupported printer model: " + modelName);
                    return null;
            }
            
            QLPrintSettings printSettings = new QLPrintSettings(printerModel);
            
            printSettings.setAutoCut(true);
            printSettings.setWorkPath(reactContext.getExternalFilesDir(null).toString());
            
            if (printerDriver != null) {
                GetStatusResult statusResult = printerDriver.getPrinterStatus();
                
                if (statusResult != null && 
                    statusResult.getError().getCode() == GetStatusError.ErrorCode.NoError && 
                    statusResult.getPrinterStatus() != null && 
                    statusResult.getPrinterStatus().getMediaInfo() != null) {
                    
                    QLPrintSettings.LabelSize currentLabelSize = statusResult.getPrinterStatus().getMediaInfo().getQLLabelSize();
                    if (currentLabelSize != null) {
                        printSettings.setLabelSize(currentLabelSize);
                    }
                }
            }
            
            return printSettings;
        } catch (Exception e) {
            Log.e(TAG, "Error creating print settings: " + e.getMessage());
            return null;
        }
    }

    @ReactMethod
    public void printImageViaWifi(String uri, String ipAddress, String modelName, Promise promise) {
        if (uri == null || uri.isEmpty()) {
            promise.reject("INVALID_PARAMS", "URI cannot be empty");
            return;
        }

        if (ipAddress == null || ipAddress.isEmpty()) {
            promise.reject("INVALID_PARAMS", "IP address cannot be empty");
            return;
        }

        if (modelName == null || modelName.isEmpty()) {
            promise.reject("INVALID_PARAMS", "Model name cannot be empty");
            return;
        }

        printerHandler.post(() -> {
            PrinterDriver printerDriver = null;
            try {
                Channel channel = Channel.newWifiChannel(ipAddress);
                PrinterDriverGenerateResult result = PrinterDriverGenerator.openChannel(channel);
                
                if (result.getError().getCode() != OpenChannelError.ErrorCode.NoError) {
                    Log.e(TAG, "Error opening channel: " + result.getError().getCode());
                    promise.reject("PRINTER_CONNECTION_ERROR", "Failed to open printer channel: " + result.getError().getCode());
                    return;
                }

                printerDriver = result.getDriver();
                if (printerDriver == null) {
                    Log.e(TAG, "Printer driver is null");
                    promise.reject("PRINTER_DRIVER_ERROR", "Failed to create printer driver");
                    return;
                }

                try {
                    QLPrintSettings printSettings = getPrinterSettings(modelName, printerDriver);
                    if (printSettings == null) {
                        promise.reject("INVALID_MODEL", "Failed to get printer settings for model: " + modelName);
                        return;
                    }
                    
                    Uri imageUri = Uri.parse(uri);
                    String filePath = imageUri.getPath();
                    
                    if (filePath == null) {
                        Log.e(TAG, "Invalid file path from URI");
                        promise.reject("INVALID_PATH", "Could not get file path from URI");
                        return;
                    }

                    PrintError printError = printerDriver.printImage(filePath, printSettings);
                    
                    if (printError.getCode() != PrintError.ErrorCode.NoError) {
                        Log.e(TAG, "Print error: " + printError.getCode());
                        promise.reject("PRINT_ERROR", "Print error: " + printError.getCode());
                        return;
                    }

                    promise.resolve(null);
                } finally {
                    printerDriver.closeChannel();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in printImageViaWifi: " + e.getMessage());
                e.printStackTrace();
                promise.reject("PRINT_ERROR", "Error in printImageViaWifi: " + e.getMessage());
            }
        });
    }

    @ReactMethod
    public void printImageViaBluetooth(String uri, String modelName, Promise promise) {
        if (uri == null || uri.isEmpty()) {
            promise.reject("INVALID_PARAMS", "URI cannot be empty");
            return;
        }

        if (modelName == null || modelName.isEmpty()) {
            promise.reject("INVALID_PARAMS", "Model name cannot be empty");
            return;
        }

        if (!checkBluetoothPermissions()) {
            promise.reject("BLUETOOTH_PERMISSION_ERROR", "Missing required Bluetooth permissions");
            return;
        }

        printerHandler.post(() -> {
            PrinterDriver printerDriver = null;
            try {
                PrinterSearchResult searchResult = PrinterSearcher.startBluetoothSearch(reactContext);
                
                if (searchResult.getError().getCode() != PrinterSearchError.ErrorCode.NoError) {
                    Log.e(TAG, "Error searching for printer: " + searchResult.getError().getCode());
                    promise.reject("SEARCH_ERROR", "Error searching for printer: " + searchResult.getError().getCode());
                    return;
                }

                Channel printerChannel = null;
                for (Channel channel : searchResult.getChannels()) {
                    String foundModel = null;
                    try {
                        if (channel.getExtraInfo() != null) {
                            foundModel = (String) channel.getExtraInfo().get(Channel.ExtraInfoKey.ModelName);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Could not get model name from channel: " + e.getMessage());
                    }
                    
                    if (foundModel != null && foundModel.startsWith(modelName)) {
                        printerChannel = channel;
                        break;
                    }
                }

                if (printerChannel == null) {
                    Log.e(TAG, "Printer not found: " + modelName);
                    promise.reject("PRINTER_NOT_FOUND", "Printer not connected");
                    return;
                }

                PrinterDriverGenerateResult result = PrinterDriverGenerator.openChannel(printerChannel);
                
                if (result.getError().getCode() != OpenChannelError.ErrorCode.NoError) {
                    Log.e(TAG, "Error opening channel: " + result.getError().getCode());
                    promise.reject("PRINTER_CONNECTION_ERROR", "Failed to open printer channel: " + result.getError().getCode());
                    return;
                }

                printerDriver = result.getDriver();
                if (printerDriver == null) {
                    Log.e(TAG, "Printer driver is null");
                    promise.reject("PRINTER_DRIVER_ERROR", "Failed to create printer driver");
                    return;
                }

                try {
                    QLPrintSettings printSettings = getPrinterSettings(modelName, printerDriver);
                    if (printSettings == null) {
                        promise.reject("INVALID_MODEL", "Failed to get printer settings for model: " + modelName);
                        return;
                    }
                    
                    Uri imageUri = Uri.parse(uri);
                    String filePath = imageUri.getPath();
                    
                    if (filePath == null) {
                        Log.e(TAG, "Invalid file path from URI");
                        promise.reject("INVALID_PATH", "Could not get file path from URI");
                        return;
                    }

                    PrintError printError = printerDriver.printImage(filePath, printSettings);
                    
                    if (printError.getCode() != PrintError.ErrorCode.NoError) {
                        Log.e(TAG, "Print error: " + printError.getCode());
                        promise.reject("PRINT_ERROR", "Print error: " + printError.getCode());
                        return;
                    }

                    promise.resolve(null);
                } finally {
                    printerDriver.closeChannel();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in printImageViaBluetooth: " + e.getMessage());
                e.printStackTrace();
                promise.reject("PRINT_ERROR", "Error in printImageViaBluetooth: " + e.getMessage());
            }
        });
    }
} 