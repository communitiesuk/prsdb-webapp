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
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ActiveInactiveServiceTests {
    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var landlordRepository: LandlordRepository

    @Mock
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @InjectMocks
    private lateinit var activeInactiveService: ActiveInactiveService

    @Test
    fun `deactivateLandlord marks an active landlord inactive with today's date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        activeInactiveService.deactivateLandlord(landlord)

        assertFalse(landlord.isActive)
        assertEquals(today(), landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `deactivateLandlord throws and makes no change for an already inactive landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        assertThrows<IllegalStateException> { activeInactiveService.deactivateLandlord(landlord) }

        assertEquals(ORIGINAL_DATE, landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateLandlord throws and makes no change when the landlord still has active properties`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(true)

        assertThrows<IllegalStateException> { activeInactiveService.deactivateLandlord(landlord) }

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties deactivates an active landlord with no active properties`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(false)

        val result = activeInactiveService.deactivateLandlordIfNoActiveProperties(landlord)

        assertTrue(result)
        assertFalse(landlord.isActive)
        assertEquals(today(), landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties makes no change when the landlord still has active properties`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(true)

        val result = activeInactiveService.deactivateLandlordIfNoActiveProperties(landlord)

        assertFalse(result)
        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties makes no change for an already inactive landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        val result = activeInactiveService.deactivateLandlordIfNoActiveProperties(landlord)

        assertFalse(result)
        assertEquals(ORIGINAL_DATE, landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `reactivateLandlord marks an inactive landlord active, clears the date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        activeInactiveService.reactivateLandlord(landlord)

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `reactivateLandlord throws and makes no change for an already active landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        assertThrows<IllegalStateException> { activeInactiveService.reactivateLandlord(landlord) }

        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `reactivateLandlordIfInactive marks an inactive landlord active, clears the date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        activeInactiveService.reactivateLandlordIfInactive(landlord)

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `reactivateLandlordIfInactive makes no change for an already active landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        activeInactiveService.reactivateLandlordIfInactive(landlord)

        assertTrue(landlord.isActive)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateProperty marks the property inactive with today's date, saves it and removes any delegation`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(true)

        activeInactiveService.deactivateProperty(PROPERTY_ID)

        assertFalse(property.isActive)
        assertEquals(today(), property.lastDeactivatedDate)
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

        activeInactiveService.deactivateProperty(PROPERTY_ID)

        assertFalse(landlord.isActive)
        assertEquals(today(), landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `deactivateProperty leaves a landlord with another active property active`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(true)

        activeInactiveService.deactivateProperty(PROPERTY_ID)

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
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

        activeInactiveService.deactivateProperty(PROPERTY_ID)

        assertTrue(landlordWithOtherProperty.isActive)
        assertFalse(landlordWithNoOtherProperty.isActive)
        assertEquals(today(), landlordWithNoOtherProperty.lastDeactivatedDate)
        verify(landlordRepository).save(landlordWithNoOtherProperty)
        verify(landlordRepository, never()).save(landlordWithOtherProperty)
    }

    @Test
    fun `deactivateProperty throws on the inconsistent state of an inactive landlord with no other active property`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(false)

        assertThrows<IllegalStateException> { activeInactiveService.deactivateProperty(PROPERTY_ID) }

        assertEquals(ORIGINAL_DATE, landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateProperty throws and makes no change for an already inactive property`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property =
            MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord), isActive = false)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        assertThrows<IllegalStateException> { activeInactiveService.deactivateProperty(PROPERTY_ID) }

        assertNull(property.lastDeactivatedDate)
        assertTrue(landlord.isActive)
        verify(propertyOwnershipRepository, never()).save(any())
        verifyNoInteractions(lettingAgentAccessService, landlordRepository)
    }

    @Test
    fun `deactivateProperty throws NOT_FOUND for an unknown property`() {
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty())

        val exception =
            assertThrows<ResponseStatusException> { activeInactiveService.deactivateProperty(PROPERTY_ID) }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `reactivateProperty marks an inactive property active, clears the date and saves it`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID)
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activeInactiveService.reactivateProperty(PROPERTY_ID)

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

        activeInactiveService.reactivateProperty(PROPERTY_ID)

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

        activeInactiveService.reactivateProperty(PROPERTY_ID)

        assertTrue(activeLandlord.isActive)
        assertTrue(inactiveLandlord.isActive)
        verify(landlordRepository).save(inactiveLandlord)
        verify(landlordRepository, never()).save(activeLandlord)
    }

    @Test
    fun `reactivateProperty throws and makes no change for an already active property`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, isActive = true)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        assertThrows<IllegalStateException> { activeInactiveService.reactivateProperty(PROPERTY_ID) }

        verify(propertyOwnershipRepository, never()).save(any())
    }

    @Test
    fun `reactivateProperty throws NOT_FOUND for an unknown property`() {
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty())

        val exception = assertThrows<ResponseStatusException> { activeInactiveService.reactivateProperty(PROPERTY_ID) }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    companion object {
        private const val PROPERTY_ID = 1L
        private val ORIGINAL_DATE = LocalDate.of(2026, 1, 1)

        private fun today() = LocalDate.now(DateTimeHelper.UK_ZONE)
    }
}
