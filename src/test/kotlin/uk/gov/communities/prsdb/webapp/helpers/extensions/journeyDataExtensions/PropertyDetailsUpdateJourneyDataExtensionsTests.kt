package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getIsOccupiedUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getLicenceNumberIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getLicensingTypeIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getNumberOfHouseholdsUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getOriginalIsOccupied
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getOwnershipTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun `getOriginalIsOccupied returns a boolean if the corresponding page is in original journeyData`() {
        val originalJourneyKey = "original-key"
        val originalJourneyData = journeyDataBuilder.withIsOccupiedUpdate(false).build()
        val testJourneyData = mapOf(originalJourneyKey to originalJourneyData)

        val originalOccupancy = testJourneyData.getOriginalIsOccupied(originalJourneyKey)!!

        assertFalse(originalOccupancy)
    }

    @Test
    fun `getOriginalIsOccupied returns null if the corresponding page is not in original journeyData`() {
        val originalJourneyKey = "original-key"
        val originalJourneyData = journeyDataBuilder.build()
        val testJourneyData = mapOf(originalJourneyKey to originalJourneyData)

        val originalOccupancy = testJourneyData.getOriginalIsOccupied(originalJourneyKey)

        assertNull(originalOccupancy)
    }

    @Test
    fun `getOriginalIsOccupied returns null if the original journeyData is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val occupancyUpdate = testJourneyData.getOriginalIsOccupied("original-key-not-in-journey-data")

        assertNull(occupancyUpdate)
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

    @Test
    fun `getLicensingTypeIfPresent return null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val licensingTypeUpdate = testJourneyData.getLicensingTypeIfPresent()

        assertNull(licensingTypeUpdate)
    }

    @Test
    fun `getLicensingTypeIfPresent return a licence type if corresponding page is in journeyData`() {
        val newLicensingType = LicensingType.SELECTIVE_LICENCE
        val testJourneyData = journeyDataBuilder.withLicensingType(newLicensingType).build()

        val licensingTypeUpdate = testJourneyData.getLicensingTypeIfPresent()

        assertEquals(newLicensingType, licensingTypeUpdate)
    }

    @Test
    fun `getLicenceNumberIfPresent return null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val licenceNumberUpdate = testJourneyData.getLicenceNumberIfPresent("originalJourneyKey")

        assertNull(licenceNumberUpdate)
    }

    @Test
    fun `getLicenceNumberIfPresent return null if the licence type is NO_LICENSING in the journeyData`() {
        val testJourneyData = journeyDataBuilder.withLicensingType(LicensingType.NO_LICENSING).build()

        val licenceNumberUpdate = testJourneyData.getLicenceNumberIfPresent("originalJourneyKey")

        assertNull(licenceNumberUpdate)
    }

    @Test
    fun `getLicensingTypeIfPresent return a licence number if corresponding page is in journeyData`() {
        val newLicenceNumber = "LN123456"
        val testJourneyData = journeyDataBuilder.withLicensingType(LicensingType.SELECTIVE_LICENCE, newLicenceNumber).build()

        val licenceNumberUpdate = testJourneyData.getLicenceNumberIfPresent("originalJourneyKey")

        assertEquals(newLicenceNumber, licenceNumberUpdate)
    }

    @Test
    fun `getLicensingTypeIfPresent return a licence number if licence type is in the original journey data`() {
        val originalJourneyKey = "originalJourneyKey"
        val originalJourneyData =
            mapOf(
                "licensing-type" to mapOf("licensingType" to LicensingType.SELECTIVE_LICENCE),
                "selective-licence" to mapOf("licenceNumber" to "LN00000000"),
            )

        val newLicenceNumber = "LN123456"
        val testJourneyData =
            journeyDataBuilder
                .withLicenceNumber(
                    "selective-licence",
                    newLicenceNumber,
                ).withOriginalData(originalJourneyKey, originalJourneyData)
                .build()

        val licenceNumberUpdate = testJourneyData.getLicenceNumberIfPresent(originalJourneyKey)

        assertEquals(newLicenceNumber, licenceNumberUpdate)
    }
}
