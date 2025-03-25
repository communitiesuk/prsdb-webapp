package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.LandlordWithListedPropertyCountRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlordWithListedPropertyCount

@ExtendWith(MockitoExtension::class)
class LandlordDeregistrationServiceTests {
    @Mock
    private lateinit var mockLandlordWithListedPropertyCountRepository: LandlordWithListedPropertyCountRepository

    @InjectMocks
    private lateinit var landlordService: LandlordService

    @Test
    fun `getLandlordHasRegisteredProperties throws an error if the landlord is not found`() {
        val baseUserId = "one-login-id"
        assertThrows<EntityNotFoundException> { landlordService.getLandlordHasRegisteredProperties(baseUserId) }
    }

    @Test
    fun `getLandlordHasRegisteredProperties returns true if listedPropertyCount is greater than 0`() {
        // Arrange
        val landlordWithListedPropertyCount = MockLandlordData.createLandlordWithListedPropertyCount(5)
        val baseUserId = landlordWithListedPropertyCount.landlord.baseUser.id
        whenever(mockLandlordWithListedPropertyCountRepository.findByLandlord_BaseUser_Id(baseUserId))
            .thenReturn(landlordWithListedPropertyCount)

        // Act, Assert
        assertTrue(landlordService.getLandlordHasRegisteredProperties(baseUserId))
    }

    @Test
    fun `getLandlordHasRegisteredProperties returns false true if listedPropertyCount is 0`() {
        // Arrange
        val landlordWithListedPropertyCount = MockLandlordData.createLandlordWithListedPropertyCount(0)
        val baseUserId = landlordWithListedPropertyCount.landlord.baseUser.id
        whenever(mockLandlordWithListedPropertyCountRepository.findByLandlord_BaseUser_Id(baseUserId))
            .thenReturn(landlordWithListedPropertyCount)

        // Act, Assert
        assertFalse(landlordService.getLandlordHasRegisteredProperties(baseUserId))
    }
}
