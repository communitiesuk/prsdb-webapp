package uk.gov.communities.prsdb.webapp.models.viewModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createProperty
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createPropertyOwnership

class PropertyDetailsViewModelTests {
    @Test
    fun `Property details contains the property address and uprn if available`() {
        // Arrange
        val expectedUprn = 1234.toLong()
        val address = createAddress(uprn = expectedUprn)
        val property = createProperty(address = address)
        val propertyOwnership = createPropertyOwnership(property = property)

        // Act
        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        // Assert
        val uprn =
            viewModel.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.uprn" }
                .fieldValue

        assertEquals(address.singleLineAddress, viewModel.address)
        assertEquals(expectedUprn.toString(), uprn)
    }

    @Test
    fun `Property details are in the correct order`() {
        val propertyOwnership =
            createPropertyOwnership(
                property =
                    createProperty(
                        address = createAddress(uprn = 1234.toLong()),
                    ),
                currentNumTenants = 2,
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        val headerList = viewModel.propertyRecord.map { it.fieldHeading }

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.registrationDate",
                "propertyDetails.propertyRecord.registrationNumber",
                "propertyDetails.propertyRecord.address",
                "propertyDetails.propertyRecord.uprn",
                "propertyDetails.propertyRecord.localAuthority",
                "propertyDetails.propertyRecord.propertyType",
                "propertyDetails.propertyRecord.ownershipType",
                "propertyDetails.propertyRecord.licensingType",
                "propertyDetails.propertyRecord.occupied",
                "propertyDetails.propertyRecord.numberOfHouseholds",
                "propertyDetails.propertyRecord.numberOfPeople",
                "propertyDetails.propertyRecord.landlordType",
            )

        assertEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `Property details hides null uprn if hideNullUprn is true`() {
        val propertyOwnership = createPropertyOwnership()

        val viewModel = PropertyDetailsViewModel(propertyOwnership, hideNullUprn = true)

        assertNull(viewModel.propertyRecord.firstOrNull { it.fieldHeading == "propertyDetails.propertyRecord.uprn" })
    }

    @Test
    fun `Property details declares null uprn unavailable if hideNullUprn is false`() {
        val propertyOwnership = createPropertyOwnership()

        val viewModel = PropertyDetailsViewModel(propertyOwnership, hideNullUprn = false)

        val uprnKey =
            viewModel.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.uprn" }
                .fieldValue

        assertEquals("propertyDetails.propertyRecord.uprn.unavailable", uprnKey)
    }
}
