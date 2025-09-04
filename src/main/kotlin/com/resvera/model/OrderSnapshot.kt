package com.resvera.model

/**
 * Snapshot of the current order book for a given currency pair.
 *
 * - `asks` sorted ascending by price
 * - `bids` sorted descending by price
 */
data class OrderBookSnapshot(
    val asks: List<Order>,
    val bids: List<Order>
)
