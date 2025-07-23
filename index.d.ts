declare module "react-native-brother-label-print" {
  export function printImageViaWifi(
    uri: string,
    ipAddress: string,
    modelName: string
  ): Promise<void>;

  export function printImageViaBluetooth(
    uri: string,
    modelName: string
  ): Promise<void>;
}
