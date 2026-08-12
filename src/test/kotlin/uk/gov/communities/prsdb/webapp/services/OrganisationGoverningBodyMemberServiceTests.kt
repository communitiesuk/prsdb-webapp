package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationGoverningBodyMemberRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
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
        whenever(mockOrganisationLandlord.id).thenReturn(42L)

        organisationGoverningBodyMemberService.clearGoverningBodyMembers(mockOrganisationLandlord)

        verify(mockOrganisationGoverningBodyMemberRepository).deleteByOrganisationalLandlord_Id(42L)
        verify(mockOrganisationGoverningBodyMemberRepository, never()).save(any())
    }

    @Test
    fun `updateGoverningBodyMembers deletes members not in update list`() {
        val retainedMember = mockExistingMember(id = 1L)
        val deletedMember = mock<OrganisationGoverningBodyMember>()
        whenever(deletedMember.id).thenReturn(2L)
        whenever(mockOrganisationLandlord.governingBodyMembers).thenReturn(listOf(retainedMember, deletedMember))

        organisationGoverningBodyMemberService.updateGoverningBodyMembers(
            mockOrganisationLandlord,
            listOf(createGoverningBodyMemberDataModel(databaseId = 1L)),
        )

        verify(mockOrganisationGoverningBodyMemberRepository).deleteById(2L)
        verify(mockOrganisationGoverningBodyMemberRepository, never()).deleteById(1L)
    }

    @Test
    fun `updateGoverningBodyMembers creates new members with null databaseId`() {
        val addressDataModel = AddressDataModel(singleLineAddress = "3 Partner Place", postcode = "N1 1AA")
        val resolvedAddress = Address(addressDataModel)
        whenever(mockOrganisationLandlord.governingBodyMembers).thenReturn(emptyList())
        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(resolvedAddress)

        organisationGoverningBodyMemberService.updateGoverningBodyMembers(
            mockOrganisationLandlord,
            listOf(
                createGoverningBodyMemberDataModel(
                    name = "Partner Pat",
                    type = GoverningBodyMemberType.PARTNER,
                    dateOfBirth = kotlinx.datetime.LocalDate(1990, 8, 1),
                    address = addressDataModel,
                ),
            ),
        )

        val savedMemberCaptor = captor<OrganisationGoverningBodyMember>()
        verify(mockOrganisationGoverningBodyMemberRepository).save(savedMemberCaptor.capture())

        val savedMember = savedMemberCaptor.allValues.first()
        assertEquals(mockOrganisationLandlord, savedMember.organisationalLandlord)
        assertEquals(GoverningBodyMemberType.PARTNER, savedMember.type)
        assertEquals("Partner Pat", savedMember.name)
        assertEquals(LocalDate.of(1990, 8, 1), savedMember.dateOfBirth)
        assertEquals(resolvedAddress, savedMember.address)
    }

    @Test
    fun `updateGoverningBodyMembers updates changed fields on existing member`() {
        val existingMember = mockExistingMember(id = 1L)
        whenever(mockOrganisationLandlord.governingBodyMembers).thenReturn(listOf(existingMember))

        organisationGoverningBodyMemberService.updateGoverningBodyMembers(
            mockOrganisationLandlord,
            listOf(
                createGoverningBodyMemberDataModel(
                    name = "Updated Director",
                    type = GoverningBodyMemberType.TRUSTEE,
                    dateOfBirth = kotlinx.datetime.LocalDate(1990, 8, 1),
                    databaseId = 1L,
                ),
            ),
        )

        verify(existingMember).type = GoverningBodyMemberType.TRUSTEE
        verify(existingMember).name = "Updated Director"
        verify(existingMember).dateOfBirth = LocalDate.of(1990, 8, 1)
        verify(mockOrganisationGoverningBodyMemberRepository).save(existingMember)
    }

    @Test
    fun `updateGoverningBodyMembers does not save unchanged existing member`() {
        val existingMember = mockExistingMember(id = 1L)
        whenever(mockOrganisationLandlord.governingBodyMembers).thenReturn(listOf(existingMember))

        organisationGoverningBodyMemberService.updateGoverningBodyMembers(
            mockOrganisationLandlord,
            listOf(createGoverningBodyMemberDataModel(databaseId = 1L)),
        )

        verify(mockOrganisationGoverningBodyMemberRepository, never()).save(any())
        verify(mockAddressService, never()).findOrCreateAddress(any())
    }

    @Test
    fun `updateGoverningBodyMembers updates address when address differs`() {
        val existingMember = mockExistingMember(id = 1L)
        val updatedAddressDataModel = AddressDataModel(singleLineAddress = "4 New Address Avenue", postcode = "B1 2CD")
        val updatedAddress = Address(updatedAddressDataModel)
        whenever(mockOrganisationLandlord.governingBodyMembers).thenReturn(listOf(existingMember))
        whenever(mockAddressService.findOrCreateAddress(updatedAddressDataModel)).thenReturn(updatedAddress)

        organisationGoverningBodyMemberService.updateGoverningBodyMembers(
            mockOrganisationLandlord,
            listOf(createGoverningBodyMemberDataModel(address = updatedAddressDataModel, databaseId = 1L)),
        )

        verify(mockAddressService).findOrCreateAddress(updatedAddressDataModel)
        verify(existingMember).address = updatedAddress
        verify(mockOrganisationGoverningBodyMemberRepository).save(existingMember)
    }

    @Test
    fun `updateGoverningBodyMembers throws for duplicate databaseIds`() {
        val existingMember = mock<OrganisationGoverningBodyMember>()
        whenever(existingMember.id).thenReturn(1L)
        whenever(mockOrganisationLandlord.governingBodyMembers).thenReturn(listOf(existingMember))

        val exception =
            assertThrows<PrsdbWebException> {
                organisationGoverningBodyMemberService.updateGoverningBodyMembers(
                    mockOrganisationLandlord,
                    listOf(
                        createGoverningBodyMemberDataModel(databaseId = 1L),
                        createGoverningBodyMemberDataModel(name = "Duplicate Director", databaseId = 1L),
                    ),
                )
            }

        assertEquals("Duplicate governing body member IDs in update request", exception.message)
        verify(mockOrganisationGoverningBodyMemberRepository, never()).deleteById(any())
        verify(mockOrganisationGoverningBodyMemberRepository, never()).save(any())
    }

    @Test
    fun `updateGoverningBodyMembers throws for foreign databaseIds`() {
        val existingMember = mock<OrganisationGoverningBodyMember>()
        whenever(existingMember.id).thenReturn(1L)
        whenever(mockOrganisationLandlord.governingBodyMembers).thenReturn(listOf(existingMember))

        val exception =
            assertThrows<PrsdbWebException> {
                organisationGoverningBodyMemberService.updateGoverningBodyMembers(
                    mockOrganisationLandlord,
                    listOf(createGoverningBodyMemberDataModel(databaseId = 2L)),
                )
            }

        assertEquals("Governing body member IDs not found for this landlord: [2]", exception.message)
        verify(mockOrganisationGoverningBodyMemberRepository, never()).deleteById(any())
        verify(mockOrganisationGoverningBodyMemberRepository, never()).save(any())
    }

    @Test
    fun `updateGoverningBodyMembers handles mixed add change and remove`() {
        val updatedExistingMember = mockExistingMember(id = 1L)
        val deletedExistingMember = mock<OrganisationGoverningBodyMember>()
        whenever(deletedExistingMember.id).thenReturn(2L)
        val newAddressDataModel = AddressDataModel(singleLineAddress = "5 New Partner Parade", postcode = "LS1 4EF")
        val newAddress = Address(newAddressDataModel)
        whenever(mockOrganisationLandlord.governingBodyMembers).thenReturn(listOf(updatedExistingMember, deletedExistingMember))
        whenever(mockAddressService.findOrCreateAddress(newAddressDataModel)).thenReturn(newAddress)

        organisationGoverningBodyMemberService.updateGoverningBodyMembers(
            mockOrganisationLandlord,
            listOf(
                createGoverningBodyMemberDataModel(name = "Updated Director", databaseId = 1L),
                createGoverningBodyMemberDataModel(
                    name = "Partner Pat",
                    type = GoverningBodyMemberType.PARTNER,
                    dateOfBirth = kotlinx.datetime.LocalDate(1990, 8, 1),
                    address = newAddressDataModel,
                ),
            ),
        )

        verify(mockOrganisationGoverningBodyMemberRepository).deleteById(2L)
        verify(updatedExistingMember).name = "Updated Director"

        val savedMemberCaptor = captor<OrganisationGoverningBodyMember>()
        verify(mockOrganisationGoverningBodyMemberRepository, times(2)).save(savedMemberCaptor.capture())

        val savedMembers = savedMemberCaptor.allValues
        assertEquals(1, savedMembers.count { it == updatedExistingMember })
        val createdMember = savedMembers.single { it != updatedExistingMember }
        assertEquals(mockOrganisationLandlord, createdMember.organisationalLandlord)
        assertEquals(GoverningBodyMemberType.PARTNER, createdMember.type)
        assertEquals("Partner Pat", createdMember.name)
        assertEquals(LocalDate.of(1990, 8, 1), createdMember.dateOfBirth)
        assertEquals(newAddress, createdMember.address)
    }

    private fun createGoverningBodyMemberDataModel(
        name: String = "Director Dave",
        type: GoverningBodyMemberType = GoverningBodyMemberType.DIRECTOR,
        dateOfBirth: kotlinx.datetime.LocalDate = kotlinx.datetime.LocalDate(1970, 5, 12),
        address: AddressDataModel = AddressDataModel(singleLineAddress = "1 Director Lane", postcode = "SW1A 1AA"),
        databaseId: Long? = null,
    ) = GoverningBodyMemberDataModel(
        name = name,
        type = type,
        dateOfBirth = dateOfBirth,
        address = address,
        databaseId = databaseId,
    )

    private fun mockExistingMember(
        id: Long,
        type: GoverningBodyMemberType = GoverningBodyMemberType.DIRECTOR,
        name: String = "Director Dave",
        dateOfBirth: LocalDate = LocalDate.of(1970, 5, 12),
        address: Address = Address(AddressDataModel(singleLineAddress = "1 Director Lane", postcode = "SW1A 1AA")),
    ): OrganisationGoverningBodyMember {
        val existingMember = mock<OrganisationGoverningBodyMember>()
        whenever(existingMember.id).thenReturn(id)
        whenever(existingMember.type).thenReturn(type)
        whenever(existingMember.name).thenReturn(name)
        whenever(existingMember.dateOfBirth).thenReturn(dateOfBirth)
        whenever(existingMember.address).thenReturn(address)
        return existingMember
    }
}
