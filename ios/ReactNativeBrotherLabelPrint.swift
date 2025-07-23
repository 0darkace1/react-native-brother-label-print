import Foundation
import UIKit
import CoreImage
import UniformTypeIdentifiers
import BRLMPrinterKit

@objc(ReactNativeBrotherLabelPrint)
class ReactNativeBrotherLabelPrint: NSObject {

    @objc
    func discoverNetworkPrinters(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        var foundPrinters: [[String: String]] = []
        let option = BRLMNetworkSearchOption()
        option.searchDuration = 10  // seconds, adjust as needed

        // Start the network search with options and a closure for each found printer
        let result = BRLMPrinterSearcher.startNetworkSearch(option) { channel in
            // Extract model name and IP from channel info
            let modelName = channel.extraInfo?.value(forKey: BRLMChannelExtraInfoKeyModelName) as? String ?? ""
            let ipAddress = channel.channelInfo ?? ""

            foundPrinters.append([
                "modelName": modelName,
                "ipAddress": ipAddress
            ])
        }

        // Wait for the search duration plus a little buffer, then resolve promise
        DispatchQueue.global().asyncAfter(deadline: .now() + option.searchDuration + 1) {
            if result.error.code != .noError {
                reject("DISCOVERY_FAILED", "Printer search failed with error code \(result.error.code.rawValue)", nil)
                return
            }
            resolve(foundPrinters)
        }
    }
    
    @objc
    func printImageViaWifi(_ printURI: String, ipAddress ip: String, modelName model: String, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.global().async {
            print("[BrotherLabelPrint] Wifi printing ", ip, model)
            let channel = BRLMChannel(wifiIPAddress: ip)
            self.printImage(channel, printURI, modelName: model) { (errorCode, errorMessage) in
                if errorCode != 0 {
                    reject("ERROR_\(errorCode)", errorMessage ?? "Unknown error", nil)
                } else {
                    resolve(nil)
                }
            }
        }
    }
    
    @objc
    func printImageViaBluetooth(_ printURI: String, modelName model: String, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.global().async {
            print("[BrotherLabelPrint] Discovering printers")
            let searcher = BRLMPrinterSearcher.startBluetoothSearch();
            
            print("[BrotherLabelPrint] Bluetooth channels", searcher.channels.count, searcher.channels)
            
            if (searcher.error.code != .noError) {
                reject("ERROR_SEARCH", "Error searching for printer", nil)
                return
            }
                        
            let optionalChannel = searcher.channels.first(where: {(channel: BRLMChannel) in
                channel.extraInfo?.value(forKey: BRLMChannelExtraInfoKeyModelName) as! String == model
            })
            
            if (optionalChannel == nil) {
                reject("ERROR_NOT_FOUND", "Printer not connected", nil)
                return
            }
            
            self.printImage(optionalChannel!, printURI, modelName: model) { (errorCode, errorMessage) in
                if errorCode != 0 {
                    reject("ERROR_\(errorCode)", errorMessage ?? "Unknown error", nil)
                } else {
                    resolve(nil)
                }
            }
        }
    }
    
    func printImage(_ channel: BRLMChannel, _ printURI: String, modelName model: String, cb callback: @escaping (_ errorCode: Int, _ errorMessage: String?) -> Void) -> Void {
        let generateResult = BRLMPrinterDriverGenerator.open(channel)
        guard generateResult.error.code == BRLMOpenChannelErrorCode.noError,
            let printerDriver = generateResult.driver else {
                print("[BrotherLabelPrint] Error - Open Channel: \(generateResult.error.code)")
                callback(Int(generateResult.error.code.rawValue), "Error opening the printer")
                return
        }
        defer {
            printerDriver.closeChannel()
        }
                    
        let printerStatus = printerDriver.getPrinterStatus()
        
        if (printerStatus.error.code != .noError) {
            print("[BrotherLabelPrint] Printer status cannot be fetched")
            callback(Int(printerStatus.error.code.rawValue), "Error fetching printer status")
            return
        }
        
        let pStatus = printerStatus.status!;
        
        var fetchedLabelSize = true;
        
        let printSettings = BRLMQLPrintSettings(defaultPrintSettingsWith: pStatus.model)
        
        if (printSettings == nil) {
            print("[BrotherLabelPrint] Unable to initalize print settings")
            callback(104, "Error initializing print settings")
            return
        }
        let pSettings = printSettings!;
        
        let labelSize = pStatus.mediaInfo?.getQLLabelSize(&fetchedLabelSize)
        
        if (!fetchedLabelSize && labelSize != nil) {
            print("[BrotherLabelPrint] Error fetching label size")
            callback(103, "Error fetching label size")
            return
        }
                  
        pSettings.autoCut = true
        pSettings.labelSize = labelSize!
        
        let url = URL(fileURLWithPath: printURI);
        let printError = printerDriver.printImage(with: url, settings: pSettings)
        
        if (printError.code != .noError) {
            print("[BrotherLabelPrint] Error printing", printError.code)
            callback(Int(printError.code.rawValue), "Error printing")
            return
        }
        
        callback(0, nil)
    }
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
      return true
    }
} 