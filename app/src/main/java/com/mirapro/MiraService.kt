package com.mirapro

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
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
    private var botaoMira: TextView? = null
    private var miraParams: WindowManager.LayoutParams? = null
    private var botaoParams: WindowManager.LayoutParams? = null

    private val prefs by lazy {
        getSharedPreferences("MIRA_PRO", MODE_PRIVATE)
    }

    private val densidade by lazy {
        resources.displayMetrics.density
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        criarMira()
        criarBotaoFlutuante()
        carregarConfiguracoes()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        carregarConfiguracoes()
        miraView?.mostrarMira(false)
        return START_STICKY
    }

    private fun tipoJanela(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun criarMira() {
        miraView = MiraOverlayView()

        miraParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            tipoJanela(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(miraView, miraParams)
    }

    private fun criarBotaoFlutuante() {
        val botao = TextView(this)

        botao.text = "MIRA"
        botao.textSize = 10f
        botao.setTextColor(Color.WHITE)
        botao.typeface = Typeface.DEFAULT_BOLD
        botao.gravity = Gravity.CENTER
        botao.alpha = 0.5f
        botao.elevation = 10f

        val fundo = GradientDrawable()
        fundo.shape = GradientDrawable.OVAL
        fundo.setColor(Color.rgb(23, 180, 85))
        fundo.setStroke(
            (2 * densidade).toInt(),
            Color.WHITE
        )
        botao.background = fundo

        // Botão reduzido exatamente pela metade: 62 dp para 31 dp.
        val tamanhoBotao = (31 * densidade).toInt()

        val params = WindowManager.LayoutParams(
            tamanhoBotao,
            tamanhoBotao,
            tipoJanela(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = (24 * densidade).toInt()
            y = (250 * densidade).toInt()
        }

        botaoParams = params
        botaoMira = botao

        val salvo = prefs.getBoolean("botao_posicao_salva", false)

        if (salvo) {
            params.x = prefs.getInt("botao_x", params.x)
            params.y = prefs.getInt("botao_y", params.y)
        }

        var inicioX = 0f
        var inicioY = 0f
        var inicialBotaoX = 0
        var inicialBotaoY = 0
        var arrastando = false

        botao.setOnTouchListener { _, evento ->

            when (evento.action) {

                MotionEvent.ACTION_DOWN -> {
                    inicioX = evento.rawX
                    inicioY = evento.rawY

                    inicialBotaoX = params.x
                    inicialBotaoY = params.y

                    arrastando = false

                    miraView?.mostrarMira(true)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deslocamentoX = evento.rawX - inicioX
                    val deslocamentoY = evento.rawY - inicioY

                    if (
                        abs(deslocamentoX) > 6 * densidade ||
                        abs(deslocamentoY) > 6 * densidade
                    ) {
                        arrastando = true
                    }

                    if (arrastando) {
                        params.x = inicialBotaoX -
                            deslocamentoX.toInt()

                        params.y = inicialBotaoY -
                            deslocamentoY.toInt()

                        try {
                            windowManager.updateViewLayout(botao, params)
                        } catch (_: Exception) {
                        }
                    }

                    true
                }

                MotionEvent.ACTION_UP -> {
                    miraView?.mostrarMira(false)

                    if (arrastando) {
                        prefs.edit()
                            .putInt("botao_x", params.x)
                            .putInt("botao_y", params.y)
                            .putBoolean("botao_posicao_salva", true)
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

        windowManager.addView(botao, params)
    }

    private fun carregarConfiguracoes() {
        val nomeMira = obterNomeMira()

        val tamanho = obterNumero("tamanho_mira", 140).toFloat()
        val offsetX = obterNumero("offset_x", 0).toFloat()
        val offsetY = obterNumero("offset_y", 0).toFloat()

        miraView?.atualizarMira(
            nomeMira,
            tamanho,
            offsetX,
            offsetY
        )
    }

    private fun obterNomeMira(): String {
        val valor = prefs.all["mira_selecionada"]?.toString()

        if (valor.isNullOrBlank()) {
            return "mira_01"
        }

        val numero = valor.toIntOrNull()

        if (numero != null) {
            return "mira_%02d".format(numero.coerceIn(1, 24))
        }

        return valor.removeSuffix(".png")
    }

    private fun obterNumero(chave: String, padrao: Int): Int {
        val valor = prefs.all[chave]

        return when (valor) {
            is Number -> valor.toInt()
            is String -> valor.toIntOrNull() ?: padrao
            else -> padrao
        }
    }

    inner class MiraOverlayView : View(this@MiraService) {

        private val pincel = Paint(Paint.ANTI_ALIAS_FLAG)

        private var bitmap: Bitmap? = null
        private var nomeAtual = ""
        private var tamanhoMira = 140f
        private var deslocamentoX = 0f
        private var deslocamentoY = 0f
        private var visivel = false

        init {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        fun atualizarMira(
            nome: String,
            tamanho: Float,
            x: Float,
            y: Float
        ) {
            if (nome != nomeAtual) {
                nomeAtual = nome

                try {
                    assets.open("miras/$nome.png").use {
                        bitmap = BitmapFactory.decodeStream(it)
                    }
                } catch (_: Exception) {
                    bitmap = null
                }
            }

            tamanhoMira = tamanho.coerceIn(40f, 250f)
            deslocamentoX = x
            deslocamentoY = y

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

            val centroX = width / 2f + deslocamentoX * densidade
            val centroY = height / 2f + deslocamentoY * densidade

            val largura = tamanhoMira * densidade
            val altura = largura * imagem.height / imagem.width.toFloat()

            val esquerda = centroX - largura / 2f
            val topo = centroY - altura / 2f

            pincel.alpha = 255

            canvas.drawBitmap(
                imagem,
                null,
                android.graphics.RectF(
                    esquerda,
                    topo,
                    esquerda + largura,
                    topo + altura
                ),
                pincel
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            miraView?.let {
                windowManager.removeView(it)
            }
        } catch (_: Exception) {
        }

        try {
            botaoMira?.let {
                windowManager.removeView(it)
            }
        } catch (_: Exception) {
        }

        miraView = null
        botaoMira = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
