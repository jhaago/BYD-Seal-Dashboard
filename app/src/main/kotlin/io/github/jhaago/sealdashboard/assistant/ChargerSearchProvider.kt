package io.github.jhaago.sealdashboard.assistant

fun interface ChargerSearchProvider { suspend fun search(routeId: String): List<ChargerOption> }
