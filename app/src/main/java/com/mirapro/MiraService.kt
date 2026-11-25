package com.mirapro

import android.app.Service
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class MiraService : Service() {

    private lateinit var windowManager: WindowManager
    private var miraView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(
            WINDOW_SERVICE
        ) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START

        miraView = object : View(this) {

            private val tinta = Paint().apply {
                color = Color.RED
                strokeWidth = 3f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }

            private val ponto = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)

                val x = width / 2f
                val y = height / 2f

                canvas.drawCircle(x, y, 22f, tinta)
                canvas.drawCircle(x, y, 4f, ponto)
            }
        }

        windowManager.addView(miraView, params)
    }

    override fun onDestroy() {
        miraView?.let {
            windowManager.removeView(it)
        }
        miraView = null
        super.onDestroy()
    }
}
