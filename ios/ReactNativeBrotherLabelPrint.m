#import <Foundation/Foundation.h>
#import "React/RCTBridgeModule.h"
#import "React/RCTEventEmitter.h"

@interface RCT_EXTERN_MODULE(ReactNativeBrotherLabelPrint, NSObject)

RCT_EXTERN_METHOD(discoverNetworkPrinters:
                  (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(printImageViaWifi: (NSString *)printURI 
                  ipAddress:(NSString *)ip 
                  modelName:(NSString *)model
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(printImageViaBluetooth: (NSString *)printURI 
                  modelName:(NSString *)model
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

@end 