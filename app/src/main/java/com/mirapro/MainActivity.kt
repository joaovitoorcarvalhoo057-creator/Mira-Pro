package com.mirapro

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private val fundo = Color.rgb(10, 12, 16)
    private val verde = Color.rgb(0, 235, 145)
    private val branco = Color.WHITE

    private var miraSelecionada = 1

    private lateinit var selecao: TextView
    private lateinit var status: TextView
    private lateinit var preview: ImageView

    private val imagens = mutableMapOf<Int, Bitmap?>()
    private val cards = mutableListOf<LinearLayout>()
    private val textosCards = mutableListOf<TextView>()

    private val modelos = listOf(
        "Ornamental 01", "Ornamental 02", "Ornamental 03",
        "Dourada", "Futurista 01", "Futurista 02",
        "Dragão 01", "Dragão 02", "Flame 01",
        "Flame 02", "Neon azul", "Neon vermelho",
        "Neon verde", "Neon roxo", "Circular 01",
        "Circular 02", "Tribal 01", "Tribal 02",
        "Elétrica", "Congelada", "Estrela",
        "Geométrica", "Precisão", "Clássica"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = fundo
        window.navigationBarColor = fundo

        miraSelecionada = getSharedPreferences(
            "MIRA_PRO",
            MODE_PRIVATE
        ).getInt("mira_selecionada", 1)

        if (miraSelecionada !in 1..24) {
            miraSelecionada = 1
        }

        criarTela()
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    private fun carregarImagem(numero: Int): Bitmap? {

        if (imagens.containsKey(numero)) {
            return imagens[numero]
        }

        val nome = "miras/mira_%02d.png".format(numero)

        val bitmap = try {
            assets.open(nome).use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }

        imagens[numero] = bitmap
        return bitmap
    }

    private fun criarTela() {

        val scroll = ScrollView(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(25), dp(20), dp(30))
            setBackgroundColor(fundo)
        }

        scroll.addView(layout)

        val titulo = TextView(this).apply {
            text = "MIRA PRO"
            textSize = 32f
            setTextColor(verde)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        layout.addView(titulo)

        val subtitulo = TextView(this).apply {
            text = "CUSTOM CROSSHAIR • COD MOBILE"
            textSize = 11f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(25))
        }

        layout.addView(subtitulo)

        val painel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = fundoCard()
            setPadding(dp(15), dp(20), dp(15), dp(20))
        }

        preview = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dp(150), dp(150)
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageBitmap(carregarImagem(miraSelecionada))
        }

        painel.addView(preview)

        selecao = TextView(this).apply {
            text = "Modelo selecionado: ${modelos[miraSelecionada - 1]}"
            textSize = 14f
            setTextColor(branco)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }

        painel.addView(selecao)
        layout.addView(painel)

        adicionarTitulo(layout, "ESCOLHA SUA MIRA")

        status = TextView(this).apply {
            text = "24 modelos disponíveis"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(5), 0, dp(12))
        }

        layout.addView(status)

        val grade = GridLayout(this).apply {
            columnCount = 3
        }

        modelos.forEachIndexed { index, nome ->

            val numero = index + 1

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(4), dp(8), dp(4), dp(8))
                background = fundoCard(numero == miraSelecionada)
            }

            val imagem = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dp(65), dp(65)
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageBitmap(carregarImagem(numero))
            }

            card.addView(imagem)

            val texto = TextView(this).apply {
                text = "%02d\n%s".format(numero, nome)
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(
                    if (numero == miraSelecionada) verde else branco
                )
                setPadding(0, dp(5), 0, 0)
            }

            card.addView(texto)

            card.setOnClickListener {
                selecionarMira(numero)
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(120)
                columnSpec = GridLayout.spec(index % 3, 1f)
                rowSpec = GridLayout.spec(index / 3)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }

            grade.addView(card, params)

            cards.add(card)
            textosCards.add(texto)
        }

        layout.addView(grade)

        adicionarTitulo(layout, "CONTROLE DA MIRA")

        val ativar = botaoPrincipal("ATIVAR MIRA") {
            ativarMira()
        }

        layout.addView(ativar)

        val desativar = botaoSecundario("DESATIVAR MIRA") {
            pararServico()
        }

        layout.addView(desativar)

        val rodape = TextView(this).apply {
            text = "MIRA PRO • VERSÃO 1.0"
            textSize = 10f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(25), 0, 0)
        }

        layout.addView(rodape)

        atualizarSelecao()

        setContentView(scroll)
    }

    private fun selecionarMira(numero: Int) {

        miraSelecionada = numero

        getSharedPreferences(
            "MIRA_PRO",
            MODE_PRIVATE
        ).edit()
            .putInt("mira_selecionada", numero)
            .apply()

        atualizarSelecao()

        status.text = "Mira ${numero} selecionada"
    }

    private fun atualizarSelecao() {

        preview.setImageBitmap(
            carregarImagem(miraSelecionada)
        )

        selecao.text =
            "Modelo selecionado: ${modelos[miraSelecionada - 1]}"

        cards.forEachIndexed { index, card ->

            val selecionado = index + 1 == miraSelecionada

            card.background = fundoCard(selecionado)

            textosCards[index].setTextColor(
                if (selecionado) verde else branco
            )
        }
    }

    private fun ativarMira() {

        if (!Settings.canDrawOverlays(this)) {

            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            startActivity(intent)

            Toast.makeText(
                this,
                "Autorize a sobreposição de tela e tente novamente.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        try {
            startService(
                Intent(this, MiraService::class.java)
            )

            Toast.makeText(
                this,
                "Solicitação de ativação enviada!",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Não foi possível ativar a mira.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun pararServico() {

        stopService(
            Intent(this, MiraService::class.java)
        )

        Toast.makeText(
            this,
            "Mira desativada!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun adicionarTitulo(
        layout: LinearLayout,
        texto: String
    ) {
        val titulo = TextView(this).apply {
            text = texto
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(verde)
            setPadding(0, dp(24), 0, dp(10))
        }

        layout.addView(titulo)
    }

    private fun fundoCard(
        selecionado: Boolean = false
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(Color.rgb(24, 29, 36))
            cornerRadius = dp(16).toFloat()

            setStroke(
                if (selecionado) dp(2) else dp(1),
                if (selecionado) verde else Color.rgb(48, 65, 60)
            )
        }
    }

    private fun botaoPrincipal(
        texto: String,
        acao: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = texto
            textSize = 16f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            setPadding(dp(15), dp(17), dp(15), dp(17))

            background = GradientDrawable().apply {
                setColor(verde)
                cornerRadius = dp(18).toFloat()
            }

            setOnClickListener { acao() }

            layoutParams = LinearLayout.LayoutParams(
                -1, -2
            ).apply {
                setMargins(0, dp(15), 0, dp(5))
            }
        }
    }

    private fun botaoSecundario(
        texto: String,
        acao: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = texto
            textSize = 15f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(branco)
            setPadding(dp(15), dp(16), dp(15), dp(16))

            background = fundoCard()

            setOnClickListener { acao() }

            layoutParams = LinearLayout.LayoutParams(
                -1, -2
            ).apply {
                setMargins(0, dp(5), 0, dp(5))
            }
        }
    }
}
