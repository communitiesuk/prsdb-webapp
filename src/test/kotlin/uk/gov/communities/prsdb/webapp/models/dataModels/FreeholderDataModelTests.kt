package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class FreeholderDataModelTests {
    @Test
    fun `fromFreeholder carries the database id so an update journey can match the saved row`() {
        // Arrange
        val freeholder =
            Freeholder(
                MockLandlordData.createPropertyOwnership(),
                name = "Alice Freeholder",
                email = "alice@example.com",
            )
        ReflectionTestUtils.setField(freeholder, "id", 42L)

        // Act
        val dataModel = FreeholderDataModel.fromFreeholder(freeholder)

        // Assert
        assertEquals(42L, dataModel.databaseId)
        assertEquals("Alice Freeholder", dataModel.name)
        assertEquals("alice@example.com", dataModel.email)
        assertNull(dataModel.address)
        assertTrue(dataModel.detailsKnown)
    }

    @Test
    fun `fromFreeholder maps a postal address freeholder`() {
        // Arrange
        val address = Address(AddressDataModel("1 Example Road, EG1 2AB"))
        val freeholder = Freeholder(MockLandlordData.createPropertyOwnership(), name = "Bob Freeholder", address = address)

        // Act
        val dataModel = FreeholderDataModel.fromFreeholder(freeholder)

        // Assert
        assertEquals("1 Example Road, EG1 2AB", dataModel.address?.singleLineAddress)
        assertNull(dataModel.email)
    }

    @Test
    fun `fromFreeholder maps a no details known freeholder`() {
        // Arrange
        val freeholder = Freeholder(MockLandlordData.createPropertyOwnership())

        // Act
        val dataModel = FreeholderDataModel.fromFreeholder(freeholder)

        // Assert
        assertFalse(dataModel.detailsKnown)
        assertNull(dataModel.name)
        assertNull(dataModel.email)
        assertNull(dataModel.address)
    }
}
