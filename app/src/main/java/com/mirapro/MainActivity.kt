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
import android.view.View

class MainActivity : Activity() {

    private val fundo = Color.rgb(10, 12, 16)
    private val verde = Color.rgb(0, 235, 145)
    private val branco = Color.WHITE
    private var miraSelecionada = 1

    private val modelos = listOf(
        "Ornamental 01", "Ornamental 02", "Ornamental 03",
        "Dourada", "Futurista 01", "Futurista 02",
        "Dragão 01", "Dragão 02", "Flame 01",
        "Flame 02", "Neon azul", "Neon vermelho",
        "Neon verde", "Neon roxo", "Circular 01",
        "Circular 02", "Tribal 01", "Tribal 02",
        "Elétrica", "Congelada", "Estrela",
        "Geometrica", "Precisão", "Clássica"
    )

    private lateinit var status: TextView
    private lateinit var selecao: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = fundo
        window.navigationBarColor = fundo
        window.decorView.systemUiVisibility = 0

        criarTela()
    }

    private fun criarTela() {

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(fundo)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 30)
        }

        scroll.addView(layout)

        val titulo = TextView(this).apply {
            text = "MIRA PRO"
            textSize = 30f
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
            setPadding(16, 20, 16, 20)
        }

        val tituloPainel = TextView(this).apply {
            text = "SUA MIRA"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(verde)
            gravity = Gravity.CENTER
        }

        painel.addView(tituloPainel)

        val simbolo = TextView(this).apply {
            text = "◎"
            textSize = 85f
            setTextColor(verde)
            gravity = Gravity.CENTER
        }

        painel.addView(simbolo)

        selecao = TextView(this).apply {
            text = "Modelo selecionado: 01"
            textSize = 13f
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
            useDefaultMargins = false
        }

        modelos.forEachIndexed { index, nome ->

            val botao = TextView(this).apply {
                text = "${index + 1}\n$nome"
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(branco)
                setPadding(5, 14, 5, 14)
                background = fundoCard()
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    miraSelecionada = index + 1
                    selecao.text =
                        "Modelo selecionado: ${index + 1} - $nome"
                    status.text = "$nome selecionada"
                    atualizarGrade(grade)
                }
            }

            val params = android.widget.GridLayout.LayoutParams().apply {
                width = 0
                height = 88
                columnSpec = android.widget.GridLayout.spec(
                    index % 3, 1f
                )
                rowSpec = android.widget.GridLayout.spec(
                    index / 3
                )
                setMargins(4, 4, 4, 4)
            }

            grade.addView(botao, params)
        }

        layout.addView(grade)

        adicionarTitulo(layout, "PERSONALIZAÇÃO")

        adicionarOpcao(layout, "Tamanho da mira", "Configuração em breve")
        adicionarOpcao(layout, "Transparência", "Configuração em breve")
        adicionarOpcao(layout, "Posição da mira", "Configuração em breve")
        adicionarOpcao(layout, "Cor do ponto central", "Configuração em breve")

        val ativar = botaoPrincipal("ATIVAR MIRA") {
            abrirPermissao()
        }

        layout.addView(ativar)

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

    private fun atualizarGrade(grade: android.widget.GridLayout) {
        for (i in 0 until grade.childCount) {
            val item = grade.getChildAt(i) as TextView
            item.setTextColor(
                if (i + 1 == miraSelecionada) verde else branco
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

    private fun adicionarOpcao(
        layout: LinearLayout,
        nome: String,
        detalhe: String
    ) {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = fundoCard()
            setPadding(15, 13, 15, 13)
        }

        val titulo = TextView(this).apply {
            text = nome
            textSize = 14f
            setTextColor(branco)
            typeface = Typeface.DEFAULT_BOLD
        }

        val descricao = TextView(this).apply {
            text = detalhe
            textSize = 11f
            setTextColor(Color.LTGRAY)
        }

        item.addView(titulo)
        item.addView(descricao)

        val params = LinearLayout.LayoutParams(
            -1, -2
        )
        params.setMargins(0, 4, 0, 4)

        layout.addView(item, params)
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

            val params = LinearLayout.LayoutParams(
                -1, -2
            )
            params.setMargins(0, 25, 0, 0)
            layoutParams = params
        }
    }

    private fun fundoCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.rgb(24, 29, 36))
            setCornerRadius(16f)
            setStroke(1, Color.rgb(48, 65, 60))
        }
    }

    private fun abrirPermissao() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            Toast.makeText(
                this,
                "Permissão concedida! A mira flutuante será integrada na próxima etapa.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
