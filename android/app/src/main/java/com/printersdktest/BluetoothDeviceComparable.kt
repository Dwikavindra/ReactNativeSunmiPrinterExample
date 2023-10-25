package com.printersdktest

import android.bluetooth.BluetoothDevice

class BluetoothDeviceComparable(device:BluetoothDevice?):Comparable<BluetoothDeviceComparable> {
    val bluetoothDevice=device
    val address=device?.address
    override fun compareTo(other: BluetoothDeviceComparable): Int {
        if(this.address===null || other.address==null){
            return 0
        }
        return if(this.address.length>other.address.length){
            1
        } else if(this.address.length< other.address?.length) {
            -1
        }else{
            0
        }
    }
}