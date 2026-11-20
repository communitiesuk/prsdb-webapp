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
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class FreeholderServiceTests {
    @Mock
    private lateinit var freeholderRepository: FreeholderRepository

    @InjectMocks
    private lateinit var freeholderService: FreeholderService

    @Test
    fun `createFreeholders saves a postal address freeholder with only their name`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")

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
        assertFalse(captor.firstValue.detailsKnown())
    }

    @Test
    fun `createFreeholders saves an email freeholder with their name and email`() {
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
        assertEquals("bob@example.com", captor.firstValue.email)
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
    }

    @Test
    fun `updateFreeholder overwrites the details of an existing freeholder`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing = Freeholder(propertyOwnership, name = "Old Name", email = "old@example.com")
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")

        // Act
        freeholderService.updateFreeholder(
            existing,
            FreeholderDataModel(detailsKnown = true, name = "New Name", address = addressDataModel),
        )

        // Assert
        assertEquals("New Name", existing.name)
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
        assertFalse(captor.firstValue.detailsKnown())
    }

    @Test
    fun `updateFreeholders saves and reports a freeholder the user added`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(emptyList())
        val newFreeholder =
            FreeholderDataModel(detailsKnown = true, name = "Alice Freeholder", email = "alice@example.com")

        // Act
        val added = freeholderService.updateFreeholders(propertyOwnership, listOf(newFreeholder))

        // Assert
        assertEquals(listOf(newFreeholder), added)
        val captor = argumentCaptor<Freeholder>()
        verify(freeholderRepository).save(captor.capture())
        assertEquals("Alice Freeholder", captor.firstValue.name)
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
        val added = freeholderService.updateFreeholders(propertyOwnership, listOf(amended))

        // Assert
        assertEquals("new@example.com", existing.email)
        verify(freeholderRepository).save(existing)
        // Contacting freeholders whose details changed is PDJB-2171, so they are saved but not reported
        assertEquals(emptyList<FreeholderDataModel>(), added)
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
            )

        // Assert
        assertEquals(emptyList<FreeholderDataModel>(), added)
        verify(freeholderRepository, never()).save(any())
    }

    @Test
    fun `updateFreeholders leaves an untouched postal freeholder alone`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val untouched = savedFreeholder(propertyOwnership, id = 1, name = "Bob Freeholder")
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(listOf(untouched))

        // Act
        val added =
            freeholderService.updateFreeholders(
                propertyOwnership,
                listOf(FreeholderDataModel(databaseId = 1, detailsKnown = true, name = "Bob Freeholder")),
            )

        // Assert
        assertEquals(emptyList<FreeholderDataModel>(), added)
        verify(freeholderRepository, never()).save(any())
    }

    @Test
    fun `updateFreeholders updates a postal freeholder whose address was re-entered`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existing = savedFreeholder(propertyOwnership, id = 1, name = "Bob Freeholder")
        whenever(freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id)).thenReturn(listOf(existing))

        // Act
        val added =
            freeholderService.updateFreeholders(
                propertyOwnership,
                listOf(
                    FreeholderDataModel(
                        databaseId = 1,
                        detailsKnown = true,
                        name = "Bob Freeholder",
                        address = AddressDataModel("1 Example Road, EG1 2AB"),
                    ),
                ),
            )

        // Assert
        assertEquals(emptyList<FreeholderDataModel>(), added)
        verify(freeholderRepository).save(existing)
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
            )

        // Assert
        verify(freeholderRepository).delete(removed)
        verify(freeholderRepository, never()).delete(kept)
        // Contacting removed freeholders is PDJB-2171, so they are deleted but not reported
        assertEquals(emptyList<FreeholderDataModel>(), added)
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
            )

        // Assert
        // Only the new freeholder is reported, even though an existing one was amended and another left alone
        assertEquals(listOf(newFreeholder), added)
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
        val added = freeholderService.updateFreeholders(propertyOwnership, listOf(stale))

        // Assert
        assertEquals(listOf(stale), added)
        verify(freeholderRepository).save(any())
    }

    private fun savedFreeholder(
        propertyOwnership: PropertyOwnership,
        id: Long,
        name: String? = null,
        email: String? = null,
    ) = Freeholder(propertyOwnership, name = name, email = email)
        .also { ReflectionTestUtils.setField(it, "id", id) }
}
