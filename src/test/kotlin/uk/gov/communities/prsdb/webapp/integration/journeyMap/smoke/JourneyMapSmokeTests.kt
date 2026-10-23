package uk.gov.communities.prsdb.webapp.integration.journeyMap.smoke

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapHarness
import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapSettings
import uk.gov.communities.prsdb.webapp.integration.journeyMap.output.JourneyMapGenerator
import uk.gov.communities.prsdb.webapp.integration.journeyMap.registry.JourneyMapRegistry
import kotlin.test.assertTrue

class JourneyMapSmokeTests : JourneyMapHarness() {
    @Test
    fun `every branch of every mapped journey can still be reached`(page: Page) {
        val settings = JourneyMapSettings.fromSystemProperties(screenshots = false)
        val generator = JourneyMapGenerator(this, page, settings)
        val problems = mutableListOf<String>()
        val usedScenarioOptions = mutableSetOf<String>()
        settings.selectedDescriptors().forEach { descriptor ->
            val result = generator.generate(descriptor, writeOutput = false)
            println("[journey-map] ${result.summary}")
            problems += result.problems
            usedScenarioOptions += result.usedScenarioOptions
        }
        if (settings.journeys == null) {
            val allOptions = JourneyMapRegistry.scenarios.flatMap { scenario -> scenario.options.map { "${scenario.name}: ${it.label}" } }
            problems += (allOptions - usedScenarioOptions).map { "Registry scenario option \"$it\" was never used" }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }
}
