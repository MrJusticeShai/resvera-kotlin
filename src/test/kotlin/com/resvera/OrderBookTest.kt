package com.resvera

import com.resvera.model.Order
import com.resvera.model.OrderSide
import com.resvera.service.OrderBook
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderBookTest {

    private val orderBook = OrderBook()

    @Test
    fun `submit buy order with no matching asks`() {
        val order = Order(
            side = OrderSide.BUY,
            price = BigDecimal("50000"),
            quantity = BigDecimal("1.0"),
            currencyPair = "BTCZAR"
        )

        val trades = orderBook.submitLimitOrder(order)
        assertEquals(0, trades.size)

        val snapshot = orderBook.getOrderBookSnapshot("BTCZAR")
        assertEquals(1, snapshot.bids.size)
        assertEquals(BigDecimal("50000"), snapshot.bids[0].price)
    }

    @Test
    fun `submit sell order matches existing buy`() {
        // First, a buy order in the book
        val buyOrder = Order(
            side = OrderSide.BUY,
            price = BigDecimal("50000"),
            quantity = BigDecimal("1.0"),
            currencyPair = "BTCZAR"
        )
        orderBook.submitLimitOrder(buyOrder)

        // Now a sell order that should partially match
        val sellOrder = Order(
            side = OrderSide.SELL,
            price = BigDecimal("49000"),
            quantity = BigDecimal("0.5"),
            currencyPair = "BTCZAR"
        )
        val trades = orderBook.submitLimitOrder(sellOrder)

        // Check trades executed
        assertEquals(1, trades.size)
        assertEquals(BigDecimal("0.5"), trades[0].quantity)
        assertEquals(BigDecimal("50000"), trades[0].price) // trade occurs at buy price

        // Check remaining order book
        val snapshot = orderBook.getOrderBookSnapshot("BTCZAR")
        assertEquals(1, snapshot.bids.size)
        assertEquals(BigDecimal("0.5"), snapshot.bids[0].quantity) // remaining buy
    }

    @Test
    fun `recent trades limit enforcement`() {
        // First, add an opposing side order to generate trades
        val sellOrder = Order(
            side = OrderSide.SELL,
            price = BigDecimal("50000"),
            quantity = BigDecimal("1.0"),
            currencyPair = "BTCZAR"
        )
        orderBook.submitLimitOrder(sellOrder)

        // Submit multiple buy orders to exceed maxTrades
        repeat(150) {
            val buyOrder = Order(
                side = OrderSide.BUY,
                price = BigDecimal("50000"),
                quantity = BigDecimal("0.01"),
                currencyPair = "BTCZAR"
            )
            orderBook.submitLimitOrder(buyOrder)
        }

        val trades = orderBook.getRecentTrades("BTCZAR")
        // Should never exceed 100
        assertEquals(100, trades.size)
    }
}