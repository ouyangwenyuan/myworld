package com.fotoable.piano.vary

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


class VaryActivity : AppCompatActivity() {

    private var mGLView: GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initGL()
    }

    fun initGL() {
        mGLView = GLSurfaceView(applicationContext)
        setContentView(mGLView)
        mGLView!!.setEGLContextClientVersion(2)
        mGLView!!.setRenderer(VaryRender(resources))
        mGLView!!.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

}
