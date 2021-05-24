package com.example.photopaint.paintPhoto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseIntArray
import android.view.Surface
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.drawToBitmap
import com.example.photopaint.Paint
import com.example.photopaint.R
import com.example.photopaint.rotate

class paintActivity : AppCompatActivity() {

    private lateinit var btnSave: Button
    private lateinit var image : ImageView
    private lateinit var paintPanel : Paint
    private lateinit var context1: Context
    private lateinit var myBmt : Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_paint_photo)
        paintPanel = findViewById(R.id.drawingPanel)
        btnSave = findViewById(R.id.btnSave)
        val arguments = intent.extras
        val strings = arguments?.get("filename")
        val opt = BitmapFactory.Options()
        opt.inMutable = true
        myBmt = BitmapFactory.decodeFile(strings.toString(), opt)
        paintPanel.setBitmap(myBmt)

        btnSave.setOnClickListener{

            val bit = paintPanel.drawToBitmap(Bitmap.Config.ARGB_8888)

        }

    }


}