package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
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
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.Instant

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
    fun `createFreeholders saves every freeholder in the list`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        // Act
        freeholderService.createFreeholders(
            propertyOwnership,
            listOf(
                FreeholderDataModel(detailsKnown = true, name = "Alice Freeholder", email = "alice@example.com"),
                FreeholderDataModel(detailsKnown = true, name = "Bob Freeholder", email = "bob@example.com"),
                FreeholderDataModel(detailsKnown = false),
            ),
        )

        // Assert
        val captor = argumentCaptor<Freeholder>()
        verify(freeholderRepository, times(3)).save(captor.capture())
        assertEquals(listOf("Alice Freeholder", "Bob Freeholder", null), captor.allValues.map { it.name })
    }

    @Test
    fun `createFreeholders saves nothing when given an empty list`() {
        // Act
        freeholderService.createFreeholders(MockLandlordData.createPropertyOwnership(), emptyList())

        // Assert
        verifyNoInteractions(freeholderRepository)
        verifyNoInteractions(addressService)
    }

    @Test
    fun `updateFreeholder overwrites the details of an existing freeholder`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing = Freeholder(propertyOwnership, name = "Old Name", email = "old@example.com")
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        whenever(addressService.findOrCreateAddress(addressDataModel)).thenReturn(address)

        // Act
        freeholderService.updateFreeholder(
            existing,
            FreeholderDataModel(detailsKnown = true, name = "New Name", address = addressDataModel),
        )

        // Assert
        assertEquals("New Name", existing.name)
        assertEquals(address, existing.address)
        verify(freeholderRepository).save(existing)
    }

    @Test
    fun `updateFreeholder clears the address when the freeholder switches to an email`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing =
            Freeholder(
                propertyOwnership,
                name = "Alice Freeholder",
                address = Address(AddressDataModel("1 Example Road, EG1 2AB")),
            )

        // Act
        freeholderService.updateFreeholder(
            existing,
            FreeholderDataModel(detailsKnown = true, name = "Alice Freeholder", email = "alice@example.com"),
        )

        // Assert
        assertEquals("alice@example.com", existing.email)
        assertNull(existing.address)
        verifyNoInteractions(addressService)
    }

    @Test
    fun `updateFreeholder keeps an unchanged manual address instead of saving a duplicate`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        val existing = Freeholder(propertyOwnership, name = "Old Name", address = address)

        // Act
        freeholderService.updateFreeholder(
            existing,
            FreeholderDataModel(detailsKnown = true, name = "New Name", address = addressDataModel),
        )

        // Assert
        assertEquals("New Name", existing.name)
        assertEquals(address, existing.address)
        verifyNoInteractions(addressService)
        verify(freeholderRepository).save(existing)
    }

    @Test
    fun `removeFreeholder deletes the freeholder`() {
        // Arrange
        val existing = Freeholder(MockLandlordData.createPropertyOwnership(), name = "Alice Freeholder")

        // Act
        freeholderService.removeFreeholder(existing)

        // Assert
        verify(freeholderRepository).delete(existing)
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
        // detailsKnown() only looks at the name, so assert the row is empty field by field
        assertNull(captor.firstValue.name)
        assertNull(captor.firstValue.email)
        assertNull(captor.firstValue.address)
        assertFalse(captor.firstValue.detailsKnown())
        verifyNoInteractions(addressService)
    }

    @Test
    fun `updateFreeholders saves and reports a freeholder the user added`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(emptyList())
        val newFreeholder =
            FreeholderDataModel(detailsKnown = true, name = "Alice Freeholder", email = "alice@example.com")
        val persisted = Freeholder(propertyOwnership, name = "Alice Freeholder", email = "alice@example.com")
        ReflectionTestUtils.setField(persisted, "id", 42L)
        whenever(freeholderRepository.save(any())).thenReturn(persisted)

        // Act
        val added = freeholderService.updateFreeholders(propertyOwnership, listOf(newFreeholder), emptyMap())

        // Assert
        assertEquals(listOf(persisted), added)
        val captor = argumentCaptor<Freeholder>()
        verify(freeholderRepository).save(captor.capture())
        assertEquals("Alice Freeholder", captor.firstValue.name)
    }

    @Test
    fun `updateFreeholders rejects a stale snapshot before making any changes`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val current = savedFreeholder(propertyOwnership, id = 1, name = "Alice Freeholder", email = "old@example.com")
        val removed = savedFreeholder(propertyOwnership, id = 2, name = "Bob Freeholder", email = "bob@example.com")
        val currentTimestamp = Instant.parse("2025-01-02T00:00:00Z")
        ReflectionTestUtils.setField(current, "lastModifiedDate", currentTimestamp)
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(listOf(current, removed))
        val staleSnapshot =
            mapOf(
                1L to Instant.parse("2025-01-01T00:00:00Z").toString(),
                2L to removed.getMostRecentlyUpdated().toString(),
            )
        // Act, Assert
        assertThrows(UpdateConflictException::class.java) {
            freeholderService.updateFreeholders(
                propertyOwnership,
                listOf(
                    FreeholderDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Freeholder",
                        email = "new@example.com",
                    ),
                    FreeholderDataModel(detailsKnown = true, name = "Carol Freeholder"),
                ),
                staleSnapshot,
            )
        }

        verify(freeholderRepository, never()).save(any())
        verify(freeholderRepository, never()).delete(any())
    }

    @Test
    fun `updateFreeholders rejects a snapshot when the saved freeholder IDs have changed`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing = savedFreeholder(propertyOwnership, id = 1, name = "Alice Freeholder", email = "alice@example.com")
        val concurrentlyAdded = savedFreeholder(propertyOwnership, id = 2, name = "Bob Freeholder", email = "bob@example.com")
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(listOf(existing, concurrentlyAdded))

        // Act, Assert
        assertThrows(UpdateConflictException::class.java) {
            freeholderService.updateFreeholders(
                propertyOwnership,
                listOf(
                    FreeholderDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Freeholder",
                        email = "alice@example.com",
                    ),
                ),
                snapshotOf(existing),
            )
        }

        verify(freeholderRepository, never()).save(any())
        verify(freeholderRepository, never()).delete(any())
    }

    @Test
    fun `updateFreeholders saves a freeholder whose contact details changed without reporting them`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing = savedFreeholder(propertyOwnership, id = 1, name = "Alice Freeholder", email = "old@example.com")
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(listOf(existing))
        val amended =
            FreeholderDataModel(
                databaseId = 1,
                detailsKnown = true,
                name = "Alice Freeholder",
                email = "new@example.com",
            )

        // Act
        val added = freeholderService.updateFreeholders(propertyOwnership, listOf(amended), snapshotOf(existing))

        // Assert
        assertEquals("new@example.com", existing.email)
        verify(freeholderRepository).save(existing)
        // Contacting freeholders whose details changed is PDJB-2171, so they are saved but not reported
        assertEquals(emptyList<Freeholder>(), added)
    }

    @Test
    fun `updateFreeholders leaves an unchanged freeholder alone`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing = savedFreeholder(propertyOwnership, id = 1, name = "Alice Freeholder", email = "alice@example.com")
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(listOf(existing))

        // Act
        val added =
            freeholderService.updateFreeholders(
                propertyOwnership,
                listOf(
                    FreeholderDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Freeholder",
                        email = "alice@example.com",
                    ),
                ),
                snapshotOf(existing),
            )

        // Assert
        assertEquals(emptyList<Freeholder>(), added)
        verify(freeholderRepository, never()).save(any())
    }

    @Test
    fun `updateFreeholders leaves a freeholder with an unchanged manual address alone`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        // A manual address has no UPRN, so it is compared on the address the user sees
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val untouched =
            savedFreeholder(
                propertyOwnership,
                id = 1,
                name = "Bob Freeholder",
                address = Address(addressDataModel),
            )
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(listOf(untouched))

        // Act
        val added =
            freeholderService.updateFreeholders(
                propertyOwnership,
                listOf(
                    FreeholderDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Bob Freeholder",
                        address = addressDataModel,
                    ),
                ),
                snapshotOf(untouched),
            )

        // Assert
        // Rewriting it would mint a duplicate address row, as findOrCreateAddress always saves a
        // manual address
        assertEquals(emptyList<Freeholder>(), added)
        verify(freeholderRepository, never()).save(any())
        verifyNoInteractions(addressService)
    }

    @Test
    fun `updateFreeholders leaves an unchanged freeholder at the same UPRN alone`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB", uprn = 123456)
        val existing =
            savedFreeholder(
                propertyOwnership,
                id = 1,
                name = "Alice Freeholder",
                address = Address(addressDataModel),
            )
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(listOf(existing))

        // Act
        val added =
            freeholderService.updateFreeholders(
                propertyOwnership,
                listOf(
                    FreeholderDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Freeholder",
                        address = addressDataModel,
                    ),
                ),
                snapshotOf(existing),
            )

        // Assert
        assertEquals(emptyList<Freeholder>(), added)
        verifyNoInteractions(addressService)
        verify(freeholderRepository, never()).save(any())
    }

    @Test
    fun `updateFreeholders treats a changed manual address as changed`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        // Neither manually entered address has a UPRN, so they are compared on the address itself
        val savedAddress = AddressDataModel("1 Example Road, EG1 2AB")
        val amendedAddress = AddressDataModel("2 Example Road, EG1 2AB")
        val existing =
            savedFreeholder(
                propertyOwnership,
                id = 1,
                name = "Alice Freeholder",
                address = Address(savedAddress),
            )
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(listOf(existing))
        whenever(addressService.findOrCreateAddress(amendedAddress)).thenReturn(Address(amendedAddress))
        val resubmitted =
            FreeholderDataModel(
                databaseId = 1,
                detailsKnown = true,
                name = "Alice Freeholder",
                address = amendedAddress,
            )

        // Act
        val added = freeholderService.updateFreeholders(propertyOwnership, listOf(resubmitted), snapshotOf(existing))

        // Assert
        verify(freeholderRepository).save(existing)
        assertEquals(emptyList<Freeholder>(), added)
    }

    @Test
    fun `updateFreeholders deletes a freeholder the user removed from the list`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val kept = savedFreeholder(propertyOwnership, id = 1, name = "Alice Freeholder", email = "alice@example.com")
        val removed = savedFreeholder(propertyOwnership, id = 2, name = "Bob Freeholder", email = "bob@example.com")
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id))
            .thenReturn(listOf(kept, removed))

        // Act
        val added =
            freeholderService.updateFreeholders(
                propertyOwnership,
                listOf(
                    FreeholderDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Freeholder",
                        email = "alice@example.com",
                    ),
                ),
                snapshotOf(kept, removed),
            )

        // Assert
        verify(freeholderRepository).delete(removed)
        verify(freeholderRepository, never()).delete(kept)
        // Contacting removed freeholders is PDJB-2171, so they are deleted but not reported
        assertEquals(emptyList<Freeholder>(), added)
    }

    @Test
    fun `updateFreeholders only reports the freeholders that were added`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val unchanged = savedFreeholder(propertyOwnership, id = 1, name = "Alice Freeholder", email = "alice@example.com")
        val amendedExisting = savedFreeholder(propertyOwnership, id = 2, name = "Bob Freeholder", email = "old@example.com")
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id))
            .thenReturn(listOf(unchanged, amendedExisting))
        val amended =
            FreeholderDataModel(
                databaseId = 2,
                detailsKnown = true,
                name = "Bob Freeholder",
                email = "new@example.com",
            )
        val newFreeholder = FreeholderDataModel(detailsKnown = true, name = "Carol Freeholder", email = "carol@example.com")
        val persistedNewFreeholder = Freeholder(propertyOwnership, name = "Carol Freeholder", email = "carol@example.com")
        ReflectionTestUtils.setField(persistedNewFreeholder, "id", 3L)
        whenever(freeholderRepository.save(any())).thenAnswer { invocation ->
            val saved = invocation.getArgument<Freeholder>(0)
            if (saved.name == "Carol Freeholder") persistedNewFreeholder else saved
        }

        // Act
        val added =
            freeholderService.updateFreeholders(
                propertyOwnership,
                listOf(
                    FreeholderDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Alice Freeholder",
                        email = "alice@example.com",
                    ),
                    amended,
                    newFreeholder,
                ),
                snapshotOf(unchanged, amendedExisting),
            )

        // Assert
        // Only the new freeholder is reported, even though an existing one was amended and another left alone
        assertEquals(listOf(persistedNewFreeholder), added)
        assertEquals("new@example.com", amendedExisting.email)
        verify(freeholderRepository, never()).save(unchanged)
    }

    @Test
    fun `updateFreeholders treats a freeholder whose saved row has gone as a new one`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(emptyList())
        val stale =
            FreeholderDataModel(
                databaseId = 99,
                detailsKnown = true,
                name = "Alice Freeholder",
                email = "alice@example.com",
            )

        // Act
        val persisted = Freeholder(propertyOwnership, name = "Alice Freeholder", email = "alice@example.com")
        ReflectionTestUtils.setField(persisted, "id", 3L)
        whenever(freeholderRepository.save(any())).thenReturn(persisted)
        val added = freeholderService.updateFreeholders(propertyOwnership, listOf(stale), emptyMap())

        // Assert
        assertEquals(listOf(persisted), added)
        verify(freeholderRepository).save(any())
    }

    private fun snapshotOf(vararg freeholders: Freeholder): Map<Long, String> =
        freeholders.associate { it.id to it.getMostRecentlyUpdated().toString() }

    private fun savedFreeholder(
        propertyOwnership: PropertyOwnership,
        id: Long,
        name: String? = null,
        email: String? = null,
        address: Address? = null,
    ) = Freeholder(propertyOwnership, name = name, email = email, address = address)
        .also {
            ReflectionTestUtils.setField(it, "id", id)
            ReflectionTestUtils.setField(it, "createdDate", Instant.parse("2025-01-01T00:00:00Z"))
        }
}
