package com.fotoable.piano.vary


import android.content.res.Resources
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.fotoable.piano.utils.VaryTools
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Created by wuwang on 2016/10/30
 */

class VaryRender(res: Resources) : GLSurfaceView.Renderer {

    private val tools: VaryTools
    private val cube: Cube

    init {
        tools = VaryTools()
        cube = Cube(res)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        cube.create()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val rate = width / height.toFloat()
        tools.ortho(-rate * 6, rate * 6, -6f, 6f, 3f, 20f)
        tools.setCamera(0f, 0f, 10f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        cube.setMatrix(tools.finalMatrix)
        cube.drawSelf()

        //y轴正方形平移
        tools.pushMatrix()
        tools.translate(0f, 3f, 0f)
        cube.setMatrix(tools.finalMatrix)
        cube.drawSelf()
        tools.popMatrix()

        //y轴负方向平移，然后按xyz->(0,0,0)到(1,1,1)旋转30度
        tools.pushMatrix()
        tools.translate(0f, -3f, 0f)
        tools.rotate(30f, 1f, 1f, 1f)
        cube.setMatrix(tools.finalMatrix)
        cube.drawSelf()
        tools.popMatrix()

        //x轴负方向平移，然后按xyz->(0,0,0)到(1,-1,1)旋转120度，在放大到0.5倍
        tools.pushMatrix()
        tools.translate(3f, 0f, 0f)
        tools.scale(0.5f, 0.5f, 0.5f)

        //在以上变换的基础上再进行变换
        tools.pushMatrix()
        tools.translate(-12f, 0f, 0f)
        tools.scale(1.0f, 2.0f, 1.0f)
        tools.rotate(30f, 1f, 2f, 1f)
        cube.setMatrix(tools.finalMatrix)
        cube.drawSelf()
        tools.popMatrix()

        //接着被中断的地方执行
        tools.rotate(30f, -1f, -1f, 1f)
        cube.setMatrix(tools.finalMatrix)
        cube.drawSelf()
        tools.popMatrix()
    }

}
