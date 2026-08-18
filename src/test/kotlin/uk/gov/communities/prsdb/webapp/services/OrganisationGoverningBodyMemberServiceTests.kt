package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationGoverningBodyMemberRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import java.time.LocalDate
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OrganisationGoverningBodyMemberServiceTests {
    @Mock
    private lateinit var mockOrganisationGoverningBodyMemberRepository: OrganisationGoverningBodyMemberRepository

    @Mock
    private lateinit var mockAddressService: AddressService

    @InjectMocks
    private lateinit var organisationGoverningBodyMemberService: OrganisationGoverningBodyMemberService

    @Mock
    private lateinit var mockOrganisationLandlord: OrganisationalLandlord

    @Test
    fun `createGoverningBodyMembers saves each member with resolved address`() {
        val addressDataModel1 = AddressDataModel(singleLineAddress = "1 Director Lane", postcode = "SW1A 1AA")
        val addressDataModel2 = AddressDataModel(singleLineAddress = "2 Trustee Road", postcode = "W1 1AA")
        val address1 = Address(addressDataModel1)
        val address2 = Address(addressDataModel2)

        whenever(mockAddressService.findOrCreateAddress(addressDataModel1)).thenReturn(address1)
        whenever(mockAddressService.findOrCreateAddress(addressDataModel2)).thenReturn(address2)

        val members =
            listOf(
                GoverningBodyMemberDataModel(
                    name = "Director Dave",
                    type = GoverningBodyMemberType.DIRECTOR,
                    dateOfBirth = kotlinx.datetime.LocalDate(1970, 5, 12),
                    address = addressDataModel1,
                ),
                GoverningBodyMemberDataModel(
                    name = "Trustee Tina",
                    type = GoverningBodyMemberType.TRUSTEE,
                    dateOfBirth = kotlinx.datetime.LocalDate(1985, 3, 20),
                    address = addressDataModel2,
                ),
            )

        organisationGoverningBodyMemberService.createGoverningBodyMembers(mockOrganisationLandlord, members)

        val captor = captor<OrganisationGoverningBodyMember>()
        verify(mockOrganisationGoverningBodyMemberRepository, times(2)).save(captor.capture())

        val saved = captor.allValues
        assertEquals("Director Dave", saved[0].name)
        assertEquals(GoverningBodyMemberType.DIRECTOR, saved[0].type)
        assertEquals(LocalDate.of(1970, 5, 12), saved[0].dateOfBirth)
        assertEquals(address1, saved[0].address)
        assertEquals(mockOrganisationLandlord, saved[0].organisationalLandlord)

        assertEquals("Trustee Tina", saved[1].name)
        assertEquals(GoverningBodyMemberType.TRUSTEE, saved[1].type)
        assertEquals(LocalDate.of(1985, 3, 20), saved[1].dateOfBirth)
        assertEquals(address2, saved[1].address)
        assertEquals(mockOrganisationLandlord, saved[1].organisationalLandlord)
    }

    @Test
    fun `createGoverningBodyMembers does not save anything when members list is empty`() {
        organisationGoverningBodyMemberService.createGoverningBodyMembers(mockOrganisationLandlord, emptyList())

        verify(mockOrganisationGoverningBodyMemberRepository, never()).save(any())
        verify(mockAddressService, never()).findOrCreateAddress(any())
    }

    @Test
    fun `createGoverningBodyMembers resolves each member address via addressService`() {
        val addressDataModel = AddressDataModel(singleLineAddress = "10 Partner Place", postcode = "N1 1AA")
        val resolvedAddress = Address(addressDataModel)

        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(resolvedAddress)

        val members =
            listOf(
                GoverningBodyMemberDataModel(
                    name = "Partner Pat",
                    type = GoverningBodyMemberType.PARTNER,
                    dateOfBirth = kotlinx.datetime.LocalDate(1990, 8, 1),
                    address = addressDataModel,
                ),
            )

        organisationGoverningBodyMemberService.createGoverningBodyMembers(mockOrganisationLandlord, members)

        verify(mockAddressService).findOrCreateAddress(eq(addressDataModel))
    }

    @Test
    fun `clearGoverningBodyMembers deletes existing members`() {
        organisationGoverningBodyMemberService.clearGoverningBodyMembers(mockOrganisationLandlord)

        verify(mockOrganisationGoverningBodyMemberRepository).deleteByOrganisationalLandlord(mockOrganisationLandlord)
        verify(mockOrganisationGoverningBodyMemberRepository, never()).save(any())
    }
}
