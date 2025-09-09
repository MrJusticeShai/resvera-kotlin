package com.resvera.api.dto

import com.resvera.model.OrderSide
import java.math.BigDecimal

data class OrderRequest(
    val side: OrderSide,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val currencyPair: String
)
