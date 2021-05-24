package com.example.photopaint

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config.*
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.drawToBitmap
import kotlin.math.max
import android.graphics.Bitmap.Config.ARGB_8888 as ARGB_88881


class Paint(context : Context, attrs: AttributeSet?) : View(context, attrs) {
    constructor(context: Context) : this(context, null){}

    private var myBitmap: Bitmap? = null
    private var myWidth = 0
    private var myHeight = 0
    private  val bgColor = 0xFFFFFDe0.toInt()
    private  val bgPaint = Paint()
    private val fgColor = 0xff00ff00.toInt()
    private val fgPaint = Paint()


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        super.onSizeChanged(w, h, oldw, oldh)
        if(w > 0 && h > 0){
            myWidth = w
            myHeight = h
        }


    }

    private val paint = Paint().apply {
        color = fgColor
        isAntiAlias = true
        isDither = true // Dithering affects how colors with higher-precision than the device are down-sampled
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = 5F // default: Hairline-width (really thin)
    }

    init{

        bgPaint.color = bgColor
        fgPaint.color = fgColor


    }



    fun setBitmap(bitmap: Bitmap){


        myBitmap = bitmap.rotate()

    }
    private val path: MutableList<Pair<Path, Paint>> = mutableListOf()

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.let{ cnv->
            myBitmap?.let{bmt->

                cnv.drawBitmap(bmt,null, Rect(0,0,myWidth,myHeight), fgPaint)

            }
            for (pair in path) {
                cnv.drawPath(pair.first, pair.second)
            }

        }
    }

    // private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                path.add(Pair(Path(), Paint(paint)))
                path.last().first.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE->{
                path.last().first.lineTo(event.x, event.y)
                path.last().first.moveTo(event.x, event.y)
            }


        }
        invalidate()
        return true
    }




    fun save(canvas: Canvas?){
        canvas?.let {

        }
    }

}