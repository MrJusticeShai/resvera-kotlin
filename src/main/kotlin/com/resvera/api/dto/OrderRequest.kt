package com.resvera.api.dto


import com.resvera.model.OrderSide
import java.math.BigDecimal

/**
 * DTO for submitting a limit order via API.
 */
data class OrderRequest(
    val side: OrderSide,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val currencyPair: String
)