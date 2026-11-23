package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ActivationServiceTests {
    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var landlordRepository: LandlordRepository

    @InjectMocks
    private lateinit var activationService: ActivationService

    @Test
    fun `reactivateLandlord marks an inactive landlord active, clears the date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        activationService.reactivateLandlord(landlord)

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `reactivateLandlord throws and makes no change for an already active landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        assertThrows<IllegalStateException> { activationService.reactivateLandlord(landlord) }

        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `reactivateLandlordIfInactive marks an inactive landlord active, clears the date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        activationService.reactivateLandlordIfInactive(landlord)

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `reactivateLandlordIfInactive makes no change for an already active landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        activationService.reactivateLandlordIfInactive(landlord)

        assertTrue(landlord.isActive)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `reactivateProperty marks an inactive property active, clears the date and saves it`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID)
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activationService.reactivateProperty(PROPERTY_ID)

        assertTrue(property.isActive)
        assertNull(property.lastDeactivatedDate)
        verify(propertyOwnershipRepository).save(property)
    }

    @Test
    fun `reactivateProperty reactivates an inactive landlord, clears the date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activationService.reactivateProperty(PROPERTY_ID)

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `reactivateProperty only reactivates the joint landlords who are inactive`() {
        val activeLandlord = MockLandlordData.createIndividualLandlord(name = "active")
        val inactiveLandlord = MockLandlordData.createIndividualLandlord(name = "inactive")
        inactiveLandlord.deactivate(ORIGINAL_DATE)
        val property =
            MockLandlordData.createPropertyOwnership(
                id = PROPERTY_ID,
                landlords = mutableSetOf(activeLandlord, inactiveLandlord),
            )
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activationService.reactivateProperty(PROPERTY_ID)

        assertTrue(activeLandlord.isActive)
        assertTrue(inactiveLandlord.isActive)
        verify(landlordRepository).save(inactiveLandlord)
        verify(landlordRepository, never()).save(activeLandlord)
    }

    @Test
    fun `reactivateProperty throws and makes no change for an already active property`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, isActive = true)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        assertThrows<IllegalStateException> { activationService.reactivateProperty(PROPERTY_ID) }

        verify(propertyOwnershipRepository, never()).save(any())
    }

    @Test
    fun `reactivateProperty throws NOT_FOUND for an unknown property`() {
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty())

        val exception = assertThrows<ResponseStatusException> { activationService.reactivateProperty(PROPERTY_ID) }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    companion object {
        private const val PROPERTY_ID = 1L
        private val ORIGINAL_DATE = LocalDate.of(2026, 1, 1)
    }
}
