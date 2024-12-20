package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
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
    fun `createProperty creates a property`() {
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

        propertyService.createProperty(addressDataModel, propertyType)

        val propertyCaptor = captor<Property>()
        verify(mockPropertyRepository).save(propertyCaptor.capture())
        assertTrue(ReflectionEquals(expectedProperty).matches(propertyCaptor.value))
    }
}
