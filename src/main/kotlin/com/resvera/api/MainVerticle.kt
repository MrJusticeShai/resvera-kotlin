package com.resvera.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.resvera.api.dto.OrderRequest
import com.resvera.model.Order
import com.resvera.service.OrderBook
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlin.jvm.java

/**
 * MainVerticle sets up the HTTP server with routes for:
 * - JWT authentication
 * - Order book retrieval
 * - Submitting limit orders
 * - Recent trades
 */
class MainVerticle : CoroutineVerticle() {

    private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    private lateinit var jwtAuth: JWTAuth

    // Single in-memory order book instance
    private val orderBook = OrderBook()

    override suspend fun start() {
        val router = Router.router(vertx)

        // --- JWT Auth setup ---
        val secret = System.getenv("JWT_SECRET") ?: "my-secret-key"
        val jwtOptions = JWTAuthOptions(JsonObject().put("secret", secret))
        jwtAuth = JWTAuth.create(vertx, jwtOptions)

        // --- Body parser for POST requests ---
        router.post().handler(BodyHandler.create())

        // --- Login endpoint ---
        router.post("/auth/login").handler { ctx ->
            try {
                val body = ctx.body().asJsonObject()
                val username = body.getString("username")
                val password = body.getString("password")

                if (username == "test" && password == "password") {
                    val token = jwtAuth.generateToken(JsonObject().put("sub", username))
                    ctx.response().putHeader("content-type", "application/json")
                        .end(JsonObject().put("token", token).encode())
                } else {
                    ctx.response().setStatusCode(401).end("Unauthorized: Invalid credentials")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ctx.response().setStatusCode(400).end("Bad Request: Invalid JSON")
            }
        }

        // --- JWT Auth Handler for protected routes ---
        val jwtHandler = JWTAuthHandler.create(jwtAuth)

        // --- Get order book ---
        router.get("/:currencyPair/orderbook")
            .handler(jwtHandler)
            .handler { ctx ->
                val currencyPair = ctx.pathParam("currencyPair")
                val snapshot = orderBook.getOrderBookSnapshot(currencyPair)
                ctx.response().putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(snapshot))
            }

        // --- Submit limit order ---
        router.post("/v1/orders/limit")
            .handler(jwtHandler)
            .handler { ctx ->
                try {
                    val req = mapper.readValue(ctx.body().asString(), OrderRequest::class.java)
                    val newOrder = Order(
                        side = req.side,
                        price = req.price,
                        quantity = req.quantity,
                        currencyPair = req.currencyPair
                    )
                    val trades = orderBook.submitLimitOrder(newOrder)
                    ctx.response().putHeader("content-type", "application/json")
                        .end(Json.encodePrettily(trades))
                } catch (e: Exception) {
                    e.printStackTrace()
                    ctx.response().setStatusCode(400)
                        .end("Bad Request: Invalid JSON format or payload")
                }
            }

        // --- Get recent trades ---
        router.get("/:currencyPair/tradehistory")
            .handler(jwtHandler)
            .handler { ctx ->
                val currencyPair = ctx.pathParam("currencyPair")
                val trades = orderBook.getRecentTrades(currencyPair)
                ctx.response().putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(trades))
            }

        // --- Start HTTP server ---
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
            .onSuccess { server ->
                println("HTTP server started on port ${server.actualPort()}")
            }
            .onFailure { it.printStackTrace() }
    }
}
