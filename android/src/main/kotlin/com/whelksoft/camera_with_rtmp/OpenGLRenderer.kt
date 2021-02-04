//package com.whelksoft.camera_with_rtmp
//
//import android.graphics.SurfaceTexture
//import android.opengl.EGL14.eglCreateContext
//import android.opengl.EGLContext
//import android.opengl.EGLDisplay
//import android.opengl.EGLSurface
//import android.opengl.GLES10
//import android.opengl.EGL14.eglGetCurrentDisplay
//
//
//class OpenGLRenderer() : Runnable{
//    protected val texture: SurfaceTexture? = null
//    private var egl: GLES10? = null
//    private var eglDisplay: EGLDisplay? = null
//    private var eglContext: EGLContext? = null
//    private var eglSurface: EGLSurface? = null
//    private val running = false
//
//    private fun initGL() {
//        egl = EGLContext.getGL() as GLES10
//        eglDisplay = egl.eglGetCurrentDisplay()
//
//        eglDisplay = egl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
//        if (eglDisplay === EGL10.EGL_NO_DISPLAY) {
//            throw RuntimeException("eglGetDisplay failed")
//        }
//        val version = IntArray(2)
//        if (!egl!!.eglInitialize(eglDisplay, version)) {
//            throw RuntimeException("eglInitialize failed")
//        }
//        val eglConfig: EGLConfig = chooseEglConfig()
//        eglContext = eglCreateContext(egl, eglDisplay, eglConfig)
//        eglSurface = egl!!.eglCreateWindowSurface(eglDisplay, eglConfig, texture, null)
//        if (eglSurface == null || eglSurface === EGL10.EGL_NO_SURFACE) {
//            throw RuntimeException("GL Error: " + GLUtils.getEGLErrorString(egl!!.eglGetError()))
//        }
//        if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
//            throw RuntimeException("GL make current error: " + GLUtils.getEGLErrorString(egl!!.eglGetError()))
//        }
//    }
//    override fun run() {
//        TODO("Not yet implemented")
//    }
//
//}