package com.mirapro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val preferencias by lazy {
        getSharedPreferences("MIRA_PRO", MODE_PRIVATE)
    }

    private val verde = Color.rgb(0, 230, 118)
    private val fundo = Color.rgb(12, 15, 18)
    private val painel = Color.rgb(25, 30, 35)
    private val branco = Color.WHITE

    private var miraSelecionada = 1
    private var deslocamentoX = 0
    private var deslocamentoY = 0
    private var tamanhoMira = 150
    private var miraAtiva = false

    private lateinit var imagemPreview: ImageView
    private lateinit var textoPosicao: TextView
    private lateinit var textoTamanho: TextView
    private lateinit var galeria: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        miraSelecionada = preferencias.getInt("mira_selecionada", 1)
        deslocamentoX = preferencias.getInt("offset_x", 0)
        deslocamentoY = preferencias.getInt("offset_y", 0)
        tamanhoMira = preferencias.getInt("tamanho_mira", 150)

        criarInterface()
    }

    private fun criarInterface() {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fundo)
            setPadding(18, 18, 18, 18)
        }

        val rolagem = ScrollView(this)
        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val titulo = TextView(this).apply {
            text = "MIRA PRO"
            textSize = 30f
            setTextColor(verde)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 4)
        }
        conteudo.addView(titulo)

        val subtitulo = TextView(this).apply {
            text = "PERSONALIZE SUA MIRA"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        conteudo.addView(subtitulo)

        imagemPreview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(20, 20, 20, 20)
            background = fundoArredondado(painel, 18)
        }

        val areaPreview = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 16)
        }
        areaPreview.addView(
            imagemPreview,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                190
            )
        )
        conteudo.addView(areaPreview)

        atualizarPreview()

        conteudo.addView(tituloSecao("ESCOLHA SUA MIRA"))

        galeria = GridLayout(this).apply {
            columnCount = 3
            useDefaultMargins = true
        }

        for (numero in 1..24) {
            val item = criarItemMira(numero)
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 112
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(4, 4, 4, 4)
            }
            galeria.addView(item, params)
        }

        conteudo.addView(galeria)

        conteudo.addView(tituloSecao("AJUSTAR MIRA"))

        val painelAjuste = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = fundoArredondado(painel, 18)
            setPadding(12, 16, 12, 16)
        }

        textoPosicao = TextView(this).apply {
            textSize = 14f
            setTextColor(branco)
            gravity = Gravity.CENTER
        }
        painelAjuste.addView(textoPosicao)
        atualizarTextosAjuste()

        val linhaCima = LinearLayout(this).apply {
            gravity = Gravity.CENTER
        }
        linhaCima.addView(
            botaoDirecao("↑") {
                alterarPosicao(0, -1)
            },
            direcaoParams()
        )
        painelAjuste.addView(linhaCima)

        val linhaMeio = LinearLayout(this).apply {
            gravity = Gravity.CENTER
        }
        linhaMeio.addView(
            botaoDirecao("←") {
                alterarPosicao(-1, 0)
            },
            direcaoParams()
        )
        linhaMeio.addView(
            botaoDirecao("CENTRO") {
                deslocamentoX = 0
                deslocamentoY = 0
                salvarAjustes()
                atualizarTextosAjuste()
                atualizarServicoSeAtivo()
            },
            direcaoParams()
        )
        linhaMeio.addView(
            botaoDirecao("→") {
                alterarPosicao(1, 0)
            },
            direcaoParams()
        )
        painelAjuste.addView(linhaMeio)

        val linhaBaixo = LinearLayout(this).apply {
            gravity = Gravity.CENTER
        }
        linhaBaixo.addView(
            botaoDirecao("↓") {
                alterarPosicao(0, 1)
            },
            direcaoParams()
        )
        painelAjuste.addView(linhaBaixo)

        conteudo.addView(painelAjuste)

        conteudo.addView(tituloSecao("TAMANHO DA MIRA"))

        textoTamanho = TextView(this).apply {
            setTextColor(branco)
            textSize = 14f
            gravity = Gravity.CENTER
        }
        conteudo.addView(textoTamanho)

        val barraTamanho = SeekBar(this).apply {
            max = 210
            progress = (tamanhoMira - 40).coerceIn(0, 210)
            progressTintList = android.content.res.ColorStateList.valueOf(verde)
            thumbTintList = android.content.res.ColorStateList.valueOf(verde)

            setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    tamanhoMira = progress + 40
                    textoTamanho.text = "Tamanho: $tamanhoMira"
                    salvarAjustes()
                    atualizarServicoSeAtivo()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        conteudo.addView(barraTamanho)

        textoTamanho.text = "Tamanho: $tamanhoMira"

        val botaoAtivar = criarBotao("ATIVAR MIRA", verde, Color.BLACK) {
            ativarMira()
        }
        conteudo.addView(botaoAtivar)

        val botaoDesativar = criarBotao(
            "DESATIVAR MIRA",
            Color.rgb(190, 45, 45),
            branco
        ) {
            pararServico()
        }
        conteudo.addView(botaoDesativar)

        val rodape = TextView(this).apply {
            text = "MIRA PRO • PERSONALIZAÇÃO DE MIRA"
            setTextColor(Color.GRAY)
            textSize = 10f
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 12)
        }
        conteudo.addView(rodape)

        rolagem.addView(conteudo)
        raiz.addView(
            rolagem,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(raiz)
    }

    private fun criarItemMira(numero: Int): View {
        val caixa = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(5, 5, 5, 5)
            background = fundoArredondado(
                if (numero == miraSelecionada) {
                    Color.rgb(25, 75, 52)
                } else {
                    painel
                },
                12
            )
        }

        val imagem = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageBitmap(carregarMira(numero))
        }

        caixa.addView(
            imagem,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                75
            )
        )

        val nome = TextView(this).apply {
            text = "MIRA %02d".format(numero)
            textSize = 11f
            setTextColor(if (numero == miraSelecionada) verde else branco)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        caixa.addView(nome)

        caixa.setOnClickListener {
            miraSelecionada = numero
            preferencias.edit()
                .putInt("mira_selecionada", miraSelecionada)
                .apply()

            atualizarPreview()
            recriarGaleria()
            atualizarServicoSeAtivo()
        }

        return caixa
    }

    private fun recriarGaleria() {
        galeria.removeAllViews()

        for (numero in 1..24) {
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 112
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(4, 4, 4, 4)
            }
            galeria.addView(criarItemMira(numero), params)
        }
    }

    private fun carregarMira(numero: Int): android.graphics.Bitmap? {
        return try {
            assets.open("miras/mira_%02d.png".format(numero)).use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun atualizarPreview() {
        imagemPreview.setImageBitmap(carregarMira(miraSelecionada))
    }

    private fun alterarPosicao(x: Int, y: Int) {
        deslocamentoX = (deslocamentoX + x).coerceIn(-500, 500)
        deslocamentoY = (deslocamentoY + y).coerceIn(-800, 800)

        salvarAjustes()
        atualizarTextosAjuste()
        atualizarServicoSeAtivo()
    }

    private fun atualizarTextosAjuste() {
        textoPosicao.text =
            "POSIÇÃO  X: $deslocamentoX  |  Y: $deslocamentoY"
    }

    private fun salvarAjustes() {
        preferencias.edit()
            .putInt("offset_x", deslocamentoX)
            .putInt("offset_y", deslocamentoY)
            .putInt("tamanho_mira", tamanhoMira)
            .putInt("mira_selecionada", miraSelecionada)
            .apply()
    }

    private fun atualizarServicoSeAtivo() {
        if (miraAtiva && Settings.canDrawOverlays(this)) {
            try {
                startService(Intent(this, MiraService::class.java))
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Não foi possível atualizar a mira",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun ativarMira() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1001)
            return
        }

        try {
            salvarAjustes()
            startService(Intent(this, MiraService::class.java))
            miraAtiva = true

            Toast.makeText(
                this,
                "MIRA ATIVADA!",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Erro ao ativar a mira",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun pararServico() {
        stopService(Intent(this, MiraService::class.java))
        miraAtiva = false

        Toast.makeText(
            this,
            "MIRA DESATIVADA",
            Toast.LENGTH_SHORT
        ).show()
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            if (Settings.canDrawOverlays(this)) {
                ativarMira()
            } else {
                Toast.makeText(
                    this,
                    "Permissão de sobreposição não concedida",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun tituloSecao(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 16f
            setTextColor(verde)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(4, 22, 4, 10)
        }
    }

    private fun botaoDirecao(
        texto: String,
        acao: () -> Unit
    ): Button {
        return Button(this).apply {
            text = texto
            textSize = if (texto == "CENTRO") 10f else 20f
            setTextColor(verde)
            background = fundoArredondado(
                Color.rgb(35, 45, 42),
                12
            )
            setOnClickListener { acao() }
        }
    }

    private fun direcaoParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            85,
            55
        ).apply {
            setMargins(5, 5, 5, 5)
        }
    }

    private fun criarBotao(
        texto: String,
        corFundo: Int,
        corTexto: Int,
        acao: () -> Unit
    ): Button {
        return Button(this).apply {
            text = texto
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(corTexto)
            background = fundoArredondado(corFundo, 14)
            setPadding(12, 12, 12, 12)
            setOnClickListener { acao() }

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 4)
            }
        }
    }

    private fun fundoArredondado(
        cor: Int,
        raio: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(cor)
            cornerRadius = raio.toFloat()
        }
    }
}
