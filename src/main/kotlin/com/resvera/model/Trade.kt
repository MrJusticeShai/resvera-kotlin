package com.resvera.model

import java.math.BigDecimal

/**
 * Represents a trade that occurs when orders match.
 *
 * @param side The side that initiated the trade (BUY or SELL)
 * @param price Matched price
 * @param quantity Traded quantity
 * @param currencyPair The currency pair
 * @param lastChange Timestamp in milliseconds
 */
data class Trade(
    val side: OrderSide,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val currencyPair: String,
    val lastChange: Long = System.currentTimeMillis()
)
