package com.fotoable.piano.utils

import android.opengl.Matrix

import java.util.Arrays
import java.util.Stack

/**
 * Created by wuwang on 2016/10/30
 */

class VaryTools {

    private val mMatrixCamera = FloatArray(16)    //相机矩阵
    private val mMatrixProjection = FloatArray(16)    //投影矩阵
    private var mMatrixCurrent = floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f)//原始矩阵

    private val mStack: Stack<FloatArray>      //变换矩阵堆栈

    init {
        mStack = Stack<FloatArray>()
    }

    //保护现场
    fun pushMatrix() {
        mStack.push(Arrays.copyOf(mMatrixCurrent, 16))
    }

    //恢复现场
    fun popMatrix() {
        mMatrixCurrent = mStack.pop()
    }

    fun clearStack() {
        mStack.clear()
    }

    //平移变换
    fun translate(x: Float, y: Float, z: Float) {
        Matrix.translateM(mMatrixCurrent, 0, x, y, z)
    }

    //旋转变换
    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.rotateM(mMatrixCurrent, 0, angle, x, y, z)
    }

    //缩放变换
    fun scale(x: Float, y: Float, z: Float) {
        Matrix.scaleM(mMatrixCurrent, 0, x, y, z)
    }

    //设置相机
    fun setCamera(ex: Float, ey: Float, ez: Float, cx: Float, cy: Float, cz: Float, ux: Float, uy: Float, uz: Float) {
        Matrix.setLookAtM(mMatrixCamera, 0, ex, ey, ez, cx, cy, cz, ux, uy, uz)
    }

    fun frustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        Matrix.frustumM(mMatrixProjection, 0, left, right, bottom, top, near, far)
    }

    fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        Matrix.orthoM(mMatrixProjection, 0, left, right, bottom, top, near, far)
    }

    val finalMatrix: FloatArray
        get() {
            val ans = FloatArray(16)
            Matrix.multiplyMM(ans, 0, mMatrixCamera, 0, mMatrixCurrent, 0)
            Matrix.multiplyMM(ans, 0, mMatrixProjection, 0, ans, 0)
            return ans
        }

}
