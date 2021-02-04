package com.whelksoft.camera_with_rtmp

import android.content.Context
import android.media.MediaCodec
import com.pedro.rtplibrary.base.Camera1Base
import java.nio.ByteBuffer

class StreamCamera(context: Context) : Camera1Base(context) {

    init {

    }

    override fun resetDroppedVideoFrames() {
        TODO("Not yet implemented")
    }

    override fun getAacDataRtp(aacBuffer: ByteBuffer?, info: MediaCodec.BufferInfo?) {
        TODO("Not yet implemented")
    }

    override fun resetSentAudioFrames() {
        TODO("Not yet implemented")
    }

    override fun getH264DataRtp(h264Buffer: ByteBuffer?, info: MediaCodec.BufferInfo?) {
        TODO("Not yet implemented")
    }

    override fun resetDroppedAudioFrames() {
        TODO("Not yet implemented")
    }

    override fun shouldRetry(reason: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int) {
        TODO("Not yet implemented")
    }

    override fun getSentVideoFrames(): Long {
        TODO("Not yet implemented")
    }

    override fun resetSentVideoFrames() {
        TODO("Not yet implemented")
    }

    override fun getSentAudioFrames(): Long {
        TODO("Not yet implemented")
    }

    override fun stopStreamRtp() {
        TODO("Not yet implemented")
    }

    override fun setLogs(enable: Boolean) {
        TODO("Not yet implemented")
    }

    override fun reConnect(delay: Long) {
        TODO("Not yet implemented")
    }

    override fun setAuthorization(user: String?, password: String?) {
        TODO("Not yet implemented")
    }

    override fun hasCongestion(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setReTries(reTries: Int) {
        TODO("Not yet implemented")
    }

    override fun getDroppedVideoFrames(): Long {
        TODO("Not yet implemented")
    }

    override fun getDroppedAudioFrames(): Long {
        TODO("Not yet implemented")
    }

    override fun onSpsPpsVpsRtp(sps: ByteBuffer?, pps: ByteBuffer?, vps: ByteBuffer?) {
        TODO("Not yet implemented")
    }

    override fun resizeCache(newSize: Int) {
        TODO("Not yet implemented")
    }

    override fun startStreamRtp(url: String?) {
        TODO("Not yet implemented")
    }

    override fun getCacheSize(): Int {
        TODO("Not yet implemented")
    }

}