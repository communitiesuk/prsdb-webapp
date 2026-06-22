package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class PropertyOwnershipTests {
    @Test
    fun `isSolelyOwnedBy is true when the landlord is the only owner`() {
        val landlord = MockLandlordData.createLandlord()
        val property = MockLandlordData.createPropertyOwnership(primaryLandlord = landlord)

        assertTrue(property.isSolelyOwnedBy(landlord))
    }

    @Test
    fun `isSolelyOwnedBy is false when the only owner is a different landlord`() {
        val owner = MockLandlordData.createLandlord()
        val otherLandlord = MockLandlordData.createLandlord()
        val property = MockLandlordData.createPropertyOwnership(primaryLandlord = owner)

        assertFalse(property.isSolelyOwnedBy(otherLandlord))
    }

    @Test
    fun `isSolelyOwnedBy is false when the property has multiple owners including the landlord`() {
        val landlord = MockLandlordData.createLandlord()
        val coLandlord = MockLandlordData.createLandlord()
        val property = MockLandlordData.createPropertyOwnership(primaryLandlord = landlord)
        property.addLandlord(coLandlord)

        assertFalse(property.isSolelyOwnedBy(landlord))
    }

    @Test
    fun `removeLandlord removes the given landlord and keeps the remaining owners`() {
        val landlord = MockLandlordData.createLandlord()
        val coLandlord = MockLandlordData.createLandlord()
        val property = MockLandlordData.createPropertyOwnership(primaryLandlord = landlord)
        property.addLandlord(coLandlord)

        property.removeLandlord(landlord)

        assertFalse(property.isSolelyOwnedBy(landlord))
        assertTrue(property.isSolelyOwnedBy(coLandlord))
    }

    @Test
    fun `lastModifiedByLandlord is null by default`() {
        val property = MockLandlordData.createPropertyOwnership()

        assertNull(property.lastModifiedByLandlord)
    }

    @Test
    fun `setLastModifiedBy updates the lastModifiedByLandlord`() {
        val property = MockLandlordData.createPropertyOwnership()
        val modifier = MockLandlordData.createLandlord(name = "Modifier")

        property.setLastModifiedBy(modifier)

        assertEquals(modifier, property.lastModifiedByLandlord)
    }
}
