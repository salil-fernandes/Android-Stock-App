package com.example.stockapp.Services

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DatabaseService private constructor(context: Context) {
    companion object {
        @Volatile
        private var instance: DatabaseService? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: DatabaseService(context).also {
                    instance = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    private val baseUrl = "https://salferns-assignment3.wm.r.appspot.com"

    suspend fun fetchBalance(): JSONArray? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/retrieve-balance"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                continuation.resumeWithException(RuntimeException("Failed to fetch portfolio", error))
            }
        )

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun updateBalance(newBalance: Double): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/update-balance/$newBalance"

        val request = JsonObjectRequest(Request.Method.PUT, url, null,
            { response ->
                continuation.resume(response) // Resume the coroutine with the fetched data
            },
            { error ->
                continuation.resumeWithException(RuntimeException("Failed to fetch portfolio", error))
            }
        )

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun fetchPortfolio(): JSONArray? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/retrieve-portfolio"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response) // Resume the coroutine with the fetched data
            },
            { error ->
                continuation.resumeWithException(RuntimeException("Failed to fetch portfolio", error))
            }
        )

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun fetchStockPortfolio(ticker: String): JSONArray? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/retrieve-stock-portfolio/$ticker"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error fetching stock portfolio: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(request)
        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun updatePortfolioOrder(newOrder: MutableList<String>): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/update-portfolio-order"

        val payload = JSONObject()
        val newOrderArray = JSONArray(newOrder)
        payload.put("newPortfolio", newOrderArray)

        val request = object : JsonObjectRequest(Method.PUT, url, payload,
            { response ->
                continuation.resume(response)
            },
            { error ->
                continuation.resumeWithException(RuntimeException("Failed to update portfolio order", error))
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Add the request to the Volley request queue
        requestQueue.add(request)

        // Handle cancellation of the coroutine
        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun fetchWatchlist(): JSONArray? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/retrieve-watchlist"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response) // Resume the coroutine with the fetched data
            },
            { error ->
                continuation.resumeWithException(RuntimeException("Failed to fetch favorites", error))
            }
        )

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun updateFavoritesOrder(newOrder: MutableList<String>): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/update-watchlist-order"

        val payload = JSONObject()
        val newOrderArray = JSONArray(newOrder)
        payload.put("newWatchlist", newOrderArray)

        val request = object : JsonObjectRequest(Method.PUT, url, payload,
            { response ->
                continuation.resume(response)
            },
            { error ->
                continuation.resumeWithException(RuntimeException("Failed to update favorites order", error))
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun removeFromFavorites(ticker: String): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/update-watchlist/REMOVE/$ticker"

        val request = JsonObjectRequest(Request.Method.PUT, url, null,
            { response ->
                continuation.resume(response) // Resume the coroutine with the fetched data
            },
            { error ->
                continuation.resumeWithException(RuntimeException("Failed to update favorites", error))
            }
        )

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }

    }

    suspend fun addToFavorites(ticker: String): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/update-watchlist/ADD/$ticker"

        val request = JsonObjectRequest(Request.Method.PUT, url, null,
            { response ->
                continuation.resume(response) // Resume the coroutine with the fetched data
            },
            { error ->
                continuation.resumeWithException(RuntimeException("Failed to update favorites", error))
            }
        )

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun insertPortfolioBuy(portfolioData: JSONObject): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/portfolio-buy-firsttime"

        val request = object : JsonObjectRequest(Method.POST, url, portfolioData,
            Response.Listener { response ->
                continuation.resume(response)
            },
            Response.ErrorListener { error ->
                continuation.resumeWithException(RuntimeException("Failed to insert new portfolio", error))
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun updatePortfolioBuy(ticker: String, updateData: JSONObject): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/portfolio-buy/$ticker"

        val request = object : JsonObjectRequest(Method.PUT, url, updateData,
            Response.Listener { response ->
                continuation.resume(response)
            },
            Response.ErrorListener { error ->
                continuation.resumeWithException(RuntimeException("Failed to update portfolio for ticker $ticker", error))
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun updatePortfolio(type: String, ticker: String): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/update-portfolio/$type/$ticker"
        val payload = JSONObject()

        val request = object : JsonObjectRequest(Method.PUT, url, payload,
            Response.Listener { response ->
                continuation.resume(response)
            },
            Response.ErrorListener { error ->
                continuation.resumeWithException(RuntimeException("Failed to update portfolio for $ticker", error))
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun sellWholePortfolioStock(ticker: String): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/portfolio-sell-whole-stock/$ticker"

        val request = object : JsonObjectRequest(Method.DELETE, url, null,
            Response.Listener { response ->
                continuation.resume(response)
            },
            Response.ErrorListener { error ->
                continuation.resumeWithException(RuntimeException("Failed to delete stock for ticker $ticker", error))
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

    suspend fun sellPortfolioStockPartial(ticker: String, updateData: JSONObject): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/portfolio-sell/$ticker"

        val request = object : JsonObjectRequest(Method.PUT, url, updateData,
            Response.Listener { response ->
                continuation.resume(response)
            },
            Response.ErrorListener { error ->
                continuation.resumeWithException(RuntimeException("Failed to update portfolio partially for ticker $ticker", error))
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }

}