package com.mirapro

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.*
import android.view.Gravity

class MainActivity : Activity() {

    private val fundo = Color.rgb(10, 12, 16)
    private val verde = Color.rgb(0, 235, 145)
    private val branco = Color.WHITE

    private var miraSelecionada = 1
    private lateinit var selecao: TextView
    private lateinit var status: TextView

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

        criarTela()
    }

    private fun criarTela() {

        val scroll = ScrollView(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 25, 20, 30)
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
            setPadding(0, 5, 0, 25)
        }

        layout.addView(subtitulo)

        val painel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = fundoCard()
            setPadding(15, 20, 15, 20)
        }

        val preview = TextView(this).apply {
            text = "◎"
            textSize = 85f
            setTextColor(verde)
            gravity = Gravity.CENTER
        }

        painel.addView(preview)

        selecao = TextView(this).apply {
            text = "Modelo selecionado: 01"
            textSize = 14f
            setTextColor(branco)
            gravity = Gravity.CENTER
        }

        painel.addView(selecao)
        layout.addView(painel)

        adicionarTitulo(layout, "ESCOLHA SUA MIRA")

        status = TextView(this).apply {
            text = "24 modelos disponíveis"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, 5, 0, 12)
        }

        layout.addView(status)

        val grade = android.widget.GridLayout(this).apply {
            columnCount = 3
        }

        modelos.forEachIndexed { index, nome ->

            val botao = TextView(this).apply {
                text = "${index + 1}\n$nome"
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(branco)
                setPadding(5, 12, 5, 12)
                background = fundoCard()

                setOnClickListener {
                    miraSelecionada = index + 1
                    selecao.text =
                        "Modelo ${index + 1}: $nome"
                    status.text = "Modelo selecionado: $nome"
                    atualizarGrade(grade)
                }
            }

            val params =
                android.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = 88
                    columnSpec =
                        android.widget.GridLayout.spec(index % 3, 1f)
                    rowSpec =
                        android.widget.GridLayout.spec(index / 3)
                    setMargins(4, 4, 4, 4)
                }

            grade.addView(botao, params)
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
            setPadding(0, 25, 0, 0)
        }

        layout.addView(rodape)

        setContentView(scroll)
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
                "Mira ativada!",
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

    private fun atualizarGrade(
        grade: android.widget.GridLayout
    ) {
        for (i in 0 until grade.childCount) {
            val item = grade.getChildAt(i) as TextView

            item.setTextColor(
                if (i + 1 == miraSelecionada) verde
                else branco
            )
        }
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
            setPadding(0, 24, 0, 10)
        }

        layout.addView(titulo)
    }

    private fun fundoCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.rgb(24, 29, 36))
            cornerRadius = 16f
            setStroke(1, Color.rgb(48, 65, 60))
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
            setPadding(15, 17, 15, 17)

            background = GradientDrawable().apply {
                setColor(verde)
                cornerRadius = 18f
            }

            setOnClickListener { acao() }

            layoutParams = LinearLayout.LayoutParams(
                -1, -2
            ).apply {
                setMargins(0, 15, 0, 5)
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
            setPadding(15, 16, 15, 16)

            background = fundoCard()

            setOnClickListener { acao() }

            layoutParams = LinearLayout.LayoutParams(
                -1, -2
            ).apply {
                setMargins(0, 5, 0, 5)
            }
        }
    }
}
