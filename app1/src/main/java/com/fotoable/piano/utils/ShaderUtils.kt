/*
 *
 * ShaderUtils.java
 * 
 * Created by Wuwang on 2016/10/8
 */
package com.fotoable.piano.utils

import android.content.res.Resources
import android.opengl.GLES20
import android.util.Log

/**
 * Description:
 */
object ShaderUtils {

    private val TAG = "ShaderUtils"

    fun checkGLError(op: String) {
        Log.e("wuwang", op)
    }

    /**
     * compile shader file
     * @param shaderType
     * *
     * @param source
     * *
     * @return
     */
    fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (0 != shader) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader:" + shaderType)
                Log.e(TAG, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    fun loadShader(res: Resources, shaderType: Int, resName: String): Int {
        return loadShader(shaderType, loadFromAssetsFile(resName, res))
    }


    /**
     * 设置 vertex shader  和 fragment shader
     * @param vertexSource
     * *
     * @param fragmentSource
     * *
     * @return
     */
    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertex == 0) return 0
        val fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragment == 0) return 0
        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertex)
            checkGLError("Attach Vertex Shader")
            GLES20.glAttachShader(program, fragment)
            checkGLError("Attach Fragment Shader")
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program:" + GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    /**
     * 读取glsl 文件转为字符串
     * @param res
     * *
     * @param vertexRes
     * *
     * @param fragmentRes
     * *
     * @return
     */
    fun createProgram(res: Resources, vertexRes: String, fragmentRes: String): Int {
        return createProgram(loadFromAssetsFile(vertexRes, res), loadFromAssetsFile(fragmentRes, res))
    }

    /**
     * 读取文件内容转为字符串

     * @param fname
     * *
     * @param res
     * *
     * @return
     */
    fun loadFromAssetsFile(fname: String, res: Resources): String {
        val result = StringBuilder()
        try {
            val ins = res.assets.open(fname)
            var ch: Int
            val buffer = ByteArray(1024)
//            while (-1 != (ch = ins.read(buffer))) {
//                result.append(String(buffer, 0, ch))
//            }
            while (true) {
                ch = ins.read(buffer)
                if (-1 == ch) {
                    break
                }
                result.append(String(buffer, 0, ch))
            }

        } catch (e: Exception) {
            return ""
        }

        return result.toString().replace("\\r\\n".toRegex(), "\n")
    }

}
