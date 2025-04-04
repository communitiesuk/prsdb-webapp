package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PropertyComplianceJourneyDataExtensionsTests {
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        journeyDataBuilder = JourneyDataBuilder(mock())
    }

    @Test
    fun `getHasGasSafetyCert returns a boolean if the corresponding page is in journeyData`() {
        val hasGasSafetyCert = true
        val testJourneyData = journeyDataBuilder.withGasSafetyCertStatus(hasGasSafetyCert).build()

        val retrievedHasGasSafetyCert = testJourneyData.getHasGasSafetyCert()

        assertEquals(hasGasSafetyCert, retrievedHasGasSafetyCert)
    }

    @Test
    fun `getHasGasSafetyCert returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasGasSafetyCert = testJourneyData.getHasGasSafetyCert()

        assertNull(retrievedHasGasSafetyCert)
    }
}
