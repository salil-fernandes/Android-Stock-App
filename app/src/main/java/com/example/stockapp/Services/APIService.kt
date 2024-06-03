package com.example.stockapp.Services

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

class APIService private constructor(context: Context) {
    companion object {
        @Volatile
        private var instance: APIService? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: APIService(context).also {
                    instance = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    private val baseUrl = "https://salferns-assignment3.wm.r.appspot.com"

    suspend fun fetchAutocompleteData(keyword: String): JSONArray? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/autocomplete/$keyword"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonArrayRequest)
        continuation.invokeOnCancellation {
            jsonArrayRequest.cancel()
        }
    }

    suspend fun fetchCompanyProfile(ticker: String): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/company-profile/$ticker"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonObjectRequest)
        continuation.invokeOnCancellation {
            jsonObjectRequest.cancel()
        }
    }

    suspend fun fetchCompanyLatest(ticker: String): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/company-latest/$ticker"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonObjectRequest)
        continuation.invokeOnCancellation {
            jsonObjectRequest.cancel()
        }
    }

    suspend fun fetchCompanyHourlyChart(ticker: String): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/company-hourly-chart/$ticker"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonObjectRequest)
        continuation.invokeOnCancellation {
            jsonObjectRequest.cancel()
        }
    }

    suspend fun fetchCompanyPeers(ticker: String): JSONArray? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/company-peers/$ticker"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonArrayRequest)
        continuation.invokeOnCancellation {
            jsonArrayRequest.cancel()
        }
    }

    suspend fun fetchCompanyNews(ticker: String): JSONArray? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/company-news/$ticker"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonArrayRequest)
        continuation.invokeOnCancellation {
            jsonArrayRequest.cancel()
        }
    }

    suspend fun fetchCompanyChart(ticker: String): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/company-chart/$ticker"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonObjectRequest)
        continuation.invokeOnCancellation {
            jsonObjectRequest.cancel()
        }
    }

    suspend fun fetchCompanyRecommendation(ticker: String): JSONArray? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/company-recommendation/$ticker"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonArrayRequest)
        continuation.invokeOnCancellation {
            jsonArrayRequest.cancel()
        }
    }

    suspend fun fetchCompanyEarnings(ticker: String): JSONArray? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/company-earnings/$ticker"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonArrayRequest)
        continuation.invokeOnCancellation {
            jsonArrayRequest.cancel()
        }
    }

    suspend fun fetchCompanyInsider(ticker: String): JSONObject? = suspendCancellableCoroutine { continuation ->
        val url = "$baseUrl/company-insider/$ticker"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                continuation.resume(response)
            },
            { error ->
                Log.e("API", "Error: ${error.toString()}")
                continuation.resume(null)
            }
        )
        requestQueue.add(jsonObjectRequest)
        continuation.invokeOnCancellation {
            jsonObjectRequest.cancel()
        }
    }
}