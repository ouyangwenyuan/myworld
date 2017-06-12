package com.fotoable.piano.vary

import android.content.res.Resources
import android.opengl.GLES20

import com.fotoable.piano.utils.ShaderUtils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


/**
 * Created by wuwang on 2016/10/30
 */

class Cube(private val res: Resources) {
    private val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "varying  vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {" +
                    "  gl_Position = vMatrix*vPosition;" +
                    "  vColor=aColor;" +
                    "}"

    private val fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}"

    /**
     * 点坐标
     */
    internal val cubePositions = floatArrayOf(-1.0f, 1.0f, 1.0f, //正面左上0
            -1.0f, -1.0f, 1.0f, //正面左下1
            1.0f, -1.0f, 1.0f, //正面右下2
            1.0f, 1.0f, 1.0f, //正面右上3
            -1.0f, 1.0f, -1.0f, //反面左上4
            -1.0f, -1.0f, -1.0f, //反面左下5
            1.0f, -1.0f, -1.0f, //反面右下6
            1.0f, 1.0f, -1.0f)//反面右上7
    /**
     * 构建面，连接点的顺序，三个点构成一个三角，
     */
    internal val index = shortArrayOf(6, 7, 4, 6, 4, 5, //后面
            6, 3, 7, 6, 2, 3, //右面
            6, 5, 1, 6, 1, 2, //下面
            0, 3, 2, 0, 2, 1, //正面
            0, 1, 5, 0, 5, 4, //左面
            0, 7, 3, 0, 4, 7)//上面

    /**
     * 颜色矩阵
     */
    internal var color = floatArrayOf(0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f)

    private var vertexBuf: FloatBuffer? = null
    private var colorBuf: FloatBuffer? = null
    private var indexBuf: ShortBuffer? = null

    private var mProgram: Int = 0
    private var hVertex: Int = 0
    private var hColor: Int = 0
    private var hMatrix: Int = 0

    init {
        initData()
    }

    private fun initData() {
        val a = ByteBuffer.allocateDirect(cubePositions.size * 4)
        a.order(ByteOrder.nativeOrder())
        vertexBuf = a.asFloatBuffer()
        vertexBuf!!.put(cubePositions)
        vertexBuf!!.position(0)

        val b = ByteBuffer.allocateDirect(color.size * 4)
        b.order(ByteOrder.nativeOrder())
        colorBuf = b.asFloatBuffer()
        colorBuf!!.put(color)
        colorBuf!!.position(0)

        val c = ByteBuffer.allocateDirect(index.size * 2)
        c.order(ByteOrder.nativeOrder())
        indexBuf = c.asShortBuffer()
        indexBuf!!.put(index)
        indexBuf!!.position(0)
    }

    fun create() {
//        mProgram = ShaderUtils.createProgram(res, "vary/vertex.sh", "vary/fragment.sh")
        mProgram = ShaderUtils.createProgram(vertexShaderCode, fragmentShaderCode)
        hVertex = GLES20.glGetAttribLocation(mProgram, "vPosition")
        hColor = GLES20.glGetAttribLocation(mProgram, "aColor")
        hMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
    }

    private var matrix: FloatArray? = null
    fun setMatrix(matrix: FloatArray) {
        this.matrix = matrix
    }

    fun drawSelf() {

        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram)
        //指定vMatrix的值
        if (matrix != null) {
            GLES20.glUniformMatrix4fv(hMatrix, 1, false, matrix, 0)
        }
        //启用句柄
        GLES20.glEnableVertexAttribArray(hVertex)
        GLES20.glEnableVertexAttribArray(hColor)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(hVertex, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuf)
        //设置绘制三角形的颜色
        GLES20.glVertexAttribPointer(hColor, 4,
                GLES20.GL_FLOAT, false,
                0, colorBuf)
        //索引法绘制正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.size, GLES20.GL_UNSIGNED_SHORT, indexBuf)
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(hVertex)
        GLES20.glDisableVertexAttribArray(hColor)
    }

}
