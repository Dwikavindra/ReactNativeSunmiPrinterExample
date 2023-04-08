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
export default PrinterModule;
