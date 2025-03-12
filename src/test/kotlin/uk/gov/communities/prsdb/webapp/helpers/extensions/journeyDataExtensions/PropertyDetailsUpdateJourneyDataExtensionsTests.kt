package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getOwnershipTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import kotlin.test.assertEquals

class PropertyDetailsUpdateJourneyDataExtensionsTests {
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        journeyDataBuilder = JourneyDataBuilder(mock(), mock())
    }

    @Test
    fun `getOwnershipTypeUpdateIfPresent returns an ownership type if the corresponding page is in journeyData`() {
        val newOwnershipType = OwnershipType.LEASEHOLD
        val testJourneyData = journeyDataBuilder.withOwnershipTypeUpdate(newOwnershipType).build()

        val ownershipTypeUpdate =
            testJourneyData.getOwnershipTypeUpdateIfPresent()

        assertEquals(newOwnershipType, ownershipTypeUpdate)
    }

    @Test
    fun `getOwnershipTypeUpdateIfPresent returns null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val ownershipTypeUpdate =
            testJourneyData.getOwnershipTypeUpdateIfPresent()

        assertEquals(null, ownershipTypeUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns an integer if the corresponding page is in journeyData`() {
        val newNumberOfPeople = 10
        val testJourneyData = journeyDataBuilder.withNumberOfPeopleUpdate(newNumberOfPeople).build()

        val numberOfPeopleUpdate =
            testJourneyData.getNumberOfPeopleUpdateIfPresent()

        assertEquals(newNumberOfPeople, numberOfPeopleUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val numberOfPeopleUpdate =
            testJourneyData.getNumberOfPeopleUpdateIfPresent()

        assertEquals(null, numberOfPeopleUpdate)
    }
}
