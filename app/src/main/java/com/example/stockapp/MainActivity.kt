package com.example.stockapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockapp.Services.APIService
import com.example.stockapp.Services.DatabaseService
import com.example.stockapp.Structures.AutocompleteItem
import com.example.stockapp.Structures.FavoritesItem
import com.example.stockapp.Structures.PortfolioItem
import com.example.stockapp.Util.FavoritesAdapter
import com.example.stockapp.Util.MovementAdapter
import com.example.stockapp.Util.NoFilterAdapter
import com.example.stockapp.Util.PortfolioAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class MainActivity : AppCompatActivity() {
    lateinit var splash: ImageView
    lateinit var progressSpinner: ProgressBar
    lateinit var infoContainer: NestedScrollView

    private lateinit var apiService: APIService
    private lateinit var databaseService: DatabaseService

    private val handler = Handler(Looper.getMainLooper())  // Handler for managing the delay
    private var searchRunnable: Runnable? = null

    private var firstLoad: Boolean = true

    lateinit var date: TextView

    var balance: Double = 0.0
    var totalMarketValue: Double = 0.0
    lateinit var portfolio: MutableList<PortfolioItem>
    private lateinit var portfolioRecyclerView: RecyclerView
    private lateinit var portfolioAdapter: PortfolioAdapter
    lateinit var currentBalanceText: TextView
    lateinit var netWorthText: TextView
    var newStockOrder: MutableList<String> = mutableListOf()
    var previousStockOrder: MutableList<String> = mutableListOf()

    lateinit var favorites: MutableList<FavoritesItem>
    private lateinit var watchlistRecyclerView: RecyclerView
    private lateinit var watchlistAdapter: FavoritesAdapter
    var newFavoritesOrder: MutableList<String> = mutableListOf()
    var previousFavoritesOrder: MutableList<String> = mutableListOf()

    lateinit var finnhubHyperlink: TextView

    private val portfolioDataRefreshCoroutineScope = CoroutineScope(Dispatchers.Main)
    private val watchlistDataRefreshCoroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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

        val toolbar: Toolbar = findViewById(R.id.toolbarMain)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val titleTextView: TextView = toolbar.findViewById(R.id.toolbar_title_main)
        titleTextView.text = "Stocks"

        val searchView: SearchView = findViewById(R.id.search_view)
        val searchIcon: ImageView = findViewById(R.id.search_icon)
        val backIcon: ImageView = findViewById(R.id.back_icon_main)

        searchIcon.setOnClickListener {
            searchView.visibility = View.VISIBLE
            backIcon.visibility = View.VISIBLE
            searchIcon.visibility = View.GONE
            titleTextView.visibility = View.GONE
            searchView.onActionViewExpanded()
            searchView.requestFocus()
        }

        backIcon.setOnClickListener {
            if(this is MainActivity) {
                searchView.visibility = View.GONE
                backIcon.visibility = View.GONE
                searchIcon.visibility = View.VISIBLE
                titleTextView.visibility = View.VISIBLE
            }
        }

        apiService = APIService.getInstance(this)
        databaseService = DatabaseService.getInstance(this)

        splash = findViewById(R.id.splashImage)
        progressSpinner = findViewById(R.id.spinnerBar)
        infoContainer = findViewById(R.id.mainInfoContainer)

        renderDate()
        refreshPortfolioPrices()
        refreshWatchlistPrices()

        lifecycleScope.launch {
            delay(2500)
            splash.visibility = View.GONE
            if(firstLoad) {
                progressSpinner.visibility = View.VISIBLE
                firstLoad = false
            }
            delay(1500)
            progressSpinner.visibility = View.GONE
            infoContainer.visibility = View.VISIBLE
        }

        finnhubHyperlink = findViewById(R.id.finnhubHyperlink)
        finnhubHyperlink.setOnClickListener {
            val url = "https://www.finnhub.io/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        portfolioDataRefreshCoroutineScope.cancel()
        watchlistDataRefreshCoroutineScope.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //menuInflater.inflate(R.menu.menu_main, menu)
        val searchView: SearchView = findViewById(R.id.search_view)
        setupSearchView(searchView)
        return true
    }

    @SuppressLint("RestrictedApi")
    private fun setupSearchView(searchView: SearchView) {
        val suggestionsAdapter = NoFilterAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        val autoCompleteTextView = searchView.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)

        autoCompleteTextView.setAdapter(suggestionsAdapter)
        autoCompleteTextView.threshold = 1

        autoCompleteTextView.setOnItemClickListener { adapterView, view, position, id ->
            val selectedSuggestion = adapterView.getItemAtPosition(position) as String
            val ticker = selectedSuggestion.split("\\s+".toRegex())[0]

            val intent = Intent(this@MainActivity, CompanyActivity::class.java)
            intent.putExtra("ticker", ticker)
            startActivity(intent)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    searchRunnable?.let { runnable -> handler.removeCallbacks(runnable) }
                    searchRunnable = Runnable {
                        suggestionsAdapter.clear()
                        if (newText.isNotEmpty()) {
                            fetchSuggestions(newText) { suggestions ->
                                runOnUiThread {
                                    suggestionsAdapter.clear()
                                    suggestionsAdapter.addAll(suggestions)
                                    suggestionsAdapter.notifyDataSetChanged()
                                    if (!autoCompleteTextView.isPopupShowing) {
                                        autoCompleteTextView.showDropDown()
                                    }
                                }
                            }
                        }
                    }
                    handler.postDelayed(searchRunnable!!, 350)
                    return true
                }
                return false
            }
        })
    }

    fun fetchSuggestions(query: String, callback: (List<String>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val suggestions = AutocompleteItem().retrieveAutocompleteData(apiService, query)
            callback(suggestions)
        }
    }


    fun roundToTwoDecimalPlaces(value: Double): Double {
        val formatter = DecimalFormat("#.00")
        return formatter.format(value).toDouble()
    }

    fun getCurrentDateFormatted(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())
        return currentDate.format(formatter)
    }

    fun getPortfolio() = CoroutineScope(Dispatchers.IO).launch {
        try {
            balance = databaseService.fetchBalance()!!.getJSONObject(0).getDouble("balance")
            totalMarketValue = 0.0
            val portfolioStocks = databaseService.fetchPortfolio() ?: throw IllegalStateException("Portfolio was null")
            val latestDeferredResults = mutableListOf<Deferred<JSONObject>>()
            val stockDeferredResults = mutableListOf<Deferred<JSONArray>>()

            val tempPortfolio = MutableList(portfolioStocks.length()) { PortfolioItem() }

            newStockOrder.clear()
            for (n in 0 until portfolioStocks.length()) {
                val ticker = portfolioStocks[n].toString()
                newStockOrder.add(ticker)
                tempPortfolio[n].ticker = ticker
                // Start both API calls in parallel
                val latestData = async { apiService.fetchCompanyLatest(ticker) as JSONObject}
                val stockData = async { databaseService.fetchStockPortfolio(ticker) as JSONArray}

                latestDeferredResults.add(latestData)
                stockDeferredResults.add(stockData)
            }
            latestDeferredResults.awaitAll()
            stockDeferredResults.awaitAll()

            var i = 0
            latestDeferredResults.forEach { deferred ->
                val result = deferred.await()
                tempPortfolio[i].currentPrice = roundToTwoDecimalPlaces(result.getDouble("c"))
                tempPortfolio[i].changeAmount = roundToTwoDecimalPlaces(result.getDouble("d"))
                tempPortfolio[i].changePercentage = roundToTwoDecimalPlaces(result.getDouble("dp"))
                i++
            }

            i = 0
            stockDeferredResults.forEach { deferred ->
                val result = deferred.await()
                tempPortfolio[i].shareCount = result.getJSONObject(0).getInt("quantity")
                tempPortfolio[i].changeAmount = (tempPortfolio[i].currentPrice - result.getJSONObject(0).getDouble("costpershare")) * tempPortfolio[i].shareCount.toDouble()
                tempPortfolio[i].changePercentage = (tempPortfolio[i].changeAmount / result.getJSONObject(0).getDouble("total")) * 100.00
                tempPortfolio[i].marketValue = tempPortfolio[i].shareCount * tempPortfolio[i].currentPrice
                totalMarketValue += roundToTwoDecimalPlaces(tempPortfolio[i].marketValue)
                i++
            }

            withContext(Dispatchers.Main) {
                portfolio = tempPortfolio
                renderPortfolio()
            }
        } catch (e: Exception) {
            Log.e("API", "Error fetching portfolio", e)
        }
    }

    fun getFavorites() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val favoriteStocks = databaseService.fetchWatchlist() ?: throw IllegalStateException("Portfolio was null")
            val latestDeferredResults = mutableListOf<Deferred<JSONObject>>()
            val profileDeferredResults = mutableListOf<Deferred<JSONObject>>()

            val tempFavorites = MutableList(favoriteStocks.length()) { FavoritesItem() }

            newFavoritesOrder.clear()
            for (n in 0 until favoriteStocks.length()) {
                val ticker = favoriteStocks[n].toString()
                newFavoritesOrder.add(ticker)
                tempFavorites[n].ticker = ticker
                val latestData = async { apiService.fetchCompanyLatest(ticker) as JSONObject}
                val profileData = async { apiService.fetchCompanyProfile(ticker) as JSONObject}

                latestDeferredResults.add(latestData)
                profileDeferredResults.add(profileData)
            }
            latestDeferredResults.awaitAll()
            profileDeferredResults.awaitAll()

            var i = 0
            latestDeferredResults.forEach { deferred ->
                val result = deferred.await()
                tempFavorites[i].currentPrice = roundToTwoDecimalPlaces(result.getDouble("c"))
                tempFavorites[i].changeAmount = roundToTwoDecimalPlaces(result.getDouble("d"))
                tempFavorites[i].changePercentage = roundToTwoDecimalPlaces(result.getDouble("dp"))
                i++
            }

            i = 0
            profileDeferredResults.forEach { deferred ->
                val result = deferred.await()
                tempFavorites[i].companyName = result.getString("name")
                i++
            }

            withContext(Dispatchers.Main) {
                favorites = tempFavorites
                renderFavorites()
            }
        } catch (e: Exception) {
            Log.e("API", "Error fetching favorites", e)
        }
    }

    fun renderDate() {
        date = findViewById(R.id.dateText)
        date.text = getCurrentDateFormatted()
    }

    fun renderPortfolio() {
        currentBalanceText = findViewById(R.id.currentBalanceValue)
        netWorthText = findViewById(R.id.netWorthValue)

        currentBalanceText.text = "$" + String.format("%.2f", balance)
        netWorthText.text = "$" + String.format("%.2f", balance + totalMarketValue)

        portfolioRecyclerView = findViewById(R.id.portfolioRecyclerView)

        if (portfolioRecyclerView.layoutManager == null) {
            portfolioRecyclerView.layoutManager = LinearLayoutManager(this)
        }

        if (portfolioRecyclerView.adapter == null) {
            portfolioAdapter = PortfolioAdapter(
                portfolio,
                onReorderOperation = { updatedList ->
                    if(updatedList.size == newStockOrder.size) {
                        previousStockOrder = newStockOrder.toMutableList()
                        newStockOrder.clear()
                        updatedList.forEach { stock ->
                            newStockOrder.add(stock.ticker)
                        }
                        if(newStockOrder != previousStockOrder ) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                databaseService.updatePortfolioOrder(newStockOrder)
                            }
                        }
                    }
                }
            )
            portfolioRecyclerView.adapter = portfolioAdapter

            val movementCallback = MovementAdapter(portfolioAdapter, this, ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
            val touchHelper = ItemTouchHelper(movementCallback)
            touchHelper.attachToRecyclerView(portfolioRecyclerView)
        } else {
            (portfolioRecyclerView.adapter as PortfolioAdapter).updateData(portfolio)
        }
    }

    fun renderFavorites() {
        watchlistRecyclerView = findViewById(R.id.favoritesRecyclerView)

        if (watchlistRecyclerView.layoutManager == null) {
            watchlistRecyclerView.layoutManager = LinearLayoutManager(this)
        }

        if (watchlistRecyclerView.adapter == null) {
            watchlistAdapter = FavoritesAdapter(
                favorites,
                onReorderOperation = { updatedList ->
                    if(updatedList.size == newFavoritesOrder.size) {
                        previousFavoritesOrder = newFavoritesOrder.toMutableList()
                        newFavoritesOrder.clear()
                        updatedList.forEach { stock ->
                            newFavoritesOrder.add(stock.ticker)
                        }
                        if(newFavoritesOrder != previousFavoritesOrder ) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                databaseService.updateFavoritesOrder(newFavoritesOrder)
                            }
                        }
                    }
                },
                onSwipeDeleteOperation = { updatedList ->
                    var newList : MutableList<String> = mutableListOf()
                    updatedList.forEach { item -> newList.add(item.ticker) }
                    var removedStock = newFavoritesOrder.filter { it !in newList }.firstOrNull()
                    if(removedStock != null) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            databaseService.removeFromFavorites(removedStock)
                        }
                    }
                }
            )
            watchlistRecyclerView.adapter = watchlistAdapter

            val movementCallback = MovementAdapter(watchlistAdapter, this, ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT)
            val touchHelper = ItemTouchHelper(movementCallback)
            touchHelper.attachToRecyclerView(watchlistRecyclerView)
        } else {
            (watchlistRecyclerView.adapter as FavoritesAdapter).updateData(favorites)
        }
    }

    private fun refreshPortfolioPrices() = portfolioDataRefreshCoroutineScope.launch {
        while (isActive) {
            getPortfolio()
            delay(15000)
        }
    }

    private fun refreshWatchlistPrices() = watchlistDataRefreshCoroutineScope.launch {
        while (isActive) {
            getFavorites()
            delay(15000)
        }
    }
}