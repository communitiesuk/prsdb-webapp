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
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
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
    fun `updateSuperiorLandlord overwrites the details of an existing superior landlord`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing = SuperiorLandlord(propertyOwnership, name = "Old Name", email = "old@example.com")
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        whenever(addressService.findOrCreateAddress(addressDataModel)).thenReturn(address)

        // Act
        superiorLandlordService.updateSuperiorLandlord(
            existing,
            SuperiorLandlordDataModel(detailsKnown = true, name = "New Name", address = addressDataModel),
        )

        // Assert
        assertEquals("New Name", existing.name)
        assertEquals(address, existing.address)
        verify(superiorLandlordRepository).save(existing)
    }

    @Test
    fun `updateSuperiorLandlord clears the address when the superior landlord switches to an email`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing =
            SuperiorLandlord(
                propertyOwnership,
                name = "Alice Superior",
                address = Address(AddressDataModel("1 Example Road, EG1 2AB")),
            )

        // Act
        superiorLandlordService.updateSuperiorLandlord(
            existing,
            SuperiorLandlordDataModel(detailsKnown = true, name = "Alice Superior", email = "alice@example.com"),
        )

        // Assert
        assertEquals("alice@example.com", existing.email)
        assertNull(existing.address)
        verifyNoInteractions(addressService)
    }

    @Test
    fun `updateSuperiorLandlord keeps an unchanged manual address instead of saving a duplicate`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        val existing = SuperiorLandlord(propertyOwnership, name = "Old Name", address = address)

        // Act
        superiorLandlordService.updateSuperiorLandlord(
            existing,
            SuperiorLandlordDataModel(detailsKnown = true, name = "New Name", address = addressDataModel),
        )

        // Assert
        assertEquals("New Name", existing.name)
        assertEquals(address, existing.address)
        verifyNoInteractions(addressService)
        verify(superiorLandlordRepository).save(existing)
    }

    @Test
    fun `removeSuperiorLandlord deletes the superior landlord`() {
        // Arrange
        val existing = SuperiorLandlord(MockLandlordData.createPropertyOwnership(), name = "Alice Superior")

        // Act
        superiorLandlordService.removeSuperiorLandlord(existing)

        // Assert
        verify(superiorLandlordRepository).delete(existing)
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

    @Test
    fun `updateSuperiorLandlords saves and reports a superior landlord the user added`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        whenever(superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(emptyList())
        val newSuperiorLandlord =
            SuperiorLandlordDataModel(detailsKnown = true, name = "Alice Superior", email = "alice@example.com")

        // Act
        val added = superiorLandlordService.updateSuperiorLandlords(propertyOwnership, listOf(newSuperiorLandlord))

        // Assert
        assertEquals(listOf(newSuperiorLandlord), added)
        val captor = argumentCaptor<SuperiorLandlord>()
        verify(superiorLandlordRepository).save(captor.capture())
        assertEquals("Alice Superior", captor.firstValue.name)
    }

    @Test
    fun `updateSuperiorLandlords saves one whose contact details changed without reporting them`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing = savedSuperiorLandlord(propertyOwnership, id = 1, name = "Alice Superior", email = "old@example.com")
        whenever(superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id))
            .thenReturn(listOf(existing))
        val amended =
            SuperiorLandlordDataModel(
                databaseId = 1,
                detailsKnown = true,
                name = "Alice Superior",
                email = "new@example.com",
            )

        // Act
        val added = superiorLandlordService.updateSuperiorLandlords(propertyOwnership, listOf(amended))

        // Assert
        assertEquals("new@example.com", existing.email)
        verify(superiorLandlordRepository).save(existing)
        // Contacting superior landlords whose details changed is PDJB-2171, so they are saved but not reported
        assertEquals(emptyList<SuperiorLandlordDataModel>(), added)
    }

    @Test
    fun `updateSuperiorLandlords leaves an unchanged superior landlord alone`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing =
            savedSuperiorLandlord(propertyOwnership, id = 1, name = "Alice Superior", email = "alice@example.com")
        whenever(superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id))
            .thenReturn(listOf(existing))

        // Act
        val added =
            superiorLandlordService.updateSuperiorLandlords(
                propertyOwnership,
                listOf(
                    SuperiorLandlordDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Superior",
                        email = "alice@example.com",
                    ),
                ),
            )

        // Assert
        assertEquals(emptyList<SuperiorLandlordDataModel>(), added)
        verify(superiorLandlordRepository, never()).save(any())
    }

    @Test
    fun `updateSuperiorLandlords leaves one with an unchanged manual address alone`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        // A manual address has no UPRN, so it is compared on the address the user sees
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val untouched =
            savedSuperiorLandlord(
                propertyOwnership,
                id = 1,
                name = "Bob Superior",
                address = Address(addressDataModel),
            )
        whenever(superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id))
            .thenReturn(listOf(untouched))

        // Act
        val added =
            superiorLandlordService.updateSuperiorLandlords(
                propertyOwnership,
                listOf(
                    SuperiorLandlordDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Bob Superior",
                        address = addressDataModel,
                    ),
                ),
            )

        // Assert
        // Rewriting it would mint a duplicate address row, as findOrCreateAddress always saves a
        // manual address
        assertEquals(emptyList<SuperiorLandlordDataModel>(), added)
        verify(superiorLandlordRepository, never()).save(any())
        verifyNoInteractions(addressService)
    }

    @Test
    fun `updateSuperiorLandlords leaves an unchanged superior landlord at the same UPRN alone`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB", uprn = 123456)
        val existing =
            savedSuperiorLandlord(
                propertyOwnership,
                id = 1,
                name = "Alice Superior",
                address = Address(addressDataModel),
            )
        whenever(superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id))
            .thenReturn(listOf(existing))

        // Act
        val added =
            superiorLandlordService.updateSuperiorLandlords(
                propertyOwnership,
                listOf(
                    SuperiorLandlordDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Superior",
                        address = addressDataModel,
                    ),
                ),
            )

        // Assert
        assertEquals(emptyList<SuperiorLandlordDataModel>(), added)
        verifyNoInteractions(addressService)
        verify(superiorLandlordRepository, never()).save(any())
    }

    @Test
    fun `updateSuperiorLandlords treats a changed manual address as changed`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        // Neither manually entered address has a UPRN, so they are compared on the address itself
        val savedAddress = AddressDataModel("1 Example Road, EG1 2AB")
        val amendedAddress = AddressDataModel("2 Example Road, EG1 2AB")
        val existing =
            savedSuperiorLandlord(
                propertyOwnership,
                id = 1,
                name = "Alice Superior",
                address = Address(savedAddress),
            )
        whenever(superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id))
            .thenReturn(listOf(existing))
        whenever(addressService.findOrCreateAddress(amendedAddress)).thenReturn(Address(amendedAddress))
        val resubmitted =
            SuperiorLandlordDataModel(
                databaseId = 1,
                detailsKnown = true,
                name = "Alice Superior",
                address = amendedAddress,
            )

        // Act
        val added = superiorLandlordService.updateSuperiorLandlords(propertyOwnership, listOf(resubmitted))

        // Assert
        verify(superiorLandlordRepository).save(existing)
        assertEquals(emptyList<SuperiorLandlordDataModel>(), added)
    }

    @Test
    fun `updateSuperiorLandlords deletes a superior landlord the user removed from the list`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val kept = savedSuperiorLandlord(propertyOwnership, id = 1, name = "Alice Superior", email = "alice@example.com")
        val removed = savedSuperiorLandlord(propertyOwnership, id = 2, name = "Bob Superior", email = "bob@example.com")
        whenever(superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id))
            .thenReturn(listOf(kept, removed))

        // Act
        val added =
            superiorLandlordService.updateSuperiorLandlords(
                propertyOwnership,
                listOf(
                    SuperiorLandlordDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Superior",
                        email = "alice@example.com",
                    ),
                ),
            )

        // Assert
        verify(superiorLandlordRepository).delete(removed)
        verify(superiorLandlordRepository, never()).delete(kept)
        // Contacting removed superior landlords is PDJB-2171, so they are deleted but not reported
        assertEquals(emptyList<SuperiorLandlordDataModel>(), added)
    }

    @Test
    fun `updateSuperiorLandlords only reports the superior landlords that were added`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val unchanged =
            savedSuperiorLandlord(propertyOwnership, id = 1, name = "Alice Superior", email = "alice@example.com")
        val amendedExisting =
            savedSuperiorLandlord(propertyOwnership, id = 2, name = "Bob Superior", email = "old@example.com")
        whenever(superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id))
            .thenReturn(listOf(unchanged, amendedExisting))
        val amended =
            SuperiorLandlordDataModel(
                databaseId = 2,
                detailsKnown = true,
                name = "Bob Superior",
                email = "new@example.com",
            )
        val newSuperiorLandlord =
            SuperiorLandlordDataModel(detailsKnown = true, name = "Carol Superior", email = "carol@example.com")

        // Act
        val added =
            superiorLandlordService.updateSuperiorLandlords(
                propertyOwnership,
                listOf(
                    SuperiorLandlordDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Superior",
                        email = "alice@example.com",
                    ),
                    amended,
                    newSuperiorLandlord,
                ),
            )

        // Assert
        // Only the new superior landlord is reported, even though an existing one was amended and another left alone
        assertEquals(listOf(newSuperiorLandlord), added)
        assertEquals("new@example.com", amendedExisting.email)
        verify(superiorLandlordRepository, never()).save(unchanged)
    }

    @Test
    fun `updateSuperiorLandlords treats a superior landlord whose saved row has gone as a new one`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        whenever(superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(emptyList())
        val stale =
            SuperiorLandlordDataModel(
                databaseId = 99,
                detailsKnown = true,
                name = "Alice Superior",
                email = "alice@example.com",
            )

        // Act
        val added = superiorLandlordService.updateSuperiorLandlords(propertyOwnership, listOf(stale))

        // Assert
        assertEquals(listOf(stale), added)
        verify(superiorLandlordRepository).save(any())
    }

    private fun savedSuperiorLandlord(
        propertyOwnership: PropertyOwnership,
        id: Long,
        name: String? = null,
        email: String? = null,
        address: Address? = null,
    ) = SuperiorLandlord(propertyOwnership, name = name, email = email, address = address)
        .also { ReflectionTestUtils.setField(it, "id", id) }
}
