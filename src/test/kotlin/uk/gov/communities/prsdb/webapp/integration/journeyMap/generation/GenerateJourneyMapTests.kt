package uk.gov.communities.prsdb.webapp.integration.journeyMap.generation

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapHarness
import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapSettings
import uk.gov.communities.prsdb.webapp.integration.journeyMap.output.JourneyMapGenerator
import kotlin.test.assertTrue

class GenerateJourneyMapTests : JourneyMapHarness() {
    @Test
    fun `generate journey maps`(page: Page) {
        val settings = JourneyMapSettings.fromSystemProperties()
        val generator = JourneyMapGenerator(this, page, settings)
        val problems = mutableListOf<String>()
        settings.selectedDescriptors().forEach { descriptor ->
            val result = generator.generate(descriptor)
            println("[journey-map] ${result.summary}")
            println("[journey-map] Open ${settings.outputDir.resolve(descriptor.name).resolve("index.html").toAbsolutePath()}")
            if (settings.strict) problems += result.problems
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }
}
