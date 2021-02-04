package com.whelksoft.camera_with_rtmp

import android.app.Activity
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.util.Log
import com.pedro.encoder.input.gl.render.filters.`object`.ImageObjectFilterRender
import com.pedro.encoder.utils.gl.TranslateTo
import com.pedro.rtplibrary.util.BitrateAdapter
import io.flutter.plugin.common.MethodChannel
import net.ossrs.rtmp.ConnectCheckerRtmp
import java.io.IOException

class CameraRtmpController(private val activity: Activity?,
                           private val dartMessenger: DartMessenger,
                           private val camera: Camera) : ConnectCheckerRtmp {
    private var bitrateAdapter: BitrateAdapter? = null
    private var rtmpCamera: RtmpCameraConnector? = null
    private val maxRetries = 3
    private var currentRetries = 0
    private val spriteTransformController: SpriteTransformController = SpriteTransformController(null, null)

    @Throws(IOException::class)
    internal fun prepareCameraForRecordAndStream(fps: Int, bitrate: Int?) {
        rtmpCamera = RtmpCameraConnector(
                context = activity!!.applicationContext!!,
                useOpenGL = true,
                isPortrait = camera.isPortrait,
                connectChecker = this)

        rtmpCamera!!.prepareAudio()
        // Bitrate for the stream/recording.
        var bitrateToUse = bitrate
        if (bitrateToUse == null) {
            bitrateToUse = 1200 * 1024
        }

        rtmpCamera?.prepareVideo(
                if (!camera.isPortrait) camera.streamingProfile.videoFrameWidth else camera.streamingProfile.videoFrameHeight,
                if (!camera.isPortrait) camera.streamingProfile.videoFrameHeight else camera.streamingProfile.videoFrameWidth,
                fps,
                bitrateToUse,
                !true,
                camera.mediaOrientation)
    }

    fun setImageFilter(data: ByteArray) {
        val imageObjectFilterRender = ImageObjectFilterRender()
        rtmpCamera!!.getGlInterface().setFilter(imageObjectFilterRender)
        imageObjectFilterRender.setImage(
                BitmapFactory.decodeByteArray(data, 0, data.size))
        imageObjectFilterRender.setDefaultScale(rtmpCamera!!.getStreamWidth(),
                rtmpCamera!!.getStreamHeight())
        imageObjectFilterRender.setPosition(TranslateTo.RIGHT)
        spriteTransformController.setBaseObjectFilterRender(imageObjectFilterRender) //Optional

        spriteTransformController.setPreventMoveOutside(false) //Optional
        //Optiona
    }

    /**
     * Bắt đầu stream với url
     *
     * @return void
     */
    internal fun startVideoStreaming(url: String?, bitrate: Int?, result: MethodChannel.Result) {
        if (url == null) {
            result.error("fileExists", "Must specify a url.", null)
            return
        }
        try {
            // Setup the rtmp session
            if (rtmpCamera == null) {
                currentRetries = 0
                prepareCameraForRecordAndStream(camera.streamingProfile.videoFrameRate, bitrate)

                // Start capturing from the camera.
                camera.createCaptureSession(
                        CameraDevice.TEMPLATE_RECORD,
                        Runnable { rtmpCamera!!.startStream(url) },
                        rtmpCamera!!.inputSurface
                )
            } else {
                rtmpCamera!!.startStream(url)
            }
            result.success(null)
        } catch (e: CameraAccessException) {
            result.error("videoStreamingFailed", e.message, null)
        } catch (e: IOException) {
            result.error("videoStreamingFailed", e.message, null)
        }
    }


    /**
     * Ngừng stream
     *
     * @return void
     */
    fun stopVideoRecordingOrStreaming(result: MethodChannel.Result) {
        Log.i("Camera", "stopVideoRecordingOrStreaming ")

        if (rtmpCamera == null) {
            result.success(null)
            return
        }
        try {
            currentRetries = 0
            if (rtmpCamera != null) {
                rtmpCamera!!.stopRecord()
                rtmpCamera!!.stopStream()
                rtmpCamera = null
            }

            result.success(null)
        } catch (e: CameraAccessException) {
            result.error("videoRecordingFailed", e.message, null)
        } catch (e: IllegalStateException) {
            result.error("videoRecordingFailed", e.message, null)
        }
    }


    /**
     * Tiếp tục stream
     *
     * @return void
     */
    fun resumeVideoStreaming(result: MethodChannel.Result) {
        if (rtmpCamera == null || !rtmpCamera!!.isStreaming) {
            result.success(null)
            return
        }
        try {
            rtmpCamera!!.resumeStream()
        } catch (e: IllegalStateException) {
            result.error("videoStreamingFailed", e.message, null)
            return
        }
        result.success(null)
    }

    /**
     * Dừng stream
     *
     * @return void
     */
    fun pauseVideoStreaming(result: MethodChannel.Result) {
        if (rtmpCamera == null || !rtmpCamera!!.isStreaming) {
            result.success(null)
            return
        }
        try {
            currentRetries = 0
            rtmpCamera!!.pauseStream()
        } catch (e: IllegalStateException) {
            result.error("videoStreamingFailed", e.message, null)
            return
        }
        result.success(null)
    }


    override fun onAuthSuccessRtmp() {

    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        if (bitrateAdapter != null) {
            bitrateAdapter!!.setMaxBitrate(bitrate.toInt());
        }
    }

    override fun onConnectionSuccessRtmp() {
        bitrateAdapter = BitrateAdapter(BitrateAdapter.Listener { bitrate -> rtmpCamera!!.setVideoBitrateOnFly(bitrate) })
        bitrateAdapter!!.setMaxBitrate(rtmpCamera!!.getBitrate())
    }

    override fun onConnectionFailedRtmp(reason: String) {
        if (rtmpCamera != null) {
            // Retry first.
            for (i in currentRetries..maxRetries) {
                currentRetries = i
                if (rtmpCamera!!.reTry(5000, reason)) {
                    activity!!.runOnUiThread {
                        dartMessenger.send(DartMessenger.EventType.RTMP_RETRY, reason)
                    }
                    // Success!
                    return
                }
            }

            rtmpCamera!!.stopStream()
            rtmpCamera = null
            activity!!.runOnUiThread {
                dartMessenger.send(DartMessenger.EventType.RTMP_STOPPED, "Failed retry")
            }
        }
    }

    override fun onAuthErrorRtmp() {
        activity!!.runOnUiThread {
            dartMessenger.send(DartMessenger.EventType.ERROR, "Auth error")
        }
    }

    override fun onDisconnectRtmp() {
        if (rtmpCamera != null) {
            rtmpCamera!!.stopStream()
            rtmpCamera = null
        }
        activity!!.runOnUiThread {
            dartMessenger.send(DartMessenger.EventType.RTMP_STOPPED, "Disconnected")
        }
    }


}