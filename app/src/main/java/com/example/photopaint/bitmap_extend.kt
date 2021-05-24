package com.example.photopaint

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.rotate(): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(90F)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}