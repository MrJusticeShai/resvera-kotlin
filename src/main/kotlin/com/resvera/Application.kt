package com.resvera

import com.resvera.api.MainVerticle
import io.vertx.core.Vertx

/**
 * Entry point for the application.
 *
 * Deploys the [MainVerticle] which wires up the API routes, authentication, and starts the HTTP server.
 */
fun main() {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(MainVerticle())
}
