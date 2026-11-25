package com.mirapro

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import kotlin.math.abs

class MiraService : Service() {

    private lateinit var windowManager: WindowManager

    private var miraView: MiraOverlayView? = null
    private var botaoFlutuante: TextView? = null
    private var parametrosBotao: WindowManager.LayoutParams? = null

    private var miraSelecionada = 1
    private var deslocamentoX = 0
    private var deslocamentoY = 0
    private var tamanhoMira = 150

    private var inicioToqueX = 0f
    private var inicioToqueY = 0f
    private var inicioBotaoX = 0
    private var inicioBotaoY = 0
    private var arrastando = false

    private val densidade: Float
        get() = resources.displayMetrics.density

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        carregarConfiguracoes()
        criarSobreposicaoMira()
        criarBotaoFlutuante()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        carregarConfiguracoes()
        miraView?.atualizarImagem()
        miraView?.mostrarMira(false)

        return START_STICKY
    }

    private fun carregarConfiguracoes() {
        val preferencias = getSharedPreferences(
            "MIRA_PRO",
            MODE_PRIVATE
        )

        miraSelecionada = preferencias.getInt(
            "mira_selecionada", 1
        )

        deslocamentoX = preferencias.getInt(
            "offset_x", 0
        )

        deslocamentoY = preferencias.getInt(
            "offset_y", 0
        )

        tamanhoMira = preferencias.getInt(
            "tamanho_mira", 150
        )
    }

    private fun criarSobreposicaoMira() {

        miraView = MiraOverlayView()

        val parametros = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            tipoSobreposicao(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        parametros.gravity = Gravity.TOP or Gravity.START

        windowManager.addView(miraView, parametros)
    }

    private fun criarBotaoFlutuante() {

        val preferencias = getSharedPreferences(
            "MIRA_PRO",
            MODE_PRIVATE
        )

        val botao = TextView(this).apply {

            text = "MIRA"
            textSize = 13f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER

            setTypeface(
                android.graphics.Typeface.DEFAULT_BOLD
            )

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(0, 230, 118))
                setStroke(
                    (2 * densidade).toInt(),
                    Color.WHITE
                )
            }

            // Transparência fixa de 50%.
            alpha = 0.5f

            elevation = 10f
        }

        val tamanhoBotao = (62 * densidade).toInt()

        val parametros = WindowManager.LayoutParams(
            tamanhoBotao,
            tamanhoBotao,
            tipoSobreposicao(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        parametros.gravity = Gravity.BOTTOM or Gravity.END

        parametros.x = preferencias.getInt(
            "botao_x",
            (24 * densidade).toInt()
        )

        parametros.y = preferencias.getInt(
            "botao_y",
            (250 * densidade).toInt()
        )

        parametrosBotao = parametros
        botaoFlutuante = botao

        configurarToqueEBotao(botao)

        windowManager.addView(botao, parametros)
    }

    private fun configurarToqueEBotao(botao: TextView) {

        botao.setOnTouchListener { _, evento ->

            val parametros = parametrosBotao
                ?: return@setOnTouchListener false

            when (evento.action) {

                MotionEvent.ACTION_DOWN -> {

                    inicioToqueX = evento.rawX
                    inicioToqueY = evento.rawY

                    inicioBotaoX = parametros.x
                    inicioBotaoY = parametros.y

                    arrastando = false

                    miraView?.mostrarMira(true)

                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    val diferencaX =
                        evento.rawX - inicioToqueX

                    val diferencaY =
                        evento.rawY - inicioToqueY

                    val limite = 6 * densidade

                    if (
                        abs(diferencaX) > limite ||
                        abs(diferencaY) > limite
                    ) {
                        arrastando = true
                    }

                    if (arrastando) {

                        // Gravity END: inverter movimento horizontal.
                        parametros.x = (
                            inicioBotaoX - diferencaX.toInt()
                        ).coerceAtLeast(0)

                        // Gravity BOTTOM: inverter movimento vertical.
                        parametros.y = (
                            inicioBotaoY - diferencaY.toInt()
                        ).coerceAtLeast(0)

                        try {
                            windowManager.updateViewLayout(
                                botao,
                                parametros
                            )
                        } catch (_: Exception) {
                        }
                    }

                    true
                }

                MotionEvent.ACTION_UP -> {

                    miraView?.mostrarMira(false)

                    if (arrastando) {
                        getSharedPreferences(
                            "MIRA_PRO",
                            MODE_PRIVATE
                        ).edit()
                            .putInt("botao_x", parametros.x)
                            .putInt("botao_y", parametros.y)
                            .apply()
                    }

                    arrastando = false

                    true
                }

                MotionEvent.ACTION_CANCEL -> {

                    miraView?.mostrarMira(false)
                    arrastando = false

                    true
                }

                else -> true
            }
        }
    }

    private fun tipoSobreposicao(): Int {
        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private inner class MiraOverlayView :
        View(this@MiraService) {

        private val pincel = Paint(
            Paint.ANTI_ALIAS_FLAG or
                Paint.FILTER_BITMAP_FLAG
        )

        private var bitmap: Bitmap? = null
        private var visivel = false

        init {
            atualizarImagem()
        }

        fun atualizarImagem() {

            bitmap = try {
                assets.open(
                    "miras/mira_%02d.png".format(miraSelecionada)
                ).use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
                null
            }

            invalidate()
        }

        fun mostrarMira(mostrar: Boolean) {
            visivel = mostrar
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (!visivel) return

            val imagem = bitmap ?: return

            val tamanho = tamanhoMira * densidade

            val centroX = width / 2f +
                deslocamentoX * densidade

            val centroY = height / 2f +
                deslocamentoY * densidade

            val destino = RectF(
                centroX - tamanho / 2f,
                centroY - tamanho / 2f,
                centroX + tamanho / 2f,
                centroY + tamanho / 2f
            )

            canvas.drawBitmap(
                imagem,
                null,
                destino,
                pincel
            )
        }
    }

    override fun onDestroy() {

        miraView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }

        botaoFlutuante?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }

        miraView = null
        botaoFlutuante = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
