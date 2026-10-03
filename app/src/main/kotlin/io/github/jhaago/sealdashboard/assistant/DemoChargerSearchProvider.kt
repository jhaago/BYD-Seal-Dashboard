package io.github.jhaago.sealdashboard.assistant

/** Fictional stations, not live search results or statements about actual availability. */
class DemoChargerSearchProvider : ChargerSearchProvider {
    override suspend fun search(routeId: String) = options.toList()
    companion object {
        val options: List<ChargerOption> = listOf(
            ChargerOption("demo-near-1", "Harbour AC · demo", "Type 2", 22.0, 2, ChargerAvailability.UNKNOWN, "DEMO FIXTURE"),
            ChargerOption("demo-fast-2", "Marina rapid · demo", "CCS2", 150.0, 5, ChargerAvailability.UNKNOWN, "DEMO FIXTURE"),
            ChargerOption("demo-fast-3", "Coastal rapid · demo", "CCS2", 250.0, 9, ChargerAvailability.UNKNOWN, "DEMO FIXTURE"),
        )
    }
}
