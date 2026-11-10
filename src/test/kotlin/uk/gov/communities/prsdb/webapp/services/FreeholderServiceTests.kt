package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class FreeholderServiceTests {
    @Mock
    private lateinit var freeholderRepository: FreeholderRepository

    @Mock
    private lateinit var addressService: AddressService

    @InjectMocks
    private lateinit var freeholderService: FreeholderService

    @Test
    fun `createFreeholders saves a postal address freeholder with its resolved address`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        whenever(addressService.findOrCreateAddress(addressDataModel)).thenReturn(address)

        // Act
        freeholderService.createFreeholders(
            propertyOwnership,
            listOf(
                FreeholderDataModel(
                    detailsKnown = true,
                    name = "Alice Freeholder",
                    address = addressDataModel,
                ),
            ),
        )

        // Assert
        val captor = argumentCaptor<Freeholder>()
        verify(freeholderRepository).save(captor.capture())
        assertEquals("Alice Freeholder", captor.firstValue.name)
        assertNull(captor.firstValue.email)
        assertEquals(address, captor.firstValue.address)
        assertEquals(propertyOwnership, captor.firstValue.propertyOwnership)
        assertTrue(captor.firstValue.detailsKnown())
    }

    @Test
    fun `createFreeholders saves a details-not-known freeholder as an empty row`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        // Act
        freeholderService.createFreeholders(propertyOwnership, listOf(FreeholderDataModel(detailsKnown = false)))

        // Assert
        val captor = argumentCaptor<Freeholder>()
        verify(freeholderRepository).save(captor.capture())
        assertNull(captor.firstValue.name)
        assertNull(captor.firstValue.email)
        assertNull(captor.firstValue.address)
        assertFalse(captor.firstValue.detailsKnown())
        verifyNoInteractions(addressService)
    }

    @Test
    fun `createFreeholders saves an email freeholder without resolving an address`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        // Act
        freeholderService.createFreeholders(
            propertyOwnership,
            listOf(FreeholderDataModel(detailsKnown = true, name = "Bob Freeholder", email = "bob@example.com")),
        )

        // Assert
        val captor = argumentCaptor<Freeholder>()
        verify(freeholderRepository).save(captor.capture())
        assertEquals("Bob Freeholder", captor.firstValue.name)
        assertNull(captor.firstValue.address)
        verifyNoInteractions(addressService)
    }

    @Test
    fun `clearFreeholders deletes every freeholder for the property`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        // Act
        freeholderService.clearFreeholders(propertyOwnership)

        // Assert
        verify(freeholderRepository).deleteByPropertyOwnership(propertyOwnership)
    }

    @Test
    fun `createPlaceholderFreeholder saves an empty row for the property`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        // Act
        freeholderService.createPlaceholderFreeholder(propertyOwnership)

        // Assert
        val captor = argumentCaptor<Freeholder>()
        verify(freeholderRepository).save(captor.capture())
        assertEquals(propertyOwnership, captor.firstValue.propertyOwnership)
        assertFalse(captor.firstValue.detailsKnown())
        verifyNoInteractions(addressService)
    }
}
