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
  Image,
  SafeAreaView,
  ScrollView,
  StatusBar,
  useColorScheme,
  View,
} from 'react-native';

import {Colors, Header} from 'react-native/Libraries/NewAppScreen';
import {base64Image} from './base64Image';
import {
  convertHTMLtoBase64,
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
  const [image, setImage] = useState<string>('');

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
          <Button
            title="Convert HTML to Image"
            onPress={async () => {
              const base64 = await convertHTMLtoBase64(
                '' +
                  '<html>\n' +
                  '<head>\n' +
                  '<style>\n' +
                  'body {\n' +
                  '  background-color: lightblue;\n' +
                  '}\n' +
                  '\n' +
                  'h1 {\n' +
                  '  text-align: center;\n' +
                  '}\n' +
                  '\n' +
                  'p {\n' +
                  '  font-family: verdana;\n' +
                  '  font-size: 20px;\n' +
                  '}\n' +
                  'p.korean {\n' +
                  '  font-family: Single Day;\n' +
                  '  font-size: 20px;\n' +
                  '}\n' +
                  '</style>\n' +
                  '</head>' +
                  '<body>' +
                  '<h1>Hello, world.</h1>' +
                  '<p>الصفحة الرئيسية \n' + // Arabiac
                  '<br>你好，世界 \n' + // Chinese
                  '<br>こんにちは世界 \n' + // Japanese
                  '<br>Привет мир \n' + // Russian
                  '<br>नमस्ते दुनिया \n' + //  Hindi
                  '<p class="korean"><br>안녕하세요 세계</p>' + // if necessary, you can download and install on your environment the Single Day from fonts.google...
                  '</body>',
              );
              setImage(base64);
            }}
          />

          <Image
            style={{width: 576, height: 300, resizeMode: 'contain'}}
            source={{
              uri: `data:image/png;base64,${image}`,
            }}
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

export default App;
