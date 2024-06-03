package com.example.stockapp.Structures

data class FavoritesItem (
    var ticker: String = "",
    var companyName: String = "",
    var currentPrice: Double = 0.0,
    var changeAmount: Double = 0.0,
    var changePercentage: Double = 0.0
)