package io.github.jhaago.sealdashboard.ui.navigation

import io.github.jhaago.sealdashboard.demo.RoutePoint

/** Original fictional neighbourhood; geometry is independent of route/provider state. */
data class DemoStreetMap(val streets: List<List<RoutePoint>>, val blocks: List<List<RoutePoint>>) {
    companion object {
        private fun line(vararg xy: Float) = xy.toList().chunked(2).map { RoutePoint(it[0], it[1]) }
        val Sample = DemoStreetMap(
            streets = listOf(
                line(0f,.95f,.32f,.74f,.35f,.4f,.67f,.3f,.95f,.1f),
                line(.2f,.1f,.22f,.7f,.35f,.98f), line(.05f,.3f,.42f,.36f,.98f,.45f),
                line(.07f,.55f,.48f,.56f,.88f,.55f), line(.08f,.83f,.9f,.86f),
                line(.42f,.07f,.48f,.92f), line(.72f,.09f,.75f,.94f),
                line(.35f,.4f,.61f,.62f,.94f,.7f)),
            blocks = listOf(
                line(.24f,.14f,.38f,.11f,.39f,.27f,.25f,.3f),
                line(.04f,.35f,.18f,.34f,.19f,.5f,.06f,.5f),
                line(.23f,.37f,.31f,.38f,.3f,.49f,.24f,.52f),
                line(.36f,.38f,.42f,.38f,.44f,.5f,.35f,.5f),
                line(.51f,.08f,.68f,.09f,.68f,.23f,.5f,.26f),
                line(.53f,.35f,.66f,.35f,.68f,.5f,.52f,.5f),
                line(.78f,.18f,.91f,.18f,.91f,.34f,.78f,.3f),
                line(.53f,.6f,.65f,.63f,.67f,.79f,.52f,.79f),
                line(.8f,.5f,.96f,.53f,.95f,.62f,.83f,.59f),
                line(.21f,.8f,.32f,.81f,.35f,.92f,.26f,.91f),
                line(.35f,.65f,.44f,.64f,.45f,.81f,.34f,.8f),
                line(.8f,.77f,.94f,.76f,.94f,.84f,.8f,.83f)))
    }
}
