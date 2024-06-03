package com.example.stockapp

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockapp.Services.APIService
import com.example.stockapp.Services.DatabaseService
import com.example.stockapp.Util.NewsAdapter
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.Objects

class CompanyActivity : AppCompatActivity() {
    private lateinit var apiService: APIService
    private lateinit var databaseService: DatabaseService
    private var dataFetched = false

    lateinit var progressSpinner: ProgressBar
    lateinit var companyInfoContainer: LinearLayout
    lateinit var hourlyHistoricalWebView: WebView
    lateinit var chartsTabLayout: TabLayout
    lateinit var hourlyChartTab: TabLayout.Tab
    lateinit var historicalChartTab: TabLayout.Tab

    // star watchlist icon
    lateinit var starSymbol: TextView
    var presentInWatchlist: Boolean = false

    // portfolio fields
    lateinit var sharesOwned: TextView
    lateinit var avgShareCost: TextView
    lateinit var totalCost: TextView
    lateinit var portfolioChange: TextView
    lateinit var marketVal: TextView

    // profile fields
    lateinit var profileLogo: ImageView
    lateinit var profileTicker: TextView
    lateinit var profileCompanyName: TextView
    lateinit var profileCurrentPrice: TextView
    lateinit var profileChangeText: TextView
    lateinit var trade: Button
    var shareQuantity: Int = 0

    // stats fields
    lateinit var highPrice: TextView
    lateinit var openPrice: TextView
    lateinit var lowPrice: TextView
    lateinit var closePrice: TextView

    // about fields
    lateinit var ipo: TextView
    lateinit var industry: TextView
    lateinit var webpage: TextView
    lateinit var peerHolder: LinearLayout

    // sentiments fields
    lateinit var tableComapny: TextView
    lateinit var posMSPR: TextView
    lateinit var negMSPR: TextView
    lateinit var totMSPR: TextView
    lateinit var posChange: TextView
    lateinit var negChange: TextView
    lateinit var totChange: TextView
    lateinit var recommendationChartWebView: WebView
    lateinit var earningsChartWebView: WebView

    // news fields
    lateinit var newsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_company)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        apiService = APIService.getInstance(this)
        databaseService = DatabaseService.getInstance(this)

    }

    override fun onResume() {
        super.onResume()
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        val ticker = intent.getStringExtra("ticker")

        val toolbar: Toolbar = findViewById(R.id.toolbarCompany)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val titleTextView: TextView = toolbar.findViewById(R.id.toolbar_title_company)
        titleTextView.text = ticker

        val backButton: ImageView = findViewById(R.id.back_button_company)
        backButton.setOnClickListener {
            onBackPressed()
        }

        progressSpinner = findViewById(R.id.progressBar)
        companyInfoContainer = findViewById(R.id.companyContainer)

        if(!dataFetched) {
            companyInfoContainer.visibility = View.GONE
            fetchAndRenderAllData(ticker!!)
            fetchStockPortfolio(ticker!!, true)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun fetchAndRenderAllData(ticker: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val balanceDataResults = async { databaseService.fetchBalance() as JSONArray }
            val currentWatchlistResults = async { databaseService.fetchWatchlist() as JSONArray}
            val profileDataResults = async { apiService.fetchCompanyProfile(ticker) as JSONObject }
            val latestDataResults = async { apiService.fetchCompanyLatest(ticker) as JSONObject }
            val peerDataResults = async { apiService.fetchCompanyPeers(ticker) as JSONArray }
            val insiderDataResults = async { apiService.fetchCompanyInsider(ticker) as JSONObject }
            val recommendationDataResults = async { apiService.fetchCompanyRecommendation(ticker) as JSONArray }
            val earningsDataResults = async { apiService.fetchCompanyEarnings(ticker) as JSONArray }
            val newsDataResults = async { apiService.fetchCompanyNews(ticker) as JSONArray }
            val currentPortfolioResults = async { databaseService.fetchPortfolio() as JSONArray }

            val balanceData = balanceDataResults.await()
            val watchList = currentWatchlistResults.await()
            val profileData = profileDataResults.await()
            val latestData = latestDataResults.await()
            val peerData = peerDataResults.await()
            val insiderData = insiderDataResults.await()
            val recommendationData = recommendationDataResults.await()
            val earningsData = earningsDataResults.await()
            val newsData = newsDataResults.await()
            val portfolioStocks = currentPortfolioResults.await()

            withContext(Dispatchers.Main) {
                dataFetched = true
                renderStar(watchList, ticker)
                renderProfile(profileData, latestData, portfolioStocks, balanceData)
                renderStats(latestData)
                renderAbout(profileData, peerData)
                renderSentiments(profileData, insiderData)
                renderRecommendationChart(recommendationData)
                renderEarningsChart(earningsData)
                renderNews(newsData)
                renderHourlyAndHistoricalCharts(ticker, latestData)
                progressSpinner.visibility = View.GONE
                companyInfoContainer.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.d("API", "Failed to get all data.")
        }
    }

    fun fetchStockPortfolio(ticker: String, check: Boolean) = CoroutineScope(Dispatchers.IO).launch {
        try {
            var portfolioStocks = JSONArray()
            if(check) {
                val currentPortfolioResults = async { databaseService.fetchPortfolio() as JSONArray }
                portfolioStocks = currentPortfolioResults.await()
            }

            if(!check || portfolioStocks.toString().contains("\"$ticker\"")) {
                val stockResults = async { databaseService.fetchStockPortfolio(ticker) as JSONArray}
                val stockData = stockResults.await()
                renderStock(stockData.getJSONObject(0))
            }
        } catch (e: Exception) {
            Log.d("API", ""+e)
        }
    }

    fun renderStar(watchlist: JSONArray, ticker: String) {
        var list = mutableListOf<String>()
        for (i in 0 until watchlist.length()) {
            list.add(watchlist.getString(i))
        }

        starSymbol = findViewById(R.id.starIcon)
        if(list.contains(ticker)){
            presentInWatchlist = true
            starSymbol.setCompoundDrawablesWithIntrinsicBounds(R.drawable.full_star, 0, 0, 0)
        } else {
            presentInWatchlist = false
            starSymbol.setCompoundDrawablesWithIntrinsicBounds(R.drawable.star_border, 0, 0, 0)
        }

        starSymbol.setOnClickListener {
            val drawableResId = if (presentInWatchlist) {
                presentInWatchlist = false
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = databaseService.removeFromFavorites(ticker)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CompanyActivity, "$ticker is removed from favorites", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.d("API", "Failed to update favorites.")
                    }
                }
                R.drawable.star_border
            } else {
                presentInWatchlist = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = databaseService.addToFavorites(ticker)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CompanyActivity,
                                "$ticker is added to favorites",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.d("API", "Failed to update favorites.")
                    }
                }
                R.drawable.full_star
            }
            starSymbol.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0)
        }
    }

    fun renderStock(stockData: JSONObject) {
        sharesOwned = findViewById(R.id.companyShareCount)
        avgShareCost = findViewById(R.id.companyAvgCost)
        totalCost = findViewById(R.id.companyTotalCost)
        portfolioChange = findViewById(R.id.companyChange)
        marketVal = findViewById(R.id.companyMarketValue)

        sharesOwned.text = stockData.getInt("quantity").toString()
        avgShareCost.text = "$" + String.format("%.2f" ,stockData.getDouble("costpershare"))
        totalCost.text = "$" + String.format("%.2f" ,stockData.getDouble("total"))
        portfolioChange.text = "$" + String.format("%.2f" ,stockData.getDouble("change"))
        marketVal.text = "$" + String.format("%.2f" ,stockData.getDouble("market"))
    }

    fun roundToTwoDecimalPlaces(value: Double): Double {
        val formatter = DecimalFormat("#.00")
        return formatter.format(value).toDouble()
    }

    private fun loadHourlyData(ticker: String, latestData: JSONObject) {
        CoroutineScope(Dispatchers.IO).launch {
            var color: String = if(latestData.getDouble("dp") >= 0) "green" else "red"
            val hourlyDataResults = async { apiService.fetchCompanyHourlyChart(ticker) as JSONObject }
            var hourlyData = hourlyDataResults.await()
            var hourlyChartData = mutableListOf<Pair<Long, Double>>()
            for(i in 0 until hourlyData.getJSONArray("results").length()) {
                var item = hourlyData.getJSONArray("results").getJSONObject(i)
                hourlyChartData.add(item.getLong("t") to item.getDouble("c"))
            }
            withContext(Dispatchers.Main) {
                val jsonData = Gson().toJson(hourlyChartData.map { arrayOf(it.first, it.second) })
                val script = "javascript:initChart(JSON.parse('${jsonData.replace("'", "\\'")}'), '$color', '$ticker');"
                hourlyHistoricalWebView.evaluateJavascript(script, null)
            }
        }
    }

    private fun loadHistoricalData(ticker: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val historicalDataResults = async { apiService.fetchCompanyChart(ticker) as JSONObject }
            var historicalData = historicalDataResults.await()
            var ohlc = mutableListOf<List<Any>>()
            var volume = mutableListOf<List<Any>>()

            for(i in 0 until historicalData.getJSONArray("results").length()) {
                var item = historicalData.getJSONArray("results").getJSONObject(i)
                ohlc.add(listOf(item.getLong("t"), item.getDouble("o"), item.getDouble("h"), item.getDouble("l"), item.getDouble("c")))
                volume.add(listOf(item.getLong("t"), item.getDouble("v")))
            }
            withContext(Dispatchers.Main) {
                val gson = Gson()
                val ohlcJson = gson.toJson(ohlc)
                val volumeJson = gson.toJson(volume)
                val script = "javascript:initChart(JSON.parse('${ohlcJson.replace("'", "\\'")}'), JSON.parse('${volumeJson.replace("'", "\\'")}'), '$ticker');"
                hourlyHistoricalWebView.evaluateJavascript(script, null)
            }
        }
    }


    fun renderProfile(profileData: JSONObject, latestData: JSONObject, portfolioStocks: JSONArray, balanceData: JSONArray) {
        profileLogo = findViewById(R.id.companyLogo)
        profileTicker = findViewById(R.id.profileTicker)
        profileCompanyName = findViewById(R.id.profileCompanyName)
        profileCurrentPrice = findViewById(R.id.profileCurrentPrice)
        profileChangeText = findViewById(R.id.profileChangeText)
        trade = findViewById(R.id.tradeButton)

        val currentPrice = String.format("%.2f",latestData.getDouble("c"))
        val changeValue = String.format("%.2f",latestData.getDouble("d"))
        val changePercent = String.format("%.2f",latestData.getDouble("dp"))

        val balance = balanceData.getJSONObject(0).getDouble("balance")

        Picasso.get().load(profileData.getString("logo"))
            .resize(1024, 768)
            .centerInside()
            .into(profileLogo)

        profileTicker.text = profileData.getString("ticker")
        profileCompanyName.text = profileData.getString("name")
        profileCurrentPrice.text = "$" + currentPrice
        if(latestData.getDouble("dp") < 0.0) {
            profileChangeText.setTextColor(getColor(R.color.red))
            profileChangeText.text = "$$changeValue ($changePercent%)"
            val downwardTrend = AppCompatResources.getDrawable(this, R.drawable.trending_down)
            downwardTrend?.setBounds(0, 0, downwardTrend.intrinsicWidth, downwardTrend.intrinsicHeight)
            profileChangeText.setCompoundDrawables(downwardTrend, null, null, null)
            profileChangeText.compoundDrawablePadding = 8
        } else if (latestData.getDouble("dp") >= 0.0) {
            profileChangeText.setTextColor(getColor(R.color.green))
            profileChangeText.text = "$$changeValue ($changePercent%)"
            val upwardTrend = AppCompatResources.getDrawable(this, R.drawable.trending_up)
            upwardTrend?.setBounds(0, 0, upwardTrend.intrinsicWidth, upwardTrend.intrinsicHeight)
            profileChangeText.setCompoundDrawables(upwardTrend, null, null, null)
            profileChangeText.compoundDrawablePadding = 8
        }

        trade.setOnClickListener {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.trade_dialog)

            val title = dialog.findViewById<TextView>(R.id.tradeDialogTitle)
            val currentInputShareCount = dialog.findViewById<TextView>(R.id.tradeDialogShareCount)
            val currentSharePrice = dialog.findViewById<TextView>(R.id.tradeDialogCurrentPrice)
            val quantityInput = dialog.findViewById<EditText>(R.id.tradeDialogShareInput)
            val calculatedCost = dialog.findViewById<TextView>(R.id.tradeDialogTotal)
            val balanceStatement = dialog.findViewById<TextView>(R.id.tradeDialogBalance)
            val buyButton = dialog.findViewById<Button>(R.id.tradeDialogBuyButton)
            val sellButton = dialog.findViewById<Button>(R.id.tradeDialogSellButton)

            title.text = "Trade ${profileCompanyName.text} shares"
            currentSharePrice.text = "* $" + currentPrice
            balanceStatement.text = "$$balance to buy ${profileTicker.text.toString()}"

            quantityInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(s.toString().isNotEmpty()) {
                        val quantity = s.toString().toInt()
                        shareQuantity = quantity
                        val pricePerShare = profileCurrentPrice.text.toString().substring(1).toDouble()
                        val cost = quantity * pricePerShare
                        currentInputShareCount.text = "${String.format("%.1f", s.toString().toDouble())}"
                        calculatedCost.text = "${String.format("%.2f", cost)}"
                    } else {
                        currentInputShareCount.text = "0"
                        calculatedCost.text = "$0.00"
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            buyButton.setOnClickListener {
                val ticker = profileTicker.text.toString()

                if (shareQuantity == null || shareQuantity <= 0) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if(portfolioStocks.toString().contains("\"$ticker\"")) {
                    lifecycleScope.launch {
                        val stockData = databaseService.fetchStockPortfolio(ticker)

                        if (stockData != null) {
                            var currentStockData = stockData.getJSONObject(0)
                            var newStockData = JSONObject()
                            newStockData.put("name", "portfolio-stock")
                            newStockData.put("ticker", ticker)
                            newStockData.put("company", profileCompanyName.text)

                            val existingQuantity = currentStockData.getDouble("quantity")
                            val existingCostPerShare = currentStockData.getDouble("costpershare")
                            val existingTotal = currentStockData.getDouble("total")
                            val currentPrice = currentStockData.getDouble("current")

                            val newQuantity = existingQuantity + shareQuantity
                            val newTotal = existingTotal + (shareQuantity * currentPrice)
                            val newCostPerShare = (existingCostPerShare * existingQuantity + shareQuantity * currentPrice) / newQuantity
                            var newBalance = balance.minus(shareQuantity.toDouble() * currentPrice)

                            if (newBalance != null) {
                                if(newBalance < 0.0) {
                                    Toast.makeText(this@CompanyActivity, "Not enough money to buy", Toast.LENGTH_LONG).show()
                                } else {
                                    newStockData.put("quantity", String.format("%.2f", newQuantity))
                                    newStockData.put("costpershare", String.format("%.2f", newCostPerShare))
                                    newStockData.put("total", String.format("%.2f", newTotal))
                                    newStockData.put("current", String.format("%.2f", currentPrice))
                                    newStockData.put("change", String.format("%.2f", currentPrice - newCostPerShare))
                                    newStockData.put("market", String.format("%.2f", newQuantity * currentPrice))
                                    newBalance = String.format("%.2f", newBalance).toDouble()

                                    val result = databaseService.updatePortfolioBuy(ticker, newStockData)
                                    val balUpdateRes = databaseService.updateBalance(newBalance)
                                    if(result != null) {
                                        Log.d("API", "Completion Dialog")
                                        dialog.dismiss()
                                        completionDialogSetup("BUY", ticker, shareQuantity)
                                        renderStock(newStockData)
                                    }
                                }
                            }
                        } else {
                            Log.e("API", "Failed to fetch stock data for $ticker")
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        val newStockData = JSONObject()

                        var newBalance = balance.minus(shareQuantity.toDouble() * currentPrice.toDouble())

                        if(newBalance < 0.0) {
                            Toast.makeText(this@CompanyActivity, "Not enough money to buy", Toast.LENGTH_LONG).show()
                        } else {
                            val newQuantity = shareQuantity.toDouble()
                            val newTotal = (shareQuantity * currentPrice.toDouble())
                            val newCostPerShare = currentPrice.toDouble()
                            var newBalance = balance.minus(shareQuantity.toDouble() * currentPrice.toDouble())

                            newStockData.put("name", "portfolio-stock")
                            newStockData.put("ticker", ticker)
                            newStockData.put("company", profileCompanyName.text.toString())
                            newStockData.put("quantity", String.format("%.2f", newQuantity))
                            newStockData.put("costpershare", String.format("%.2f", newCostPerShare))
                            newStockData.put("total", String.format("%.2f", newTotal))
                            newStockData.put("current", String.format("%.2f", newCostPerShare))
                            newStockData.put("change", "0.00")
                            newStockData.put("market", String.format("%.2f", newQuantity * newCostPerShare))
                            newBalance = String.format("%.2f", newBalance).toDouble()

                            val result1 = databaseService.insertPortfolioBuy(newStockData)
                            val result2 = databaseService.updatePortfolio("ADD", ticker)
                            val balUpdateRes = databaseService.updateBalance(newBalance)
                            if (result1 != null && result2 != null) {
                                Log.d("API", "Completion Dialog")
                                dialog.dismiss()
                                completionDialogSetup("BUY", ticker, shareQuantity)
                                renderStock(newStockData)
                            } else {
                                Log.e("API", "Failed to add new stock to portfolio for $ticker")
                            }
                        }
                    }
                }
            }

            sellButton.setOnClickListener {
                val ticker = profileTicker.text.toString()

                if (shareQuantity == null || shareQuantity <= 0) {
                    Toast.makeText(this, "Cannot sell non-positive shares", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if(portfolioStocks.toString().contains("\"$ticker\"")) {
                    lifecycleScope.launch {
                        val stockData = databaseService.fetchStockPortfolio(ticker)

                        if (stockData != null) {
                            var currentStockData = stockData.getJSONObject(0)
                            var newStockData = JSONObject()

                            val existingQuantity = currentStockData.getInt("quantity")

                            if(existingQuantity - shareQuantity == 0) {
                                var newBalance = balance + currentPrice.toDouble()

                                newStockData.put("quantity", "0.00")
                                newStockData.put("costpershare", "0.00")
                                newStockData.put("total", "0.00")
                                newStockData.put("change", "0.00")
                                newStockData.put("market", "0.00")
                                newBalance = String.format("%.2f", newBalance).toDouble()

                                val result1 = databaseService.sellWholePortfolioStock(ticker)
                                val result2 = databaseService.updatePortfolio("REMOVE", ticker)
                                val balUpdateRes = databaseService.updateBalance(newBalance)

                                if(result1 != null && result2 != null) {
                                    dialog.dismiss()
                                    completionDialogSetup("SELL", ticker, shareQuantity)
                                    renderStock(newStockData)
                                }
                            } else if(existingQuantity - shareQuantity < 0) {
                                Toast.makeText(this@CompanyActivity, "Not enough shares to sell", Toast.LENGTH_LONG).show()
                            } else {
                                val existingQuantity = currentStockData.getDouble("quantity")
                                val existingCostPerShare = currentStockData.getDouble("costpershare")
                                val existingTotal = currentStockData.getDouble("total")
                                val currentPrice = currentStockData.getDouble("current")
                                val currentBalance = balance // Assuming balance is a double

                                val newQuantity = existingQuantity - shareQuantity
                                val newTotal = existingTotal - (shareQuantity * existingCostPerShare)
                                val newCostPerShare = if (newQuantity > 0) newTotal / newQuantity else 0.00
                                val newCurrentPrice = currentPrice
                                val newChange = newCurrentPrice - newCostPerShare
                                val newMarket = newQuantity * newCurrentPrice
                                var newBalance = currentBalance + (currentPrice * shareQuantity)
                                newBalance = String.format("%.2f", newBalance).toDouble()

                                newStockData.put("quantity", String.format("%.2f", newQuantity))
                                newStockData.put("costpershare", String.format("%.2f", newCostPerShare))
                                newStockData.put("total", String.format("%.2f", newTotal))
                                newStockData.put("current", String.format("%.2f", newCurrentPrice))
                                newStockData.put("change", String.format("%.2f", newChange))
                                newStockData.put("market", String.format("%.2f", newMarket))

                                val result = databaseService.sellPortfolioStockPartial(ticker, newStockData)
                                val balUpdateRes = databaseService.updateBalance(newBalance)
                                if(result != null) {
                                    dialog.dismiss()
                                    completionDialogSetup("SELL", ticker, shareQuantity)
                                    renderStock(newStockData)
                                }
                            }
                        } else {
                            Log.e("API", "Failed to fetch stock data for $ticker")
                        }
                    }
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(1350, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            dialog.show()
        }

    }

    fun completionDialogSetup(dialogType: String, ticker: String, shareQuantity: Int) {
        val completionDialog = Dialog(this@CompanyActivity)
        completionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        completionDialog.setContentView(R.layout.trade_complete_dialog)

        val statement = completionDialog.findViewById<TextView>(R.id.tradeCompleteDialogTradeStatement)
        val doneButton = completionDialog.findViewById<Button>(R.id.tradeCompleteDialogDone)

        if(dialogType == "BUY") {
            val shareText = if (shareQuantity == 1) "share" else "shares"
            statement.text = "You have successfully bought $shareQuantity $shareText of $ticker."
        } else if(dialogType == "SELL") {
            val shareText = if (shareQuantity == 1) "share" else "shares"
            statement.text = "You have successfully sold $shareQuantity $shareText of $ticker"
        }

        doneButton.setOnClickListener {
            completionDialog.dismiss()
        }

        completionDialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(1350, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        completionDialog.show()
    }


    fun renderHourlyAndHistoricalCharts(ticker: String, latestData: JSONObject) {
        chartsTabLayout = findViewById(R.id.chartTabs)

        hourlyHistoricalWebView = findViewById(R.id.hourlyHistoricalChartWebView)
        hourlyHistoricalWebView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (url.contains("hourly-chart.html")) {
                        loadHourlyData(ticker, latestData)
                    } else if(url.contains("historical-chart.html")) {
                        loadHistoricalData(ticker)
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d("API", "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}")
                    }
                    return true
                }
            }
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true // If you use localStorage or session
                setSupportMultipleWindows(true)
                setWebContentsDebuggingEnabled(true)
            }
            loadUrl("file:///android_asset/hourly-chart.html")
        }

        if(chartsTabLayout.tabCount == 0) {
            hourlyChartTab = chartsTabLayout.newTab()
            hourlyChartTab.icon = ContextCompat.getDrawable(this, R.drawable.chart_hour)
            chartsTabLayout.addTab(hourlyChartTab)

            historicalChartTab = chartsTabLayout.newTab()
            historicalChartTab.icon = ContextCompat.getDrawable(this, R.drawable.chart_historical)
            chartsTabLayout.addTab(historicalChartTab)
        }

        chartsTabLayout.getTabAt(0)?.icon?.setTint(ContextCompat.getColor(this@CompanyActivity, R.color.selected_tab))
        chartsTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this@CompanyActivity, R.color.selected_tab))
        chartsTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.icon?.setTint(ContextCompat.getColor(this@CompanyActivity, R.color.selected_tab))
                chartsTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this@CompanyActivity, R.color.selected_tab))
                when (tab.position) {
                    0 -> hourlyHistoricalWebView.loadUrl("file:///android_asset/hourly-chart.html")
                    1 -> hourlyHistoricalWebView.loadUrl("file:///android_asset/historical-chart.html")
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    fun renderStats(latestData: JSONObject) {
        openPrice = findViewById(R.id.companyOpenPrice)
        closePrice = findViewById(R.id.companyClosePrice)
        highPrice = findViewById(R.id.companyHighPrice)
        lowPrice = findViewById(R.id.companyLowPrice)

        openPrice.text = "$" + roundToTwoDecimalPlaces(latestData.getDouble("o"))
        highPrice.text = "$" + roundToTwoDecimalPlaces(latestData.getDouble("h"))
        lowPrice.text = "$" + roundToTwoDecimalPlaces(latestData.getDouble("l"))
        closePrice.text = "$" + roundToTwoDecimalPlaces(latestData.getDouble("pc"))
    }

    fun renderAbout(profileData: JSONObject, peerData: JSONArray) {
        ipo = findViewById(R.id.companyIPODate)
        industry = findViewById(R.id.companyIndustry)
        webpage = findViewById(R.id.companyWebpage)
        peerHolder = findViewById(R.id.peersLayout)

        ipo.text = profileData.getString("ipo")
        industry.text = profileData.getString("finnhubIndustry")
        webpage.text = Html.fromHtml("<u>" + profileData.getString("weburl") + "</u>", Html.FROM_HTML_MODE_LEGACY)
        webpage.setOnClickListener {
            val url = webpage.text.toString()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        for(i in 0 until peerData.length()) {
            var peer = peerData.getString(i)
            val textView = TextView(this).apply {
                text = Html.fromHtml("<u>$peer</u>", Html.FROM_HTML_MODE_LEGACY)
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.selected_tab))
                setOnClickListener {
                    val intent = Intent(context, CompanyActivity::class.java)
                    intent.putExtra("ticker", peer)
                    context.startActivity(intent)
                }
            }

            // Add a comma unless it's the last item
            val textViewWithComma = TextView(this).apply {
                text = Html.fromHtml("<u>$peer</u>, ", Html.FROM_HTML_MODE_LEGACY)
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.selected_tab))
                setOnClickListener {
                    val intent = Intent(context, CompanyActivity::class.java)
                    intent.putExtra("ticker", peer)
                    context.startActivity(intent)
                }
            }

            if (i == peerData.length() - 1) {
                peerHolder.addView(textView)  // Add without comma for the last item
            } else {
                peerHolder.addView(textViewWithComma)  // Add with comma
            }

        }
    }

    fun renderSentiments(profileData: JSONObject, insiderData: JSONObject) {
        tableComapny = findViewById(R.id.tableCompanyName)
        posMSPR = findViewById(R.id.tablePositiveMsrp)
        negMSPR = findViewById(R.id.tableNegativeMsrp)
        totMSPR = findViewById(R.id.tableTotalMsrp)
        posChange = findViewById(R.id.tablePositiveChange)
        negChange = findViewById(R.id.tableNegativeChange)
        totChange = findViewById(R.id.tableTotalChange)

        val mspr = mutableMapOf("total" to 0.0, "positive" to 0.0, "negative" to 0.0)
        val change = mutableMapOf("total" to 0.0, "positive" to 0.0, "negative" to 0.0)

        for (i in 0 until insiderData.getJSONArray("data").length()) {
            val item = insiderData.getJSONArray("data").getJSONObject(i)

            val msprValue = item.getDouble("mspr")
            if (msprValue < 0.0) {
                mspr["negative"] = (mspr["negative"] ?: 0.0) + msprValue
            } else {
                mspr["positive"] = (mspr["positive"] ?: 0.0) + msprValue
            }

            val changeValue = item.getDouble("change")
            if (changeValue < 0.0) {
                change["negative"] = (change["negative"] ?: 0.0) + changeValue
            } else {
                change["positive"] = (change["positive"] ?: 0.0) + changeValue
            }
        }
        mspr["negative"] = roundToTwoDecimalPlaces(mspr["negative"] ?: 0.0)
        mspr["positive"] = roundToTwoDecimalPlaces(mspr["positive"] ?: 0.0)
        mspr["total"] = roundToTwoDecimalPlaces((mspr["positive"] ?: 0.0) + (mspr["negative"] ?: 0.0))
        change["negative"] = roundToTwoDecimalPlaces(change["negative"] ?: 0.0)
        change["positive"] = roundToTwoDecimalPlaces(change["positive"] ?: 0.0)
        change["total"] = roundToTwoDecimalPlaces((change["positive"] ?: 0.0) + (change["negative"] ?: 0.0))

        tableComapny.text = profileData.getString("name")
        posMSPR.text = mspr["positive"].toString()
        negMSPR.text = mspr["negative"].toString()
        totMSPR.text = mspr["total"].toString()
        posChange.text = change["positive"].toString()
        negChange.text = change["negative"].toString()
        totChange.text = change["total"].toString()
    }

    private fun loadRecommendationData(trends: MutableMap<String, MutableList<Any>>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                val jsonData = Gson().toJson(trends)
                Log.d("API", "" + jsonData.replace("'", "\\'"))
                val script = "javascript:initChart(JSON.parse('${jsonData.replace("'", "\\'")}'));"
                recommendationChartWebView.evaluateJavascript(script, null)
            }
        }
    }

    fun renderRecommendationChart(recommendationData: JSONArray) {
        val trends = mutableMapOf<String, MutableList<Any>>(
            "b" to mutableListOf(),
            "h" to mutableListOf(),
            "s" to mutableListOf(),
            "sb" to mutableListOf(),
            "ss" to mutableListOf(),
            "dates" to mutableListOf()
        )

        for (i in 0 until recommendationData.length()) {
            val recommendation = recommendationData.getJSONObject(i)
            trends["b"]?.add(recommendation.getInt("buy"))
            trends["s"]?.add(recommendation.getInt("sell"))
            trends["h"]?.add(recommendation.getInt("hold"))
            trends["sb"]?.add(recommendation.getInt("strongBuy"))
            trends["ss"]?.add(recommendation.getInt("strongSell"))
            trends["dates"]?.add(recommendation.getString("period"))
        }

        recommendationChartWebView = findViewById(R.id.recommendationChartWebView)
        recommendationChartWebView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (url.contains("recommendation-chart.html")) {
                        loadRecommendationData(trends)
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d("API", "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}")
                    }
                    return true
                }
            }
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true // If you use localStorage or session
                setSupportMultipleWindows(true)
                setWebContentsDebuggingEnabled(true)
            }
            loadUrl("file:///android_asset/recommendation-chart.html")
        }
    }

    private fun loadEarningsData(categories: MutableList<String>, estimates: MutableList<Double>, actuals: MutableList<Double>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                val catJson = Gson().toJson(categories)
                val earJson = Gson().toJson(estimates)
                val actJson = Gson().toJson(actuals)
                val script = "javascript:initChart(JSON.parse('${catJson.replace("'", "\\'")}'), JSON.parse('${earJson.replace("'", "\\'")}'), JSON.parse('${actJson.replace("'", "\\'")}'));"
                earningsChartWebView.evaluateJavascript(script, null)
            }
        }
    }

    fun renderEarningsChart(earningsData: JSONArray) {
        val categories = mutableListOf<String>()
        val estimates = mutableListOf<Double>()
        val actuals = mutableListOf<Double>()

        for (i in 0 until earningsData.length()) {
            val earning = earningsData.getJSONObject(i)
            categories.add("${earning.getString("period")}<br>Surprise: ${earning.getDouble("surprise")}")
            estimates.add(earning.getDouble("estimate"))
            actuals.add(earning.getDouble("actual"))
        }

        Log.d("API", "" + categories)

        earningsChartWebView = findViewById(R.id.earningsChartWebView)
        earningsChartWebView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (url.contains("earnings-chart.html")) {
                        loadEarningsData(categories, estimates, actuals)
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d("API", "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}")
                    }
                    return true
                }
            }
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true // If you use localStorage or session
                setSupportMultipleWindows(true)
                setWebContentsDebuggingEnabled(true)
            }
            loadUrl("file:///android_asset/earnings-chart.html")
        }
    }

    fun renderNews(newsData: JSONArray) {
        var newsList = JSONArray()
        var count: Int = 0

        for(i in 0 until newsData.length()) {
            var item = newsData.getJSONObject(i)
            if(item.getString("image").isNotEmpty()) {
                count += 1
                newsList.put(item)
            }
            if(count == 20) {
                break
            }
        }
        newsRecyclerView = findViewById(R.id.newsRecycler)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)
        newsRecyclerView.adapter = NewsAdapter(newsList)
    }
}