package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.AddressSearchRegion
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class AddressServiceTests {
    @Mock
    private lateinit var mockAddressRepository: AddressRepository

    @Mock
    private lateinit var mockLocalCouncilService: LocalCouncilService

    @InjectMocks
    private lateinit var addressService: AddressService

    @Nested
    inner class FindOrCreateAddressTests {
        @Test
        fun `findOrCreateAddress returns the corresponding address when given an AddressDataModel with a UPRN`() {
            // Arrange
            val addressDataModel = AddressDataModel(singleLineAddress = "1 Example Road, EG1 2AB", uprn = 123456L)
            val address = Address(addressDataModel)
            whenever(mockAddressRepository.findByIsActiveTrueAndUprn(addressDataModel.uprn!!)).thenReturn(address)

            // Act
            val foundAddress = addressService.findOrCreateAddress(addressDataModel)

            // Assert
            assertEquals(address, foundAddress)
        }

        @Test
        fun `findOrCreateAddress throws an error when given an AddressDataModel with an invalid UPRN`() {
            // Arrange
            val addressDataModel = AddressDataModel(singleLineAddress = "1 Example Road, EG1 2AB", uprn = 123456L)
            whenever(mockAddressRepository.findByIsActiveTrueAndUprn(addressDataModel.uprn!!)).thenReturn(null)

            // Act & Assert
            assertThrows<EntityNotFoundException> { addressService.findOrCreateAddress(addressDataModel) }
        }

        @ParameterizedTest(name = "and {0}")
        @MethodSource("uk.gov.communities.prsdb.webapp.services.AddressServiceTests#provideAddressDataModels")
        fun `findOrCreateAddress creates an address when given an AddressDataModel with no UPRN`(addressDataModel: AddressDataModel) {
            // Arrange
            addressDataModel.localCouncilId?.let {
                whenever(mockLocalCouncilService.retrieveLocalCouncilById(it))
                    .thenReturn(MockLocalCouncilData.createLocalCouncil(id = it))
            }

            whenever(mockAddressRepository.save(any())).thenReturn(MockLandlordData.createAddress())

            // Act
            addressService.findOrCreateAddress(addressDataModel)

            // Assert
            val addressCaptor = captor<Address>()
            verify(mockAddressRepository).save(addressCaptor.capture())

            val createdAddress = AddressDataModel.fromAddress(addressCaptor.value)
            assertEquals(addressDataModel, createdAddress)
        }
    }

    @Nested
    inner class SearchForAddressesTests {
        private val houseNameOrNumber = "1"
        private val postcode = "EG1 2AB"
        private val matchingAddresses =
            listOf(
                MockLandlordData.createAddress(uprn = 1),
                MockLandlordData.createAddress(uprn = 2),
                MockLandlordData.createAddress(uprn = 3),
            )
        private val expectedResults = matchingAddresses.map { AddressDataModel.fromAddress(it) }

        @Test
        fun `searchForAddresses searches addresses with an English local council for that region`() {
            whenever(mockAddressRepository.searchWithEnglishLocalCouncil(houseNameOrNumber, postcode)).thenReturn(matchingAddresses)

            val results = addressService.searchForAddresses(houseNameOrNumber, postcode, AddressSearchRegion.WITH_ENGLISH_LOCAL_COUNCIL)

            assertEquals(expectedResults, results)
        }

        @Test
        fun `searchForAddresses searches England and Wales addresses for that region`() {
            whenever(mockAddressRepository.searchInEnglandAndWales(houseNameOrNumber, postcode)).thenReturn(matchingAddresses)

            val results = addressService.searchForAddresses(houseNameOrNumber, postcode, AddressSearchRegion.ENGLAND_AND_WALES)

            assertEquals(expectedResults, results)
        }

        @Test
        fun `searchForAddresses searches UK addresses for that region`() {
            whenever(mockAddressRepository.searchAllAddresses(houseNameOrNumber, postcode)).thenReturn(matchingAddresses)

            val results = addressService.searchForAddresses(houseNameOrNumber, postcode, AddressSearchRegion.UK)

            assertEquals(expectedResults, results)
        }
    }

    companion object {
        @JvmStatic
        private fun provideAddressDataModels() =
            listOf(
                named("no local council", AddressDataModel("1 Example Road, EG1 2AB", localCouncilId = null)),
                named("a local council", AddressDataModel("1 Example Road, EG1 2AB", localCouncilId = 1)),
            )
    }
}
