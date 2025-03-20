package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.util.Optional
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class PropertyServiceTests {
    @Mock
    private lateinit var mockPropertyRepository: PropertyRepository

    @Mock
    private lateinit var mockAddressService: AddressService

    @InjectMocks
    private lateinit var propertyService: PropertyService

    @Test
    fun `activateOrCreateProperty activates a property when it exists`() {
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB", uprn = 0)
        val address = Address(addressDataModel)

        val inactiveProperty =
            Property(
                status = RegistrationStatus.REGISTERED,
                propertyType = PropertyType.DETACHED_HOUSE,
                address = address,
                isActive = false,
            )

        val newPropertyType = PropertyType.FLAT
        val expectedActivatedProperty =
            Property(
                status = RegistrationStatus.REGISTERED,
                propertyType = newPropertyType,
                address = address,
            )
        ReflectionTestUtils.setField(expectedActivatedProperty, "id", inactiveProperty.id)

        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockPropertyRepository.findByAddress_Uprn(addressDataModel.uprn!!)).thenReturn(inactiveProperty)
        whenever(mockPropertyRepository.save(any(Property::class.java))).thenReturn(expectedActivatedProperty)

        propertyService.activateOrCreateProperty(addressDataModel, newPropertyType)

        val propertyCaptor = captor<Property>()
        verify(mockPropertyRepository).save(propertyCaptor.capture())
        assertTrue(ReflectionEquals(expectedActivatedProperty).matches(propertyCaptor.value))
    }

    @Test
    fun `activateOrCreateProperty creates a property when it does not exist`() {
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val propertyType = PropertyType.DETACHED_HOUSE
        val address = Address(addressDataModel)
        val expectedProperty =
            Property(
                status = RegistrationStatus.REGISTERED,
                propertyType = propertyType,
                address = address,
            )

        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockPropertyRepository.save(any(Property::class.java))).thenReturn(expectedProperty)

        propertyService.activateOrCreateProperty(addressDataModel, propertyType)

        val propertyCaptor = captor<Property>()
        verify(mockPropertyRepository).save(propertyCaptor.capture())
        assertTrue(ReflectionEquals(expectedProperty).matches(propertyCaptor.value))
    }

    @Test
    fun `deleteProperty calls delete on the propertyRepository`() {
        val property = MockLandlordData.createProperty()

        propertyService.deleteProperty(property)

        verify(mockPropertyRepository).delete(property)
    }

    @Test
    fun `retrievePropertyById returns the property if it is in the database`() {
        // Arrange
        val property = MockLandlordData.createProperty()
        val propertyId = property.id
        doReturn(Optional.of(property)).whenever(mockPropertyRepository).findById(propertyId)

        // Act
        val retrievedProperty = propertyService.retrievePropertyById(propertyId)

        // Assert
        assertEquals(property, retrievedProperty)
    }

    @Test
    fun `retrievePropertyById returns null if no matching property is in the database`() {
        assertNull(propertyService.retrievePropertyById(1))
    }
}
