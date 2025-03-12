package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getIsOccupiedUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getNumberOfHouseholdsUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getOwnershipTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertyDetailsUpdateJourneyDataExtensionsTests {
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        journeyDataBuilder = JourneyDataBuilder(mock())
    }

    @Test
    fun `getOwnershipTypeUpdateIfPresent returns an ownership type if the corresponding page is in journeyData`() {
        val newOwnershipType = OwnershipType.LEASEHOLD
        val testJourneyData = journeyDataBuilder.withOwnershipTypeUpdate(newOwnershipType).build()

        val ownershipTypeUpdate = testJourneyData.getOwnershipTypeUpdateIfPresent()

        assertEquals(newOwnershipType, ownershipTypeUpdate)
    }

    @Test
    fun `getOwnershipTypeUpdateIfPresent returns null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val ownershipTypeUpdate = testJourneyData.getOwnershipTypeUpdateIfPresent()

        assertNull(ownershipTypeUpdate)
    }

    @Test
    fun `getIsOccupiedUpdateIfPresent returns a boolean if the corresponding page is in journeyData`() {
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(true).build()

        val occupancyUpdate = testJourneyData.getIsOccupiedUpdateIfPresent()!!

        assertTrue(occupancyUpdate)
    }

    @Test
    fun `getIsOccupiedUpdateIfPresent returns null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val occupancyUpdate = testJourneyData.getIsOccupiedUpdateIfPresent()

        assertNull(occupancyUpdate)
    }

    @Test
    fun `getNumberOfHouseholdsUpdateIfPresent returns 0 if the occupancy has been updated to false`() {
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(false).build()

        val numberOfHouseholdsUpdate = testJourneyData.getNumberOfHouseholdsUpdateIfPresent()

        assertEquals(0, numberOfHouseholdsUpdate)
    }

    @Test
    fun `getNumberOfHouseholdsUpdateIfPresent returns an integer if the occupancy has been updated to true`() {
        val newNumberOfHouseholds = 3
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(true).withNumberOfHouseholdsUpdate(newNumberOfHouseholds).build()

        val numberOfHouseholdsUpdate = testJourneyData.getNumberOfHouseholdsUpdateIfPresent()

        assertEquals(newNumberOfHouseholds, numberOfHouseholdsUpdate)
    }

    @Test
    fun `getNumberOfHouseholdsUpdateIfPresent returns an integer if the corresponding page is in journeyData`() {
        val newNumberOfHouseholds = 3
        val testJourneyData = journeyDataBuilder.withNumberOfHouseholdsUpdate(newNumberOfHouseholds).build()

        val numberOfHouseholdsUpdate = testJourneyData.getNumberOfHouseholdsUpdateIfPresent()

        assertEquals(newNumberOfHouseholds, numberOfHouseholdsUpdate)
    }

    @Test
    fun `getNumberOfHouseholdsUpdateIfPresent returns null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val numberOfHouseholdsUpdate = testJourneyData.getNumberOfHouseholdsUpdateIfPresent()

        assertNull(numberOfHouseholdsUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns 0 if the occupancy has been updated to false`() {
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(false).build()

        val numberOfPeopleUpdate = testJourneyData.getNumberOfPeopleUpdateIfPresent()

        assertEquals(0, numberOfPeopleUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns an integer if the occupancy has been updated to true`() {
        val newNumberOfPeople = 10
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(true).withNumberOfPeopleUpdate(newNumberOfPeople).build()

        val numberOfPeopleUpdate = testJourneyData.getNumberOfPeopleUpdateIfPresent()

        assertEquals(newNumberOfPeople, numberOfPeopleUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns an integer if the corresponding page is in journeyData`() {
        val newNumberOfPeople = 10
        val testJourneyData = journeyDataBuilder.withNumberOfPeopleUpdate(newNumberOfPeople).build()

        val numberOfPeopleUpdate = testJourneyData.getNumberOfPeopleUpdateIfPresent()

        assertEquals(newNumberOfPeople, numberOfPeopleUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val numberOfPeopleUpdate = testJourneyData.getNumberOfPeopleUpdateIfPresent()

        assertNull(numberOfPeopleUpdate)
    }
}
