/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */
import {request, PERMISSIONS, openSettings} from 'react-native-permissions';

import React, {useState} from 'react';
import {
  Button,
  DeviceEventEmitter,
  FlatList,
  Image,
  PermissionsAndroid,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  useColorScheme,
  View,
} from 'react-native';

import {Colors, Header} from 'react-native/Libraries/NewAppScreen';
import {base64Image} from './base64Image';
import {
  convertHTMLtoBase64,
  EscPosImageWithTCPConnection,
  printImageWithTCP2,
  printTextByBluetooth,
  scanBLDevice,
  scanLeDevice,
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
  const [printerName, setPrinterName] = useState<string>('');
  const [port, setPort] = useState<string>('');
  const [image, setImage] = useState<string>('');
  const [devices, setListofDevices] = useState<string[]>([]);

  const Item = ({item, onPress, backgroundColor, textColor}: any) => (
    <TouchableOpacity
      onPress={onPress}
      style={[styles.item, {backgroundColor}]}>
      <Text style={[styles.title, {color: textColor}]}>{item}</Text>
    </TouchableOpacity>
  );
  const renderItem = ({item}: {item: string}) => {
    const backgroundColor = item === printerName ? '#00008B' : 'blue';
    return (
      <Item
        item={item}
        onPress={async () => {
          setPrinterName(item);
        }}
        backgroundColor={backgroundColor}
        textColor={'white'}
      />
    );
  };

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
          <View style={{borderWidth: 5, height: 300}}>
            <FlatList
              data={devices}
              renderItem={renderItem}
              keyExtractor={item => item}
              extraData={printerName}
            />
          </View>

          <Text style={{alignSelf: 'center', fontSize: 20, marginTop: 10}}>
            Current Printer :{printerName}
          </Text>
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
          <Button
            title="startBTDisovery"
            onPress={async () => {
              const requestBLEPermissions = async () => {
                const res = await PermissionsAndroid.request(
                  PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
                );
                await PermissionsAndroid.requestMultiple([
                  PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
                  PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
                ]);
                console.log(res);
              };
              await requestBLEPermissions();
              const granted = await PermissionsAndroid.request(
                PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
                {
                  title: 'Android Scan Permission',
                  message: 'Scan Bluetooth Permission',
                  buttonNeutral: 'Ask Me Later',
                  buttonNegative: 'Cancel',
                  buttonPositive: 'OK',
                },
              );
              console.log('This is granted', granted);
              if (granted) {
                const results = await scanBLDevice();
                console.log(results);
                setListofDevices(results);
              }
            }}
          />

          <Button
            title="printTextByBluetooth"
            onPress={async () => {
              const result = await printTextByBluetooth(printerName);
              console.log(result);
            }}
          />

          <Button
            title="startLEDiscovery"
            onPress={async () => {
              const requestLocationPermissions = async () => {
                try {
                  const res = await PermissionsAndroid.request(
                    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
                    {
                      title: 'App asking for Fine Location ',
                      message: ' App needs access to your Location ',
                      buttonNeutral: 'Ask Me Later',
                      buttonNegative: 'Cancel',
                      buttonPositive: 'OK',
                    },
                  );
                  if (res === PermissionsAndroid.RESULTS.GRANTED) {
                    console.log('You can use the Location');
                    return true;
                  } else {
                    console.log('Location permission denied');
                    return false;
                  }
                } catch (error) {
                  console.log(error);
                }
              };

              const requestBluetoothConnect = async () => {
                try {
                  const res = await PermissionsAndroid.request(
                    PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
                    {
                      title: 'App asking for Bluetooth Connect  ',
                      message: ' App needs access to your Bluetooth Connect ',
                      buttonNeutral: 'Ask Me Later',
                      buttonNegative: 'Cancel',
                      buttonPositive: 'OK',
                    },
                  );
                  if (res === PermissionsAndroid.RESULTS.GRANTED) {
                    console.log('You can use the Bluetooth_CONNECT');
                    return true;
                  } else {
                    console.log('You cant use Bluetooth_CONNECT');
                    return false;
                  }
                } catch (error) {
                  console.log(error);
                }
              };

              const requestBluetoothPermission = async () => {
                try {
                  const res = await PermissionsAndroid.request(
                    PermissionsAndroid.PERMISSIONS.BLUETOOTH,
                    {
                      title: 'App asking for Bluetooth ',
                      message: ' App needs access to your Bluetooth Scan ',
                      buttonNeutral: 'Ask Me Later',
                      buttonNegative: 'Cancel',
                      buttonPositive: 'OK',
                    },
                  );
                  console.log('this is res in scan', res);
                  if (res === PermissionsAndroid.RESULTS.GRANTED) {
                    console.log('You can use the Bluetooth');
                    return true;
                  } else {
                    console.log('You cant use Bluetooth');
                    return false;
                  }
                } catch (error) {
                  console.log(error);
                }
              };
              const requestBluetoothScan = async () => {
                try {
                  const res = await PermissionsAndroid.request(
                    PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
                    {
                      title: 'App asking for Bluetooth Scan  ',
                      message: ' App needs access to your Bluetooth Scan ',
                      buttonNeutral: 'Ask Me Later',
                      buttonNegative: 'Cancel',
                      buttonPositive: 'OK',
                    },
                  );
                  console.log('this is res in scan', res);
                  if (res === PermissionsAndroid.RESULTS.GRANTED) {
                    console.log('You can use the Bluetooth_Scan');
                    return true;
                  } else {
                    console.log('You cant use Bluetooth_Scan');
                    return false;
                  }
                } catch (error) {
                  console.log(error);
                }
              };

              const grantedLocation = await requestLocationPermissions();
              const grantedBluetoothConnect = await requestBluetoothConnect();
              const grantedBluetoothScan = await requestBluetoothScan();
              const bluetoothCheck = await request(
                PERMISSIONS.ANDROID.BLUETOOTH_CONNECT,
              );

              console.log('This is grantedLocation', grantedLocation);
              console.log(
                'This is grantedBluetoothConnect',
                grantedBluetoothConnect,
              );
              console.log('This is grantedBluetoothScan', grantedBluetoothScan);
              console.log('This is bluetoothCheck', bluetoothCheck);
              // await openSettings();

              await scanLeDevice();
            }}
          />
          {/* 
          <Image
            style={{width: 576, height: 300, resizeMode: 'contain'}}
            source={{
              uri: `data:image/png;base64,${image}`,
            }}
          /> */}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
  item: {
    flex: 1,
    padding: 20,
    marginVertical: 8,
    marginHorizontal: 16,
  },
  title: {
    fontSize: 10,
  },
  devicesContainer: {
    height: '300',
  },
});

export default App;
