package com.example.stockapp.Structures

data class PortfolioItem(
    var ticker: String = "",
    var shareCount: Int = 0,
    var marketValue: Double = 0.0,
    var currentPrice: Double = 0.0,
    var changeAmount: Double = 0.0,
    var changePercentage: Double = 0.0
)