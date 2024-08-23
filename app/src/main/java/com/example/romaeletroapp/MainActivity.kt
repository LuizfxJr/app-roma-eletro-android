package com.example.romaeletroapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {
    private val websiteURL = "https://www.gruporomaeletro.com.br/" // Coloque aqui a URL do seu site
    private lateinit var webview: WebView
    private lateinit var mySwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!CheckNetwork.isInternetAvailable(this)) { // Retorna true se tiver sinal de internet
            // Se não tiver internet
            AlertDialog.Builder(this) // Alertar o usuário
                .setTitle("Sem conexão com a internet :(")
                .setMessage("Por favor, verifique se você tem dados móveis ou rede Wifi.")
                .setPositiveButton("Ok") { dialog, which -> finish() }
                .show()
        } else {
            // WebView stuff
            webview = findViewById(R.id.webView)
            mySwipeRefreshLayout = findViewById(R.id.swipeContainer)

            webview.settings.javaScriptEnabled = true
            webview.settings.domStorageEnabled = true
            webview.settings.loadWithOverviewMode = true
            webview.settings.useWideViewPort = true
            webview.overScrollMode = WebView.OVER_SCROLL_NEVER
            webview.webViewClient = WebViewClientDemo()
            webview.loadUrl(websiteURL)

            // Funcionalidade de Deslizar e Atualizar
            mySwipeRefreshLayout.setOnRefreshListener { webview.reload() }
        }
    }

    private inner class WebViewClientDemo : WebViewClient() {
        // Ao clicar em links abrir dentro do app
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            return if (url.startsWith("https://api.whatsapp.com/")) {
                view.context.startActivity(
                    Intent(Intent.ACTION_VIEW, request.url)
                )
                true
            } else {
                view.loadUrl(url)
                true
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            mySwipeRefreshLayout.isRefreshing = false
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            view.loadUrl("about:blank")
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Instabilidade detectada")
                .setMessage("Estamos passando por uma instabilidade. Por favor, tente novamente mais tarde.")
                .setPositiveButton("Ok", null)
                .show()
        }
    }

    // Funcionalidade para o botão voltar
    override fun onBackPressed() {
        super.onBackPressed()
        if (webview.canGoBack()) { // Verifique se WebView pode voltar
            webview.goBack() // Voltar na WebView
        } else { // Se não puder voltar mais...
            AlertDialog.Builder(this) // Alertar o usuário
                .setTitle("Roma Eletro")
                .setMessage("Tem certeza que quer fechar esse app?")
                .setPositiveButton("Fechar") { dialog, which -> finish() }
                .setNegativeButton("Voltar", null)
                .show()
        }
    }
}

object CheckNetwork {
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }
}
