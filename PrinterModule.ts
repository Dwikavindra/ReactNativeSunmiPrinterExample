import {NativeModules} from 'react-native';
const {PrinterModule} = NativeModules;
export const SunmiSetBTPrinter = async () => {
  try {
    return await PrinterModule.setBTPrinter();
  } catch (error) {
    return error;
  }
};
export const SunmiConnect = async () => {
  try {
    return await PrinterModule.connect();
  } catch (error) {
    return error;
  }
};

export const SunmiPrintImage = async (base64Image: string) => {
  try {
    return await PrinterModule.printImage(base64Image);
  } catch (error) {
    return error;
  }
};

export const EscPosImageWithTCPConnection = async (
  base64Image: string,
  ipAddress: String,
  port: String,
  paperWidth: Number,
) => {
  try {
    return await PrinterModule.printImageWithTCP(
      base64Image,
      ipAddress,
      port,
      paperWidth,
    );
  } catch (error) {
    return error;
  }
};
export const startNetworkDiscovery = async () => {
  try {
    return await PrinterModule.startDiscovery();
  } catch (error) {
    return error;
  }
};

export const stopNetworkDiscovery = async () => {
  try {
    return await PrinterModule.stopDiscovery();
  } catch (error) {
    return error;
  }
};
export const printImageWithTCP2 = async (
  base64Image: string,
  ipAddress: string,
  port: string,
) => {
  try {
    return await PrinterModule.printImageWithTCP2(base64Image, ipAddress, port);
  } catch (error) {
    return error;
  }
};
export default PrinterModule;
