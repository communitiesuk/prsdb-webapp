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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.database.repository.SuperiorLandlordRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class SuperiorLandlordServiceTests {
    @Mock
    private lateinit var superiorLandlordRepository: SuperiorLandlordRepository

    @Mock
    private lateinit var addressService: AddressService

    @InjectMocks
    private lateinit var superiorLandlordService: SuperiorLandlordService

    @Test
    fun `createSuperiorLandlords saves a postal address superior landlord with its resolved address`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        whenever(addressService.findOrCreateAddress(addressDataModel)).thenReturn(address)

        // Act
        superiorLandlordService.createSuperiorLandlords(
            propertyOwnership,
            listOf(
                SuperiorLandlordDataModel(
                    detailsKnown = true,
                    name = "Alice Superior",
                    address = addressDataModel,
                ),
            ),
        )

        // Assert
        val captor = argumentCaptor<SuperiorLandlord>()
        verify(superiorLandlordRepository).save(captor.capture())
        assertEquals("Alice Superior", captor.firstValue.name)
        assertNull(captor.firstValue.email)
        assertEquals(address, captor.firstValue.address)
        assertEquals(propertyOwnership, captor.firstValue.propertyOwnership)
        assertTrue(captor.firstValue.detailsKnown())
    }

    @Test
    fun `createSuperiorLandlords saves a details-not-known superior landlord as an empty row`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        // Act
        superiorLandlordService.createSuperiorLandlords(
            propertyOwnership,
            listOf(SuperiorLandlordDataModel(detailsKnown = false)),
        )

        // Assert
        val captor = argumentCaptor<SuperiorLandlord>()
        verify(superiorLandlordRepository).save(captor.capture())
        assertNull(captor.firstValue.name)
        assertNull(captor.firstValue.email)
        assertNull(captor.firstValue.address)
        assertFalse(captor.firstValue.detailsKnown())
        verifyNoInteractions(addressService)
    }

    @Test
    fun `createSuperiorLandlords saves an email superior landlord without resolving an address`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        // Act
        superiorLandlordService.createSuperiorLandlords(
            propertyOwnership,
            listOf(SuperiorLandlordDataModel(detailsKnown = true, name = "Bob Superior", email = "bob@example.com")),
        )

        // Assert
        val captor = argumentCaptor<SuperiorLandlord>()
        verify(superiorLandlordRepository).save(captor.capture())
        assertEquals("Bob Superior", captor.firstValue.name)
        assertNull(captor.firstValue.address)
        verifyNoInteractions(addressService)
    }

    @Test
    fun `createSuperiorLandlords saves every superior landlord in the list`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        // Act
        superiorLandlordService.createSuperiorLandlords(
            propertyOwnership,
            listOf(
                SuperiorLandlordDataModel(detailsKnown = true, name = "Alice Superior", email = "alice@example.com"),
                SuperiorLandlordDataModel(detailsKnown = true, name = "Bob Superior", email = "bob@example.com"),
                SuperiorLandlordDataModel(detailsKnown = false),
            ),
        )

        // Assert
        val captor = argumentCaptor<SuperiorLandlord>()
        verify(superiorLandlordRepository, times(3)).save(captor.capture())
        assertEquals(listOf("Alice Superior", "Bob Superior", null), captor.allValues.map { it.name })
    }

    @Test
    fun `createSuperiorLandlords saves nothing when given an empty list`() {
        // Act
        superiorLandlordService.createSuperiorLandlords(MockLandlordData.createPropertyOwnership(), emptyList())

        // Assert
        verifyNoInteractions(superiorLandlordRepository)
        verifyNoInteractions(addressService)
    }

    @Test
    fun `clearSuperiorLandlords deletes every superior landlord for the property`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        // Act
        superiorLandlordService.clearSuperiorLandlords(propertyOwnership)

        // Assert
        verify(superiorLandlordRepository).deleteByPropertyOwnership(propertyOwnership)
    }
}
