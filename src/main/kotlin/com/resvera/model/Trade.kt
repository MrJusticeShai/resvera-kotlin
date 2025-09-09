package com.resvera.model

import java.math.BigDecimal

data class Trade(
    val side: OrderSide,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val currencyPair: String,
    val lastChange: Long = System.currentTimeMillis()
)
