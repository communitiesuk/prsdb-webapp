package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class SuperiorLandlordDataModelTests {
    @Test
    fun `fromSuperiorLandlord carries the database id so an update journey can match the saved row`() {
        // Arrange
        val superiorLandlord =
            SuperiorLandlord(
                MockLandlordData.createPropertyOwnership(),
                name = "Alice Superior",
                email = "alice@example.com",
            )
        ReflectionTestUtils.setField(superiorLandlord, "id", 42L)

        // Act
        val dataModel = SuperiorLandlordDataModel.fromSuperiorLandlord(superiorLandlord)

        // Assert
        assertEquals(42L, dataModel.databaseId)
        assertEquals("Alice Superior", dataModel.name)
        assertEquals("alice@example.com", dataModel.email)
        assertNull(dataModel.address)
        assertTrue(dataModel.detailsKnown)
    }

    @Test
    fun `fromSuperiorLandlord maps a postal address superior landlord`() {
        // Arrange
        val address = Address(AddressDataModel("1 Example Road, EG1 2AB"))
        val superiorLandlord =
            SuperiorLandlord(MockLandlordData.createPropertyOwnership(), name = "Bob Superior", address = address)

        // Act
        val dataModel = SuperiorLandlordDataModel.fromSuperiorLandlord(superiorLandlord)

        // Assert
        assertEquals("1 Example Road, EG1 2AB", dataModel.address?.singleLineAddress)
        assertNull(dataModel.email)
    }

    @Test
    fun `fromSuperiorLandlord maps a no details known superior landlord`() {
        // Arrange
        val superiorLandlord = SuperiorLandlord(MockLandlordData.createPropertyOwnership())

        // Act
        val dataModel = SuperiorLandlordDataModel.fromSuperiorLandlord(superiorLandlord)

        // Assert
        assertFalse(dataModel.detailsKnown)
        assertNull(dataModel.name)
        assertNull(dataModel.email)
        assertNull(dataModel.address)
    }
}
