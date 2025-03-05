package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class AddressServiceTests {
    @Mock
    private lateinit var mockAddressRepository: AddressRepository

    @Mock
    private lateinit var mockLocalAuthorityService: LocalAuthorityService

    @InjectMocks
    private lateinit var addressService: AddressService

    private val mockAddress = Address()

    @Test
    fun `findOrCreateAddress creates an address when given an AddressDataModel with no UPRN`() {
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")

        whenever(mockAddressRepository.save(any(Address::class.java))).thenReturn(mockAddress)

        addressService.findOrCreateAddress(addressDataModel)

        val addressCaptor = captor<Address>()
        verify(mockAddressRepository).save(addressCaptor.capture())
        assertEquals(addressDataModel.singleLineAddress, addressCaptor.value.singleLineAddress)
    }

    @Test
    fun `findOrCreateAddress returns the corresponding address when given an AddressDataModel with an already existing UPRN`() {
        val uprn = 123456L
        val addressDataModel = AddressDataModel(singleLineAddress = "1 Example Road, EG1 2AB", uprn = uprn)
        val address = Address(addressDataModel)

        whenever(mockAddressRepository.findByUprn(uprn)).thenReturn(address)

        val createdAddress = addressService.findOrCreateAddress(addressDataModel)

        assertEquals(address, createdAddress)
    }

    @Test
    fun `findOrCreateAddress creates an address when given an AddressDataModel with a new UPRN`() {
        val uprn = 123456L
        val addressDataModel = AddressDataModel(singleLineAddress = "1 Example Road, EG1 2AB", uprn = uprn)

        whenever(mockAddressRepository.findByUprn(uprn)).thenReturn(null)
        whenever(mockAddressRepository.save(any(Address::class.java))).thenReturn(mockAddress)

        addressService.findOrCreateAddress(addressDataModel)

        verify(mockAddressRepository).findByUprn(uprn)
        val addressCaptor = captor<Address>()
        verify(mockAddressRepository).save(addressCaptor.capture())
        assertEquals(addressDataModel.singleLineAddress, addressCaptor.value.singleLineAddress)
    }

    @Test
    fun `findOrCreateAddress creates an address with a local authority`() {
        val localAuthority = createLocalAuthority()
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB", localAuthorityId = localAuthority.id)

        whenever(mockLocalAuthorityService.retrieveLocalAuthorityById(addressDataModel.localAuthorityId!!)).thenReturn(
            localAuthority,
        )
        whenever(mockAddressRepository.save(any(Address::class.java))).thenReturn(mockAddress)

        addressService.findOrCreateAddress(addressDataModel)

        val addressCaptor = captor<Address>()
        verify(mockAddressRepository).save(addressCaptor.capture())
        assertEquals(addressDataModel.localAuthorityId, addressCaptor.value.localAuthority!!.id)
    }
}
