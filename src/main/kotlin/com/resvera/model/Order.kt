package com.resvera.model

import java.math.BigDecimal
import java.util.UUID

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val side: OrderSide,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val currencyPair: String
)
