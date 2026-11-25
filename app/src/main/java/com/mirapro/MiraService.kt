package com.mirapro

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class MiraService : Service() {

    private lateinit var windowManager: WindowManager
    private var miraView: MiraView? = null
    private var imagemMira: Bitmap? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(
            WINDOW_SERVICE
        ) as WindowManager

        carregarMiraSelecionada()

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

        miraView = MiraView()

        windowManager.addView(miraView, params)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        carregarMiraSelecionada()
        miraView?.invalidate()

        return START_STICKY
    }

    private fun carregarMiraSelecionada() {

        val preferencias = getSharedPreferences(
            "MIRA_PRO",
            MODE_PRIVATE
        )

        val numero = preferencias.getInt(
            "mira_selecionada",
            1
        ).coerceIn(1, 24)

        val nomeArquivo = "miras/mira_%02d.png".format(numero)

        imagemMira = try {
            assets.open(nomeArquivo).use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    private inner class MiraView : View(this@MiraService) {

        private val tinta = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val bitmap = imagemMira ?: return

            val tamanho = 140f * resources.displayMetrics.density

            val centroX = width / 2f
            val centroY = height / 2f

            val destino = RectF(
                centroX - tamanho / 2f,
                centroY - tamanho / 2f,
                centroX + tamanho / 2f,
                centroY + tamanho / 2f
            )

            canvas.drawBitmap(
                bitmap,
                null,
                destino,
                tinta
            )
        }
    }

    override fun onDestroy() {

        miraView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // A visualização já pode ter sido removida.
            }
        }

        miraView = null
        imagemMira = null

        super.onDestroy()
    }
}
