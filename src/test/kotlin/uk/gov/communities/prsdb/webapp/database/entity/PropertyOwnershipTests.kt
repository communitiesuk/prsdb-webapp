package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class PropertyOwnershipTests {
    @Test
    fun `isSolelyOwnedBy is true when the landlord is the only owner`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))

        assertTrue(property.isSolelyOwnedBy(landlord))
    }

    @Test
    fun `isSolelyOwnedBy is false when the only owner is a different landlord`() {
        val owner = MockLandlordData.createIndividualLandlord()
        val otherLandlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(owner))

        assertFalse(property.isSolelyOwnedBy(otherLandlord))
    }

    @Test
    fun `isSolelyOwnedBy is false when the property has multiple owners including the landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val coLandlord = MockLandlordData.createIndividualLandlord(name = "coLandlord")
        val property = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord, coLandlord))

        assertFalse(property.isSolelyOwnedBy(landlord))
    }

    @Test
    fun `removeLandlord removes the given landlord and keeps the remaining owners`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val coLandlord = MockLandlordData.createIndividualLandlord(name = "coLandlord")
        val property = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord, coLandlord))

        property.removeLandlord(landlord)

        assertFalse(property.landlords.contains(landlord))
        assertTrue(property.landlords.contains(coLandlord))
    }

    @Test
    fun `licenseType is PROVIDE_LATER when the landlord chose to provide licensing details later`() {
        val property =
            MockLandlordData.createPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
                licenseProvideLater = true,
            )

        assertEquals(LicensingType.PROVIDE_LATER, property.licenseType)
    }

    @Test
    fun `licenseType is the stored licence type when a licence is stored and details were not deferred`() {
        val property =
            MockLandlordData.createPropertyOwnership(
                license = License(LicensingType.SELECTIVE_LICENCE, "L1234"),
                licenseProvideLater = false,
            )

        assertEquals(LicensingType.SELECTIVE_LICENCE, property.licenseType)
    }

    @Test
    fun `licenseType is NO_LICENSING when no licence is stored and details were not deferred`() {
        val property =
            MockLandlordData.createPropertyOwnership(
                license = null,
                licenseProvideLater = false,
            )

        assertEquals(LicensingType.NO_LICENSING, property.licenseType)
    }
}
