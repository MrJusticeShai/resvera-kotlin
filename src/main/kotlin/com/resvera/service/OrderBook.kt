package com.resvera.service

import com.resvera.model.Order
import com.resvera.model.OrderBookSnapshot
import com.resvera.model.OrderSide
import com.resvera.model.Trade
import java.math.BigDecimal
import java.util.TreeMap

class OrderBook {

    private val bids: MutableMap<String, TreeMap<BigDecimal, MutableList<Order>>> = mutableMapOf()
    private val asks: MutableMap<String, TreeMap<BigDecimal, MutableList<Order>>> = mutableMapOf()

    private val recentTrades: MutableMap<String, MutableList<Trade>> = mutableMapOf()
    private val maxTrades = 100

    fun getOrderBookSnapshot(currencyPair: String): OrderBookSnapshot {
        val sortedBids = bids[currencyPair]?.values?.flatten()?.sortedByDescending { it.price } ?: emptyList()
        val sortedAsks = asks[currencyPair]?.values?.flatten()?.sortedBy { it.price } ?: emptyList()
        return OrderBookSnapshot(asks = sortedAsks, bids = sortedBids)
    }

    fun getRecentTrades(currencyPair: String): List<Trade> =
        recentTrades[currencyPair]?.toList() ?: emptyList()

    fun submitLimitOrder(newOrder: Order): List<Trade> {
        val trades = mutableListOf<Trade>()
        var remaining = newOrder.quantity
        val pair = newOrder.currencyPair

        val bookBids = bids.getOrPut(pair) { TreeMap(compareByDescending<BigDecimal> { it }) }
        val bookAsks = asks.getOrPut(pair) { TreeMap(compareBy<BigDecimal> { it }) }
        val tradeLog = recentTrades.getOrPut(pair) { mutableListOf() }

        if (newOrder.side == OrderSide.BUY) {
            remaining = matchOrders(newOrder, remaining, bookAsks, trades, tradeLog)
            if (remaining > BigDecimal.ZERO) addOrder(bookBids, newOrder.copy(quantity = remaining))
        } else {
            remaining = matchOrders(newOrder, remaining, bookBids, trades, tradeLog)
            if (remaining > BigDecimal.ZERO) addOrder(bookAsks, newOrder.copy(quantity = remaining))
        }

        while (tradeLog.size > maxTrades) tradeLog.removeLast()
        return trades
    }

    private fun matchOrders(
        order: Order,
        quantity: BigDecimal,
        oppositeBook: TreeMap<BigDecimal, MutableList<Order>>,
        trades: MutableList<Trade>,
        tradeLog: MutableList<Trade>
    ): BigDecimal {
        var remaining = quantity
        val priceLevels = oppositeBook.keys.toList() // snapshot to avoid concurrent modification

        for (price in priceLevels) {
            if ((order.side == OrderSide.BUY && price > order.price) ||
                (order.side == OrderSide.SELL && price < order.price)
            ) break

            val queue = oppositeBook[price] ?: continue
            val toRemove = mutableListOf<Order>()

            for (existing in queue) {
                if (remaining <= BigDecimal.ZERO) break

                val fill = minOf(remaining, existing.quantity)
                val trade = Trade(order.side, price, fill, order.currencyPair)
                trades += trade
                tradeLog.add(0, trade)

                remaining -= fill
                val leftover = existing.quantity - fill
                if (leftover > BigDecimal.ZERO) {
                    val idx = queue.indexOf(existing)
                    queue[idx] = existing.copy(quantity = leftover)
                } else {
                    toRemove += existing
                }
            }

            queue.removeAll(toRemove)
            if (queue.isEmpty()) oppositeBook.remove(price)
            if (remaining <= BigDecimal.ZERO) break
        }

        return remaining
    }

    private fun addOrder(book: TreeMap<BigDecimal, MutableList<Order>>, order: Order) {
        book.getOrPut(order.price) { mutableListOf() }.add(order)
    }
}
