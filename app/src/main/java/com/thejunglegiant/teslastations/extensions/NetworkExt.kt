package com.thejunglegiant.teslastations.extensions

import kotlinx.coroutines.delay
import kotlin.random.Random

suspend inline fun simResponseDelay() =
    delay(Random.nextLong(100, 1000))
