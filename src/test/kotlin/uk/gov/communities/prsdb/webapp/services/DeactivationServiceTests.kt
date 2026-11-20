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
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class DeactivationServiceTests {
    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var landlordRepository: LandlordRepository

    @Mock
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @Mock
    private lateinit var delegateToLettingAgentEmailService: DelegateToLettingAgentEmailService

    // TODO PDJB-2149: Remove this mock when the ACTIVE_INACTIVE flag is removed
    @Mock
    private lateinit var featureFlagManager: FeatureFlagManager

    @InjectMocks
    private lateinit var deactivationService: DeactivationService

    @Test
    fun `deactivateLandlord marks an active landlord inactive with today's date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        deactivationService.deactivateLandlord(landlord)

        assertFalse(landlord.isActive)
        assertEquals(today(), landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `deactivateLandlord throws and makes no change for an already inactive landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        assertThrows<IllegalStateException> { deactivationService.deactivateLandlord(landlord) }

        assertEquals(ORIGINAL_DATE, landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateLandlord throws and makes no change when the landlord still has active properties`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(true)

        assertThrows<IllegalStateException> { deactivationService.deactivateLandlord(landlord) }

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties deactivates an active landlord with no active properties`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(false)

        deactivationService.deactivateLandlordIfNoActiveProperties(landlord)

        assertFalse(landlord.isActive)
        assertEquals(today(), landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties makes no change when the landlord still has active properties`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(true)

        deactivationService.deactivateLandlordIfNoActiveProperties(landlord)

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties makes no change for an already inactive landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        deactivationService.deactivateLandlordIfNoActiveProperties(landlord)

        assertEquals(ORIGINAL_DATE, landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateProperty marks the property inactive with today's date, saves it and removes any delegation`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(true)

        deactivationService.deactivateProperty(PROPERTY_ID)

        assertFalse(property.isActive)
        assertEquals(today(), property.lastDeactivatedDate)
        verify(propertyOwnershipRepository).save(property)
        verify(lettingAgentAccessService).deleteDelegationByPropertyOwnershipId(PROPERTY_ID)
    }

    @Test
    fun `deactivateProperty emails the letting agent when the property had a delegation`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        val delegation =
            MockLettingAgentData.createLettingAgentAccess(invitedEmail = LETTING_AGENT_EMAIL, propertyOwnership = property)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(true)
        whenever(lettingAgentAccessService.getDelegationByPropertyOwnershipIdOrNull(PROPERTY_ID)).thenReturn(delegation)
        // TODO PDJB-2149: Remove this stub when the ACTIVE_INACTIVE flag is removed
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)

        deactivationService.deactivateProperty(PROPERTY_ID)

        verify(delegateToLettingAgentEmailService).sendLettingAgentPropertyDeactivatedEmail(property, LETTING_AGENT_EMAIL)
    }

    @Test
    fun `deactivateProperty does not email a letting agent when the property had no delegation`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(true)
        whenever(lettingAgentAccessService.getDelegationByPropertyOwnershipIdOrNull(PROPERTY_ID)).thenReturn(null)

        deactivationService.deactivateProperty(PROPERTY_ID)

        verifyNoInteractions(delegateToLettingAgentEmailService)
    }

    // TODO PDJB-2149: Remove this test when the ACTIVE_INACTIVE flag is removed
    @Test
    fun `deactivateProperty does not email the letting agent when ACTIVE_INACTIVE is disabled`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        val delegation =
            MockLettingAgentData.createLettingAgentAccess(invitedEmail = LETTING_AGENT_EMAIL, propertyOwnership = property)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(true)
        whenever(lettingAgentAccessService.getDelegationByPropertyOwnershipIdOrNull(PROPERTY_ID)).thenReturn(delegation)
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(false)

        deactivationService.deactivateProperty(PROPERTY_ID)

        verifyNoInteractions(delegateToLettingAgentEmailService)
    }

    @Test
    fun `deactivateProperty still deactivates the property when the letting agent email fails to send`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        val delegation =
            MockLettingAgentData.createLettingAgentAccess(invitedEmail = LETTING_AGENT_EMAIL, propertyOwnership = property)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlord.id, PROPERTY_ID))
            .thenReturn(true)
        whenever(lettingAgentAccessService.getDelegationByPropertyOwnershipIdOrNull(PROPERTY_ID)).thenReturn(delegation)
        // TODO PDJB-2149: Remove this stub when the ACTIVE_INACTIVE flag is removed
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        doThrow(RuntimeException("Notify is down"))
            .whenever(delegateToLettingAgentEmailService)
            .sendLettingAgentPropertyDeactivatedEmail(any(), any())

        deactivationService.deactivateProperty(PROPERTY_ID)

        assertFalse(property.isActive)
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

        deactivationService.deactivateProperty(PROPERTY_ID)

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

        deactivationService.deactivateProperty(PROPERTY_ID)

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

        deactivationService.deactivateProperty(PROPERTY_ID)

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

        assertThrows<IllegalStateException> { deactivationService.deactivateProperty(PROPERTY_ID) }

        assertEquals(ORIGINAL_DATE, landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `deactivateProperty throws and makes no change for an already inactive property`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        val property =
            MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord), isActive = false)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        assertThrows<IllegalStateException> { deactivationService.deactivateProperty(PROPERTY_ID) }

        assertNull(property.lastDeactivatedDate)
        assertTrue(landlord.isActive)
        verify(propertyOwnershipRepository, never()).save(any())
        verifyNoInteractions(lettingAgentAccessService, landlordRepository)
    }

    @Test
    fun `deactivateProperty throws NOT_FOUND for an unknown property`() {
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty())

        val exception =
            assertThrows<ResponseStatusException> { deactivationService.deactivateProperty(PROPERTY_ID) }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    companion object {
        private const val PROPERTY_ID = 1L
        private const val LETTING_AGENT_EMAIL = "letting.agent@example.com"
        private val ORIGINAL_DATE = LocalDate.of(2026, 1, 1)

        private fun today() = LocalDate.now(DateTimeHelper.UK_ZONE)
    }
}
