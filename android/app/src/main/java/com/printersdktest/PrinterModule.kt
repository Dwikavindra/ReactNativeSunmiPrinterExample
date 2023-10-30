
package com.printersdktest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
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
import java.net.InetAddress
import java.util.SortedSet
import java.util.TreeSet


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
    val bluetoothManager: BluetoothManager = getSystemService(this.reactApplicationContext,BluetoothManager::class.java)!!
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val bleScanResults: SortedSet<BluetoothDeviceComparable> = TreeSet()
    var stream:BluetoothStream?=null

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
                escpos.feed(5).cut(EscPos.CutMode.FULL).close()
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
                escpos.close()



            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                promise.reject("Error",e.toString())
            }
        }.start()


    }







    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    // Device scan callback.
    private val serverCallBack:BluetoothGattCallback= object:BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

        }
    }




    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice=
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceComparable:BluetoothDeviceComparable=BluetoothDeviceComparable(device)
                    this@PrinterModule.bleScanResults.add(deviceComparable)

            }
        }
    }
        }

    @ReactMethod
    private fun scanBLDevice(promise:Promise) {
        this.promise=promise
            if(Helper.checkBluetoothScanPermission(this.reactApplicationContext,this.currentActivity!!)) {
                Thread {

                    if(!scanning){
                        handler.postDelayed({
                            scanning=false
                            bluetoothAdapter?.cancelDiscovery()
                            val result: WritableArray = Helper.SetBLDevicestoWriteableArray(bleScanResults,this.reactApplicationContext,this.currentActivity!!)
                            Log.d("Printer Module"," Bluetooth Discovery Returned with Results")
                            promise.resolve(result);
                        },SCAN_PERIOD)
                        scanning=true
                        bluetoothAdapter?.startDiscovery()
                        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                        this.reactApplicationContext.registerReceiver(receiver, filter)
                        Log.d("Printer Module"," Bluetooth Discovery Started")
                    }else{
                        scanning=false
                        bluetoothAdapter?.cancelDiscovery()
                        Log.d("Printer Module","Bluetooth Discovery went over the time limit")
                        promise.reject("Printer Module","Bluetooth Discovery went over the time limit")
                    }

                }.start()
            }
    }





    @ReactMethod
    private fun printImageByBluetooth(nameOraddress:String,base64Image:String,addresspromise:Promise){
        this.promise=addresspromise
        Thread {
            try {
                val blDevice=Helper.findBLDevice(nameOraddress,bluetoothAdapter!!,bleScanResults)!!
                if(stream!==null){
                    stream!!.closeSocket()
                }
                stream=BluetoothStream(blDevice, this.promise!!)
                val escpos= EscPos(stream)
                val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
                val scaledBitmap= Bitmap.createScaledBitmap(bitmap,bitmap.width-40,bitmap.height,true)
                val algorithm= BitonalOrderedDither()
                val imageWrapper = RasterBitImageWrapper()
                val escposImage = EscPosImage(CoffeeImageAndroidImpl(scaledBitmap), algorithm)
                escpos.write(imageWrapper, escposImage).feed(5).cut(EscPos.CutMode.FULL).close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                promise?.reject("Error",e.toString())
            }
        }.start()


    }

}