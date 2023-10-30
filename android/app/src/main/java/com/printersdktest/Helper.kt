package com.printersdktest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import java.util.SortedSet
import java.util.regex.Pattern

class Helper {

companion object{
     fun checkBluetoothConnectPermission(context: Context,activity: Activity):Boolean{
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1)
        }
        return true

    }
    fun checkBluetoothScanPermission(context: Context,activity: Activity):Boolean{
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                1)
        }
        return true

    }
    fun SetBLDevicestoWriteableArray(bleDevices:Set<BluetoothDeviceComparable>,context:Context,activity: Activity): WritableArray {
        val result: WritableArray = Arguments.createArray()
        var map: WritableMap = Arguments.createMap()

        if(this.checkBluetoothConnectPermission(context,activity)) {
            for (bleDevice in bleDevices) {
                map.putString("name",bleDevice.bluetoothDevice.name)
                map.putString("address",bleDevice.bluetoothDevice.address)
                result.pushMap(map)
                map= Arguments.createMap()
            }
        }
        return result

    }

     fun isMacAddress(input: String?): Boolean {
        // Define regular expressions for valid MAC address patterns
        val macAddressPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"

        // Compile the regular expression pattern
        val pattern: Pattern = Pattern.compile(macAddressPattern)

        // Check if the input matches the pattern
        return pattern.matcher(input).matches()
    }
    @SuppressLint("MissingPermission")
    fun findBLDevice(nameOraddress: String,bluetoothAdapter:BluetoothAdapter,blescanResults: SortedSet<BluetoothDeviceComparable>): BluetoothDevice? {
        try{
            val pairedDevices = bluetoothAdapter.bondedDevices
            val pairedDevice= pairedDevices.find {
                if (Helper.isMacAddress(nameOraddress)) {
                    if (nameOraddress == it.address) {
                        return it
                    }
                    if (it.name == nameOraddress) return it
                }

                return it
            }
            val foundDevice:BluetoothDeviceComparable?=blescanResults.find {
                if(Helper.isMacAddress(nameOraddress)){
                    if(nameOraddress==it.bluetoothDevice.address){
                        return it.bluetoothDevice
                    }
                }
                if(it.bluetoothDevice.name==nameOraddress) return it.bluetoothDevice

                return it.bluetoothDevice
            }
            if(pairedDevices!==null){
                return pairedDevice
            }
            return foundDevice!!.bluetoothDevice}
        catch(e:Error){
            Log.e("Error findBL","BluetoothDevice Not Found")
            throw Exception("Bluetooth Device with the name or address ${nameOraddress} Not Found")

        }
    }

}




}