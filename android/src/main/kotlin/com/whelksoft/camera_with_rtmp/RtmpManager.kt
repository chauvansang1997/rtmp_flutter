package com.whelksoft.camera_with_rtmp

import android.app.Activity
import io.flutter.plugin.common.MethodChannel

class RtmpManager(camera: Camera, activity: Activity, private val dartMessenger: DartMessenger) {
    private val camera: Camera
    private val activity: Activity
    private var cameraRtmpControllers: MutableList<CameraRtmpController>? = null
    private var cameraRtmpControllerMap: MutableMap<String, CameraRtmpController>? = null

    init {
        cameraRtmpControllers = mutableListOf()
        cameraRtmpControllerMap = mutableMapOf()
        this.camera = camera
        this.activity = activity
    }

    fun switchCamera() {

    }

    fun setImageFilter(data: ByteArray?, result: MethodChannel.Result) {
        for ((key, value) in cameraRtmpControllerMap!!) {
            value.setImageFilter(data!!)
        }
        result.success(null)
    }

    fun startConnection(url: String?, bitrate: Int?, result: MethodChannel.Result) {
        if (!cameraRtmpControllerMap!!.containsKey(url)) {
            val cameraRtmp: CameraRtmpController = CameraRtmpController(activity = activity, camera = camera,
                    dartMessenger = dartMessenger)
            cameraRtmp.startVideoStreaming(url = url, bitrate = bitrate, result = result)
            cameraRtmpControllers!!.add(cameraRtmp)
            cameraRtmpControllerMap!![url!!] = cameraRtmp

        }
    }


    fun closeAllConnection(result: MethodChannel.Result) {
    
    }

    fun closeConnection(url: String, result: MethodChannel.Result) {
        if (cameraRtmpControllerMap!!.containsKey(url)) {
            cameraRtmpControllerMap!![url]?.stopVideoRecordingOrStreaming(result)
        }
    }

}