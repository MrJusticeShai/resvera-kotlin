package com.resvera.model

data class OrderBookSnapshot(
    val asks: List<Order>,
    val bids: List<Order>
)
