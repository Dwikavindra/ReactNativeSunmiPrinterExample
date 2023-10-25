package com.printersdktest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

class BluetoothDeviceComparable(device:BluetoothDevice):Comparable<BluetoothDeviceComparable> {
    val bluetoothDevice=device

    @SuppressLint("MissingPermission")
    override fun compareTo(other: BluetoothDeviceComparable): Int {
        if(this.bluetoothDevice.name == other.bluetoothDevice.name || (this.bluetoothDevice.name==null || other.bluetoothDevice.name==null) ){
            return this.bluetoothDevice.address.compareTo(other.bluetoothDevice.address)
        }
        return this.bluetoothDevice.name.compareTo(other.bluetoothDevice.name)
    }
}