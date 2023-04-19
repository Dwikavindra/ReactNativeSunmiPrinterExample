package com.printersdktest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.dantsu.escposprinter.EscPosPrinterCommands
import com.dantsu.escposprinter.connection.tcp.TcpConnection
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.sunmi.externalprinterlibrary.api.ConnectCallback
import com.sunmi.externalprinterlibrary.api.SunmiPrinter
import com.sunmi.externalprinterlibrary.api.SunmiPrinterApi


class PrinterModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var promise: Promise? = null
    override fun getName(): String {
        return "PrinterModule"
    }

    fun setCloudPrinter() {
        SunmiPrinterApi.getInstance().setPrinter(SunmiPrinter.SunmiCloudPrinter)
    }
    @ReactMethod
    fun setBTPrinter(promise: Promise) {
        try {
            val macList = SunmiPrinterApi.getInstance().findBleDevice(reactApplicationContext.applicationContext)
            if(macList.size > 0) {
                SunmiPrinterApi.getInstance().setPrinter(SunmiPrinter.SunmiBlueToothPrinter, macList[0])
            }
            promise.resolve(macList[0]);
        }catch (e:ArrayIndexOutOfBoundsException){
            promise.resolve("Bluetooth Printer Not Found")
        }catch (e:java.lang.IndexOutOfBoundsException){
            promise.reject("Error: ","Bluetooth Printer Not Found")
        }catch (e:Exception) {
            promise.reject("Error: ", e.toString())
        }

        }

    @ReactMethod
    fun connect(promise:Promise) {
        try{
        if(!SunmiPrinterApi.getInstance().isConnected) {
            SunmiPrinterApi.getInstance().connectPrinter(reactApplicationContext.applicationContext, object : ConnectCallback {

                override fun onFound() {
                    promise.resolve("Printer Found")
                }

                override fun onUnfound() {
                    promise.resolve("Printer Not Found")
                }

                override fun onConnect() {
                   promise.resolve("Printer Connected")
                }

                override fun onDisconnect() {
                    promise.resolve("Printer Disconnected")
                }

            })
        }}catch (e:Exception){
            promise.resolve(e.toString());
        }

    }
    @ReactMethod
    fun printImageWithBluetooth(base64Image:String,promise:Promise) {
        this.promise=promise
        Thread {
            try {
                if (SunmiPrinterApi.getInstance().isConnected) {
                    SunmiPrinterApi.getInstance().printerInit()
                    val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, bitmap.height, true)
                    SunmiPrinterApi.getInstance().printBitmap(scaledBitmap, 1)
                    SunmiPrinterApi.getInstance().cutPaper(2, 2)
                    promise.resolve("Print Success")
                }
            } catch (e: Exception) {
                promise.reject("Error: ", "Print Failed")

            }
        }.start()
    }

    @ReactMethod
    fun printImageWithTCP(base64Image:String, promise: Promise) {
        this.promise=promise

        Thread {
            try {
                val targetWidth= 600
                val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
                val scaledBitmap= Bitmap.createScaledBitmap(bitmap,targetWidth, Math.round(
                    bitmap.height.toFloat() * targetWidth.toFloat() / bitmap.width.toFloat()
                ), true)


                val printer=EscPosPrinterCommands(TcpConnection("100.96.109.236",  9100,10 ))
                val printerConnection=TcpConnection("100.96.109.236",  9100,10 )
                printer.connect()
                printer.reset()
                printer.printImage(EscPosPrinterCommands.bitmapToBytes(scaledBitmap))
                printer.feedPaper(50)
                printer.cutPaper()

                promise.resolve("Print Completed")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                promise.reject("Error",e.toString())
            }
        }.start()

}
}