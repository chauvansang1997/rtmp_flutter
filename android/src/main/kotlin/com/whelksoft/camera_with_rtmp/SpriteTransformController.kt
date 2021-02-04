package com.whelksoft.camera_with_rtmp

import android.graphics.PointF
import android.os.Build
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import com.pedro.encoder.input.gl.render.filters.AndroidViewFilterRender
import com.pedro.encoder.input.gl.render.filters.BaseFilterRender
import com.pedro.encoder.input.gl.render.filters.`object`.BaseObjectFilterRender
import com.pedro.encoder.input.video.CameraHelper
import kotlin.math.sqrt

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class SpriteTransformController(private val baseFilterSprite: BaseObjectFilterRender?, private val androidViewSprite: AndroidViewFilterRender?) {
    private var baseObjectFilterRender: BaseObjectFilterRender? = null
    private var androidViewFilterRender: AndroidViewFilterRender? = null
    private var lastDistance = 0f
    private var preventMoveOutside = true

    init {
        baseObjectFilterRender = baseFilterSprite
        androidViewFilterRender = androidViewSprite
    }

    fun getFilterRender(): BaseFilterRender? {
        return if (androidViewFilterRender == null) baseObjectFilterRender else androidViewFilterRender
    }

    fun setBaseObjectFilterRender(baseObjectFilterRender: BaseObjectFilterRender?) {
        this.baseObjectFilterRender = baseObjectFilterRender
        androidViewFilterRender = null
    }

    fun setBaseObjectFilterRender(androidViewFilterRender: AndroidViewFilterRender?) {
        this.androidViewFilterRender = androidViewFilterRender
        baseObjectFilterRender = null
    }

    fun stopListener() {
        androidViewFilterRender = null
        baseObjectFilterRender = null
    }

    fun setPreventMoveOutside(preventMoveOutside: Boolean) {
        this.preventMoveOutside = preventMoveOutside
    }

    fun spriteTouched(rect: Rect, motionEvent: MotionEvent): Boolean {
        if (baseObjectFilterRender == null && androidViewFilterRender == null) return false
        val xPercent = motionEvent.x * 100 / rect.width
        val yPercent = motionEvent.y * 100 / rect.height
        val scale: PointF
        val position: PointF
        if (baseObjectFilterRender != null) {
            scale = baseObjectFilterRender!!.scale
            position = baseObjectFilterRender!!.position
        } else {
            scale = androidViewFilterRender!!.scale
            position = androidViewFilterRender!!.position
        }
        val xTouched = xPercent >= position.x && xPercent <= position.x + scale.x
        val yTouched = yPercent >= position.y && yPercent <= position.y + scale.y
        return xTouched && yTouched
    }

    fun moveSprite(rect: Rect, point: PointF) {
        if (baseObjectFilterRender == null && androidViewFilterRender == null) return
        val xPercent = point.x * 100 / rect.width
        val yPercent = point.y * 100 / rect.height
        val scale: PointF = if (baseObjectFilterRender != null) {
            baseObjectFilterRender!!.scale
        } else {
            androidViewFilterRender!!.scale
        }
        if (preventMoveOutside) {
            var x = xPercent - scale.x / 2.0f
            var y = yPercent - scale.y / 2.0f
            if (x < 0) {
                x = 0f
            }
            if (x + scale.x > 100.0f) {
                x = 100.0f - scale.x
            }
            if (y < 0) {
                y = 0f
            }
            if (y + scale.y > 100.0f) {
                y = 100.0f - scale.y
            }
            if (baseObjectFilterRender != null) {
                baseObjectFilterRender!!.setPosition(x, y)
            } else {
                androidViewFilterRender!!.setPosition(x, y)
            }
        } else {
            if (baseObjectFilterRender != null) {
                baseObjectFilterRender!!.setPosition(xPercent - scale.x / 2f, yPercent - scale.y / 2f)
            } else {
                androidViewFilterRender!!.setPosition(xPercent - scale.x / 2f, yPercent - scale.y / 2f)
            }
        }
    }


    fun scaleSprite(scale: PointF) {
        if (baseObjectFilterRender == null && androidViewFilterRender == null) return

//        val scale: PointF = if (baseObjectFilterRender != null) {
//            baseObjectFilterRender!!.scale
//        } else {
//            androidViewFilterRender!!.scale
//        }
//        scale.x += point.x
//        scale.y += point.y
        if (baseObjectFilterRender != null) {
            baseObjectFilterRender!!.setScale(scale.x, scale.y)
        } else {
            androidViewFilterRender!!.setScale(scale.x, scale.y)
        }
//        lastDistance = distance
    }

}