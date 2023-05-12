/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useState} from 'react';
import {
  Button,
  DeviceEventEmitter,
  SafeAreaView,
  ScrollView,
  StatusBar,
  useColorScheme,
  View,
} from 'react-native';

import {Colors, Header} from 'react-native/Libraries/NewAppScreen';
import {base64Image} from './base64Image';
import {
  EscPosImageWithTCPConnection,
  printImageWithTCP2,
  startNetworkDiscovery,
  stopNetworkDiscovery,
} from './PrinterModule';
import {
  SunmiSetBTPrinter,
  SunmiConnect,
  SunmiPrintImage,
} from './PrinterModule';

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const [ipAddress, setIpAddress] = useState<string>('');
  const [port, setPort] = useState<string>('');

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Header />
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}>
          <Button
            title="Find Bluetooth Printer"
            onPress={async () => {
              const BTprinterStatus = await SunmiSetBTPrinter();
              console.log('This is the printer mac Addres ', BTprinterStatus);
            }}
          />
          <Button
            title="Connect Bluetooth Printer"
            onPress={async () => {
              await SunmiConnect();
            }}
          />
          <Button
            title="Print"
            onPress={async () => {
              const Print = await SunmiPrintImage(base64Image);
              console.log(Print);
            }}
          />
          <Button
            title="Print with TCP"
            onPress={async () => {
              const Print = await EscPosImageWithTCPConnection(
                base64Image,
                ipAddress,
                port,
                80,
              );
              console.log(Print);
            }}
          />
          <Button
            title="Print with TCP2"
            onPress={async () => {
              const Print = await printImageWithTCP2(
                base64Image,
                ipAddress,
                port,
              );
              console.log(Print);
            }}
          />
          <Button
            title="Start Network Discovery"
            onPress={async () => {
              const Print = await startNetworkDiscovery();
              DeviceEventEmitter.addListener('OnPrinterFound', event => {
                console.log(event.ip);
                console.log(event.port);
                setIpAddress(event.ip); //recommended way is to create a listview where onclick it would set the IpAddress
                //im testing this where there is only one machine discoverable
                setPort(event.port);
              });
              console.log(Print);
            }}
          />
          <Button
            title="Stop Discovery"
            onPress={async () => {
              const Print = await stopNetworkDiscovery();
              DeviceEventEmitter.removeAllListeners();
              console.log(Print);
            }}
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

export default App;
