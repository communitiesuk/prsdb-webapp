package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
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
class ActiveStatusServiceTests {
    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var landlordRepository: LandlordRepository

    @Mock
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @InjectMocks
    private lateinit var activeStatusService: ActiveStatusService

    @Test
    fun `deactivateLandlord marks an active landlord inactive with the given date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        activeStatusService.deactivateLandlord(landlord, DEACTIVATION_DATE)

        assertFalse(landlord.isActive)
        assertEquals(DEACTIVATION_DATE, landlord.deactivationDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `deactivateLandlord leaves an already inactive landlord and its original date unchanged`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        activeStatusService.deactivateLandlord(landlord, DEACTIVATION_DATE)

        assertFalse(landlord.isActive)
        assertEquals(ORIGINAL_DATE, landlord.deactivationDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateLandlord throws and makes no change when the landlord still has active properties`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(true)

        assertThrows<IllegalStateException> { activeStatusService.deactivateLandlord(landlord, DEACTIVATION_DATE) }

        assertTrue(landlord.isActive)
        assertNull(landlord.deactivationDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `reactivateLandlord marks an inactive landlord active, clears the date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        activeStatusService.reactivateLandlord(landlord)

        assertTrue(landlord.isActive)
        assertNull(landlord.deactivationDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `reactivateLandlord does nothing for an already active landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        activeStatusService.reactivateLandlord(landlord)

        assertTrue(landlord.isActive)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateProperty marks the property inactive with the given date, saves it and removes any delegation`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(true)

        activeStatusService.deactivateProperty(PROPERTY_ID, DEACTIVATION_DATE)

        assertFalse(property.isActive)
        assertEquals(DEACTIVATION_DATE, property.deactivationDate)
        verify(propertyOwnershipRepository).save(property)
        verify(lettingAgentAccessService).deleteDelegationByPropertyOwnershipId(PROPERTY_ID)
    }

    @Test
    fun `deactivateProperty deactivates a sole landlord with no other active property`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(false)

        activeStatusService.deactivateProperty(PROPERTY_ID, DEACTIVATION_DATE)

        assertFalse(landlord.isActive)
        assertEquals(DEACTIVATION_DATE, landlord.deactivationDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `deactivateProperty leaves a landlord with another active property active`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(true)

        activeStatusService.deactivateProperty(PROPERTY_ID, DEACTIVATION_DATE)

        assertTrue(landlord.isActive)
        assertNull(landlord.deactivationDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateProperty only deactivates the joint landlords who have no other active property`() {
        val landlordWithOtherProperty = MockLandlordData.createIndividualLandlord(name = "has other")
        val landlordWithNoOtherProperty = MockLandlordData.createIndividualLandlord(name = "no other")
        val property =
            MockLandlordData.createPropertyOwnership(
                id = PROPERTY_ID,
                landlords = mutableSetOf(landlordWithOtherProperty, landlordWithNoOtherProperty),
            )
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(
            propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(
                landlordWithOtherProperty.id,
                PROPERTY_ID,
            ),
        ).thenReturn(true)
        whenever(
            propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(
                landlordWithNoOtherProperty.id,
                PROPERTY_ID,
            ),
        ).thenReturn(false)

        activeStatusService.deactivateProperty(PROPERTY_ID, DEACTIVATION_DATE)

        assertTrue(landlordWithOtherProperty.isActive)
        assertFalse(landlordWithNoOtherProperty.isActive)
        assertEquals(DEACTIVATION_DATE, landlordWithNoOtherProperty.deactivationDate)
        verify(landlordRepository).save(landlordWithNoOtherProperty)
        verify(landlordRepository, never()).save(landlordWithOtherProperty)
    }

    @Test
    fun `deactivateProperty keeps the original deactivation date of an already inactive landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(false)

        activeStatusService.deactivateProperty(PROPERTY_ID, DEACTIVATION_DATE)

        assertEquals(ORIGINAL_DATE, landlord.deactivationDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateProperty does nothing for an already inactive property`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property =
            MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord), isActive = false)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activeStatusService.deactivateProperty(PROPERTY_ID, DEACTIVATION_DATE)

        assertNull(property.deactivationDate)
        assertTrue(landlord.isActive)
        verify(propertyOwnershipRepository, never()).save(any())
        verifyNoInteractions(lettingAgentAccessService, landlordRepository)
    }

    @Test
    fun `deactivateProperty throws NOT_FOUND for an unknown property`() {
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty())

        val exception =
            assertThrows<ResponseStatusException> { activeStatusService.deactivateProperty(PROPERTY_ID, DEACTIVATION_DATE) }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `reactivateProperty marks an inactive property active, clears the date and saves it without touching landlords`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activeStatusService.reactivateProperty(PROPERTY_ID)

        assertTrue(property.isActive)
        assertNull(property.deactivationDate)
        verify(propertyOwnershipRepository).save(property)
        assertFalse(landlord.isActive)
        verifyNoInteractions(landlordRepository)
    }

    @Test
    fun `reactivateProperty does nothing for an already active property`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, isActive = true)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activeStatusService.reactivateProperty(PROPERTY_ID)

        assertTrue(property.isActive)
        verify(propertyOwnershipRepository, never()).save(any())
    }

    @Test
    fun `reactivateProperty throws NOT_FOUND for an unknown property`() {
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty())

        val exception = assertThrows<ResponseStatusException> { activeStatusService.reactivateProperty(PROPERTY_ID) }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    companion object {
        private const val PROPERTY_ID = 1L
        private val DEACTIVATION_DATE = LocalDate.of(2026, 11, 10)
        private val ORIGINAL_DATE = LocalDate.of(2026, 1, 1)
    }
}
