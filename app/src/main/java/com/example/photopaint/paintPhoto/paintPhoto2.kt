package com.example.photopaint.paintPhoto

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.photopaint.Paint
import com.example.photopaint.R
import java.io.File

class paintPhoto2(

    private val file: File

): Fragment()
{
    private lateinit var mView: View
    private lateinit var btnSave: Button
    private lateinit var image : ImageView
    private lateinit var paintPanel : Paint
    private lateinit var context1: Context
    private lateinit var myBmt : Bitmap
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = inflater.inflate(R.layout.fragment_paint_photo, container, false)
        btnSave = mView.findViewById(R.id.btnSave)
        paintPanel = mView.findViewById(R.id.drawingPanel)
        val opt = BitmapFactory.Options()
        opt.inMutable = true
        myBmt = BitmapFactory.decodeFile(file.absolutePath, opt)
        paintPanel.setBitmap(myBmt)

        btnSave.setOnClickListener{
            Toast.makeText(activity, "Saved:$file", Toast.LENGTH_SHORT).show()
        }

        return mView
    }


}