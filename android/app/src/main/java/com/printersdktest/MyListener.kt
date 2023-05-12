package com.printersdktest

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import java.net.InetAddress


class MyListener: NsdManager.ResolveListener {
    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {

        println("Resolve failed")
        val port: Int = serviceInfo.port
        val serviceName= serviceInfo.serviceName
        println("This is port ${port} and serviceName ${serviceName}")
        println("This is the error code${errorCode}")
        //your code

    }


    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        val port: Int = serviceInfo.port
        val host: InetAddress = serviceInfo.host
        val serviceName= serviceInfo.serviceName
        println("This is port ${port} and serviceName ${serviceName}")
        Log.v("On Service Resolved", "Port: ${port} Service Name : ${serviceName} host:${host.hostAddress}" )
    }
}