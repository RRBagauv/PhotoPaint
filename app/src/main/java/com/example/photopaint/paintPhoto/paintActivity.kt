package com.example.photopaint.paintPhoto

import android.R.attr.bitmap
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.example.photopaint.MainActivity
import com.example.photopaint.Paint
import com.example.photopaint.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


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
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.getDefault())
            val name =  "/" + simpleDateFormat.format(Calendar.getInstance().time) + "bit.jpg"
            val file = File(
                this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString(),
                name
            )

            try {
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(file)
                    bit.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    Toast.makeText(this,"Файл сохранён", Toast.LENGTH_LONG).show()
                    Intent(this.baseContext, MainActivity::class.java).let {
                        it.putExtra("filename", file.absolutePath)
                        this.startActivity(it)
                    }
                } finally {
                    if(fos != null){
                        fos.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }


}