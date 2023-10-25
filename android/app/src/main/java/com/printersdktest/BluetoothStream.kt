package com.printersdktest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

@SuppressLint("MissingPermission")
open class BluetoothStream(device:BluetoothDevice): PipedOutputStream() {
    private var pipedInputStream: PipedInputStream? = null
    private val MY_UUID= " 00001101-0000-1000-8000-00805F9B34FB"
    private var threadPrint: Thread? = null
    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {// permission should already be checked
        device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))
    }
    var uncaughtException =
        Thread.UncaughtExceptionHandler { t: Thread?, e: Throwable ->
            Logger.getLogger(
                this.javaClass.name
            ).log(Level.SEVERE, e.message, e)
        }

    private fun checkConnect():Boolean{
        try {
            mmSocket?.connect()
            Log.d("Socket Connect","Socket Connect Successful")
            return true
        }catch(error:Error){
            Log.e("Socket Connect","Error",error)
            return false
        }
    }
    init{
        pipedInputStream = PipedInputStream()
        super.connect(pipedInputStream)
        val printRunnable=Runnable{
        //connect to BlDevice first
          if(checkConnect()){
              val mmOutStream: OutputStream = mmSocket!!.outputStream
              val mmBuffer: ByteArray = ByteArray(1024)
              while (true) {
                  val n = pipedInputStream!!.read(mmBuffer)
                  if (n < 0) break
                  mmOutStream.write(mmBuffer, 0, n)
              }
          }
        };
        threadPrint = Thread(printRunnable)
        threadPrint!!.setUncaughtExceptionHandler(uncaughtException)
        threadPrint!!.start()
    }
}