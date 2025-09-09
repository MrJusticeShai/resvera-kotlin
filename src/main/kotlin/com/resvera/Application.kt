package com.resvera

import com.resvera.api.MainVerticle
import io.vertx.core.Vertx

fun main() {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(MainVerticle())
}
