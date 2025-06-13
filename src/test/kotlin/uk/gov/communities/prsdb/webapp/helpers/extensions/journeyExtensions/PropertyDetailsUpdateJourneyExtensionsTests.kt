package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsGroupIdentifier
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getIsOccupiedUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLatestNumberOfHouseholds
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicenceNumberUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicensingTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfHouseholdsUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getOriginalIsOccupied
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getOwnershipTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertyDetailsUpdateJourneyExtensionsTests {
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
    fun `getOwnershipTypeUpdateIfPresent returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val ownershipTypeUpdate = testJourneyData.getOwnershipTypeUpdateIfPresent()

        assertNull(ownershipTypeUpdate)
    }

    @Test
    fun `getOriginalIsOccupied returns a boolean if the corresponding page is in original journeyData`() {
        val originalJourneyKey = "original-key"
        val originalJourneyData = journeyDataBuilder.withIsOccupiedUpdate(false).build()
        val testJourneyData = mapOf(originalJourneyKey to originalJourneyData)

        val originalOccupancy = testJourneyData.getOriginalIsOccupied(UpdatePropertyDetailsGroupIdentifier.Occupancy, originalJourneyKey)!!

        assertFalse(originalOccupancy)
    }

    @Test
    fun `getOriginalIsOccupied returns null if the corresponding page is not in original journeyData`() {
        val originalJourneyKey = "original-key"
        val originalJourneyData = journeyDataBuilder.build()
        val testJourneyData = mapOf(originalJourneyKey to originalJourneyData)

        val originalOccupancy = testJourneyData.getOriginalIsOccupied(UpdatePropertyDetailsGroupIdentifier.Occupancy, originalJourneyKey)

        assertNull(originalOccupancy)
    }

    @Test
    fun `getOriginalIsOccupied returns null if the original journeyData is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val occupancyUpdate =
            testJourneyData.getOriginalIsOccupied(UpdatePropertyDetailsGroupIdentifier.Occupancy, "original-key-not-in-journey-data")

        assertNull(occupancyUpdate)
    }

    @Test
    fun `getIsOccupiedUpdateIfPresent returns a boolean if the corresponding page is in journeyData`() {
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(true).build()

        val occupancyUpdate = testJourneyData.getIsOccupiedUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.Occupancy)!!

        assertTrue(occupancyUpdate)
    }

    @Test
    fun `getIsOccupiedUpdateIfPresent returns null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val occupancyUpdate = testJourneyData.getIsOccupiedUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.Occupancy)

        assertNull(occupancyUpdate)
    }

    @Test
    fun `getNumberOfHouseholdsUpdateIfPresent returns 0 if the occupancy has been updated to false`() {
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(false).build()

        val numberOfHouseholdsUpdate =
            testJourneyData.getNumberOfHouseholdsUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.Occupancy)

        assertEquals(0, numberOfHouseholdsUpdate)
    }

    @Test
    fun `getNumberOfHouseholdsUpdateIfPresent returns an integer if the occupancy has been updated to true`() {
        val newNumberOfHouseholds = 3
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(true).withNumberOfHouseholdsUpdate(newNumberOfHouseholds).build()

        val numberOfHouseholdsUpdate =
            testJourneyData.getNumberOfHouseholdsUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds)

        assertEquals(newNumberOfHouseholds, numberOfHouseholdsUpdate)
    }

    @Test
    fun `getNumberOfHouseholdsUpdateIfPresent returns an integer if the corresponding page is in journeyData`() {
        val newNumberOfHouseholds = 3
        val testJourneyData = journeyDataBuilder.withNumberOfHouseholdsUpdate(newNumberOfHouseholds).build()

        val numberOfHouseholdsUpdate =
            testJourneyData.getNumberOfHouseholdsUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds)

        assertEquals(newNumberOfHouseholds, numberOfHouseholdsUpdate)
    }

    @Test
    fun `getNumberOfHouseholdsUpdateIfPresent returns null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val numberOfHouseholdsUpdate =
            testJourneyData.getNumberOfHouseholdsUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds)

        assertNull(numberOfHouseholdsUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns 0 if the occupancy has been updated to false`() {
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(false).build()

        val numberOfPeopleUpdate =
            testJourneyData.getNumberOfPeopleUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.Occupancy)

        assertEquals(0, numberOfPeopleUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns an integer if the occupancy has been updated to true`() {
        val newNumberOfPeople = 10
        val testJourneyData = journeyDataBuilder.withIsOccupiedUpdate(true).withNumberOfPeopleUpdate(newNumberOfPeople).build()

        val numberOfPeopleUpdate =
            testJourneyData.getNumberOfPeopleUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.NumberOfPeople)

        assertEquals(newNumberOfPeople, numberOfPeopleUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns an integer if the corresponding page is in journeyData`() {
        val newNumberOfPeople = 10
        val testJourneyData = journeyDataBuilder.withNumberOfPeopleUpdate(newNumberOfPeople).build()

        val numberOfPeopleUpdate =
            testJourneyData.getNumberOfPeopleUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.NumberOfPeople)

        assertEquals(newNumberOfPeople, numberOfPeopleUpdate)
    }

    @Test
    fun `getNumberOfPeopleUpdateIfPresent returns null if the corresponding page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val numberOfPeopleUpdate =
            testJourneyData.getNumberOfPeopleUpdateIfPresent(UpdatePropertyDetailsGroupIdentifier.NumberOfPeople)

        assertNull(numberOfPeopleUpdate)
    }

    @Test
    fun `getLatestNumberOfHouseholds returns updated number of households if the corresponding page is in journeyData`() {
        val originalDataKey = "original-data-key"
        val originalNumberOfHouseholds = 2
        val expectedNumberOfHouseholds = 3

        val testJourneyData =
            journeyDataBuilder
                .withNumberOfHouseholdsUpdate(expectedNumberOfHouseholds)
                .withOriginalNumberOfHouseholdsData(originalDataKey, originalNumberOfHouseholds)
                .build()

        val latestNumberOfHouseholds =
            testJourneyData.getLatestNumberOfHouseholds(UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds, originalDataKey)

        assertEquals(expectedNumberOfHouseholds, latestNumberOfHouseholds)
    }

    @Test
    fun `getLatestNumberOfHouseholds returns original number of households if there's no update in journeyData`() {
        val originalDataKey = "original-data-key"
        val expectedNumberOfHouseholds = 2

        val testJourneyData =
            journeyDataBuilder
                .withOriginalNumberOfHouseholdsData(originalDataKey, expectedNumberOfHouseholds)
                .build()

        val latestNumberOfHouseholds =
            testJourneyData.getLatestNumberOfHouseholds(UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds, originalDataKey)

        assertEquals(expectedNumberOfHouseholds, latestNumberOfHouseholds)
    }

    @Test
    fun `getLatestNumberOfHouseholds returns 0 if occupation status has been updated to false in journeyData`() {
        val originalDataKey = "original-data-key"
        val originalNumberOfHouseholds = 2
        val expectedNumberOfHouseholds = 0

        val testJourneyData =
            journeyDataBuilder
                .withIsOccupiedUpdate(false)
                .withOriginalNumberOfHouseholdsData(originalDataKey, originalNumberOfHouseholds)
                .build()

        val latestNumberOfHouseholds =
            testJourneyData.getLatestNumberOfHouseholds(UpdatePropertyDetailsGroupIdentifier.Occupancy, originalDataKey)

        assertEquals(expectedNumberOfHouseholds, latestNumberOfHouseholds)
    }

    @Test
    fun `getLicensingTypeUpdateIfPresent returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val licensingTypeUpdate = testJourneyData.getLicensingTypeUpdateIfPresent()

        assertNull(licensingTypeUpdate)
    }

    @Test
    fun `getLicensingTypeUpdateIfPresent returns a licence type if corresponding page is in journeyData`() {
        val newLicensingType = LicensingType.SELECTIVE_LICENCE
        val testJourneyData = journeyDataBuilder.withLicensingTypeUpdate(newLicensingType).build()

        val licensingTypeUpdate = testJourneyData.getLicensingTypeUpdateIfPresent()

        assertEquals(newLicensingType, licensingTypeUpdate)
    }

    @Test
    fun `getLicenceNumberUpdateIfPresent returns null if the corresponding page is not in journeyData and there is a valid licence type`() {
        val testJourneyData = journeyDataBuilder.withLicensingTypeUpdate(LicensingType.SELECTIVE_LICENCE).build()

        val licenceNumberUpdate = testJourneyData.getLicenceNumberUpdateIfPresent()

        assertNull(licenceNumberUpdate)
    }

    @Test
    fun `getLicenceNumberUpdateIfPresent returns null if the licence type is NO_LICENSING in the journeyData`() {
        val testJourneyData = journeyDataBuilder.withLicensingTypeUpdate(LicensingType.NO_LICENSING).build()

        val licenceNumberUpdate = testJourneyData.getLicenceNumberUpdateIfPresent()

        assertNull(licenceNumberUpdate)
    }

    @Test
    fun `getLicenceNumberUpdateIfPresent returns a licence number if corresponding page is in journeyData`() {
        val newLicenceNumber = "LN123456"
        val testJourneyData = journeyDataBuilder.withLicenceUpdate(LicensingType.SELECTIVE_LICENCE, newLicenceNumber).build()

        val licenceNumberUpdate = testJourneyData.getLicenceNumberUpdateIfPresent()

        assertEquals(newLicenceNumber, licenceNumberUpdate)
    }
}
