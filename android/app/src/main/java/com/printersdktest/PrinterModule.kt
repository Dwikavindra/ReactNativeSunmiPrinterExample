
package com.printersdktest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.github.anastaciocintra.escpos.EscPos
import com.github.anastaciocintra.escpos.image.BitonalOrderedDither
import com.github.anastaciocintra.escpos.image.CoffeeImage
import com.github.anastaciocintra.escpos.image.EscPosImage
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper
import com.github.anastaciocintra.output.TcpIpOutputStream
import com.izettle.html2bitmap.Html2Bitmap
import com.izettle.html2bitmap.content.WebViewContent
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetAddress
import java.util.SortedSet
import java.util.TreeSet
import com.facebook.react.bridge.Arguments


class CoffeeImageAndroidImpl(private val bitmap: Bitmap) : CoffeeImage {
    override fun getWidth(): Int {
        return bitmap.width
    }

    override fun getHeight(): Int {
        return bitmap.height
    }

    override fun getSubimage(x: Int, y: Int, w: Int, h: Int): CoffeeImage {
        return CoffeeImageAndroidImpl(Bitmap.createBitmap(bitmap, x, y, w, h))
    }

    override fun getRGB(x: Int, y: Int): Int {
        return bitmap.getPixel(x, y)
    }
}
class PrinterModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var promise: Promise? = null
    private var nsdManager:NsdManager?=null

    private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.

        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {

            val port: Int = serviceInfo.port
            val host: InetAddress = serviceInfo.host
            val serviceName= serviceInfo.serviceName
            val payload=Arguments.createMap().apply{
                putString("Service Discovery",serviceName)
                putString("ip",host.hostAddress)
                putString("port",port.toString())
            }
            sendEvent(reactContext,"OnPrinterFound",payload)
        }
    }

    private val discoveryListener: NsdManager.DiscoveryListener = object :NsdManager.DiscoveryListener{
        override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
            nsdManager?.stopServiceDiscovery(this)

        }

        override fun onStopDiscoveryFailed(p0: String?, p1: Int) {

            nsdManager?.stopServiceDiscovery(this)
        }

        override fun onDiscoveryStarted(p0: String?) {

        }

        override fun onDiscoveryStopped(p0: String?) {

        }

        override fun onServiceFound(p0: NsdServiceInfo?) {
            println("Found")
            nsdManager?.resolveService(p0,  resolveListener)


        }

        override fun onServiceLost(p0: NsdServiceInfo?) {

        }

    }
    override fun getName(): String {
        return "PrinterModule"
    }

    @ReactMethod
    fun convertHTMLtoBase64(htmlString:String, promise:Promise){
        this.promise=promise
        Thread {
            try {
                val bitmap: Bitmap? =
                    Html2Bitmap.Builder().setContext(reactApplicationContext.applicationContext)
                        .setContent(WebViewContent.html(htmlString))
                        .build().bitmap
                val resizedBitmap = Bitmap.createScaledBitmap(
                    bitmap as Bitmap,
                    631,
                    bitmap.height,
                    true
                )/// what works the best so far 80mm
                val byteArrayOutputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
                promise.resolve(base64String)

            }catch(e: java.lang.Exception){
                e.printStackTrace()
                promise.reject("Error",e.toString())

            }

        }.start()


    }
    @ReactMethod
    fun printImageWithTCP(base64Image:String,ipAddress:String,port:String,promise: Promise) {
        this.promise=promise

        Thread {
            try {
                val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
                val scaledBitmap= Bitmap.createScaledBitmap(bitmap,bitmap.width-40,bitmap.height,true)
                val  stream = TcpIpOutputStream(ipAddress,port.toInt())
                val escpos= EscPos(stream)
                val algorithm= BitonalOrderedDither()
                val imageWrapper = RasterBitImageWrapper()
                val escposImage = EscPosImage(CoffeeImageAndroidImpl(scaledBitmap), algorithm)
                escpos.write(imageWrapper, escposImage)
                escpos.feed(5).cut(EscPos.CutMode.FULL)
                promise.resolve("Print Successfully")

                promise.resolve("Print Completed")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                promise.reject("Error",e.toString())
            }
        }.start()

}
    @ReactMethod
    fun startDiscovery(promise:Promise){
        try {
            nsdManager = reactApplicationContext.applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager
            nsdManager?.discoverServices(
             "_afpovertcp._tcp",
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
            )
            promise.resolve("Discovery Started")
        }catch (e:Exception){
            promise.reject("Error",e.toString())
        }


    }
    @ReactMethod
    fun stopDiscovery(promise:Promise){
        try {
            if(nsdManager!==null){
                nsdManager?.stopServiceDiscovery(discoveryListener)
            }
            else{
                throw Exception ("nsdManager cannot be null")
            }
            promise.resolve("Network Discovery stopped")
        }catch (e:Exception){
            promise.reject("Error",e.toString())
        }
    }

    @ReactMethod
    fun printImageWithTCP2(base64Image:String,ipAddress:String,port:String,promise: Promise){
        this.promise=promise

        Thread {
            try {
                val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
                val scaledBitmap= Bitmap.createScaledBitmap(bitmap,bitmap.width-40,bitmap.height,true)
                val  stream = TcpIpOutputStream(ipAddress,port.toInt())
                val escpos= EscPos(stream)
                val algorithm= BitonalOrderedDither()
                val imageWrapper = RasterBitImageWrapper()
                val escposImage = EscPosImage(CoffeeImageAndroidImpl(scaledBitmap), algorithm)
                escpos.write(imageWrapper, escposImage)
                escpos.cut(EscPos.CutMode.FULL)
                promise.resolve("Print Successfully")



            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                promise.reject("Error",e.toString())
            }
        }.start()


    }



    val bluetoothManager: BluetoothManager = getSystemService(this.reactApplicationContext,BluetoothManager::class.java)!!
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    private val blescanResults:SortedSet<BluetoothDevice> = TreeSet()
    // Device scan callback.

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            blescanResults.add(result.device)
            println("Is device null? ${result.device==null}")
            println("This is BLE Device Address ${result.device.address}")
            if (ActivityCompat.checkSelfPermission(
                    this@PrinterModule.reactApplicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                throw IOException("Error:Bluetooth Connect Permission Not Given")
            }
            println("This is BLE Device Name ${result.device.name}")
        }
    }
    private fun SetBLEDevicestoWriteableArray(bleDevices:Set<BluetoothDevice>):WritableArray{
        val result:WritableArray = Arguments.createArray()
        for(bleDevice in bleDevices){
            if (ActivityCompat.checkSelfPermission(
                    this.reactApplicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                throw IOException("Error:Bluetooth Connect Permission Not Given")
            }
            result.pushString(bleDevice.name)
        }
        return result

    }
    @ReactMethod
    private fun scanLeDevice(promise:Promise) {
        Thread {
            if (!scanning) { // Stops scanning after a pre-defined scan period.

                /// run this commmand below after 10 seconds
                handler.postDelayed({
                    scanning = false
                    if (ActivityCompat.checkSelfPermission(
                            this.reactApplicationContext,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        promise.reject("Bluetoth Scan Permission","Not GrantedΩ")

                    }
                    bluetoothLeScanner.stopScan(leScanCallback)
                    val result:WritableArray = SetBLEDevicestoWriteableArray(blescanResults)
                    promise.resolve(result)
                }, SCAN_PERIOD)
                scanning = true
                bluetoothLeScanner.startScan(leScanCallback)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback);
            }
        }.start()
    }


}