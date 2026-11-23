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
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.NoLandlordEmailRecipientsException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.time.LocalDate
import java.util.Optional
import java.util.function.Consumer

@ExtendWith(MockitoExtension::class)
class DeactivationServiceTests {
    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var landlordRepository: LandlordRepository

    @Mock
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @Mock
    private lateinit var deactivationEmailService: DeactivationEmailService

    @Mock
    private lateinit var transactionTemplate: TransactionTemplate

    // TODO PDJB-2149: Remove this mock when the ACTIVE_INACTIVE flag is removed
    @Mock
    private lateinit var featureFlagManager: FeatureFlagManager

    @InjectMocks
    private lateinit var deactivationService: DeactivationService

    @Test
    fun `deactivateLandlord marks an active landlord inactive and saves it without emailing them when ACTIVE_INACTIVE is disabled`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        deactivationService.deactivateLandlord(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)

        assertFalse(landlord.isActive)
        assertEquals(today(), landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
        verifyNoInteractions(deactivationEmailService)
    }

    @Test
    fun `deactivateLandlord emails the landlord that their account is deactivated when ACTIVE_INACTIVE is enabled`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        val landlord = MockLandlordData.createIndividualLandlord()

        deactivationService.deactivateLandlord(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)

        verify(deactivationEmailService).sendLandlordDeactivatedEmail(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)
    }

    @Test
    fun `deactivateLandlord still deactivates the landlord when the email fails to send`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        val landlord = MockLandlordData.createIndividualLandlord()
        doThrow(NoLandlordEmailRecipientsException(landlord.id))
            .whenever(deactivationEmailService).sendLandlordDeactivatedEmail(any(), any())

        deactivationService.deactivateLandlord(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)

        assertFalse(landlord.isActive)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `deactivateLandlord throws and makes no change for an already inactive landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        assertThrows<IllegalStateException> {
            deactivationService.deactivateLandlord(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)
        }

        assertEquals(ORIGINAL_DATE, landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
        verifyNoInteractions(deactivationEmailService)
    }

    @Test
    fun `deactivateLandlord throws and makes no change when the landlord still has active properties`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(true)

        assertThrows<IllegalStateException> {
            deactivationService.deactivateLandlord(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)
        }

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
        verifyNoInteractions(deactivationEmailService)
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

        verify(deactivationEmailService).sendLettingAgentPropertyDeactivatedEmail(property, LETTING_AGENT_EMAIL)
    }

    @Test
    fun `deactivateProperty only emails the letting agent after the transaction commits`() {
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
        TransactionSynchronizationManager.initSynchronization()
        try {
            deactivationService.deactivateProperty(PROPERTY_ID)

            verifyNoInteractions(deactivationEmailService)
            TransactionSynchronizationManager.getSynchronizations().forEach { it.afterCommit() }
            verify(deactivationEmailService).sendLettingAgentPropertyDeactivatedEmail(property, LETTING_AGENT_EMAIL)
        } finally {
            TransactionSynchronizationManager.clearSynchronization()
        }
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

        verifyNoInteractions(deactivationEmailService)
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

        verifyNoInteractions(deactivationEmailService)
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
            .whenever(deactivationEmailService)
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

    @Test
    fun `batch property deactivation queries with the UK-local cutoff and continues after an individual failure`() {
        val firstSuccessfulProperty = MockLandlordData.createPropertyOwnership(id = 2L, landlords = mutableSetOf())
        val secondSuccessfulProperty = MockLandlordData.createPropertyOwnership(id = 3L, landlords = mutableSetOf())
        whenever(propertyOwnershipRepository.findActivePropertyIdsWithAnyProvideLaterTaskCreatedBefore(cutoff()))
            .thenReturn(listOf(PROPERTY_ID, firstSuccessfulProperty.id, secondSuccessfulProperty.id))

        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty())
        whenever(propertyOwnershipRepository.findById(firstSuccessfulProperty.id)).thenReturn(Optional.of(firstSuccessfulProperty))
        whenever(propertyOwnershipRepository.findById(secondSuccessfulProperty.id)).thenReturn(Optional.of(secondSuccessfulProperty))
        doAnswer { invocation ->
            invocation.getArgument<Consumer<TransactionStatus>>(0).accept(SimpleTransactionStatus())
        }.whenever(transactionTemplate).executeWithoutResult(any())

        val failureCount = deactivationService.deactivateMissingDetailPropertiesPastDueDateReturningFailureCount()

        assertEquals(1, failureCount)
        verify(propertyOwnershipRepository).findActivePropertyIdsWithAnyProvideLaterTaskCreatedBefore(cutoff())
        verify(transactionTemplate, times(3)).executeWithoutResult(any())

        verify(lettingAgentAccessService, never()).deleteDelegationByPropertyOwnershipId(PROPERTY_ID)
        verify(lettingAgentAccessService).deleteDelegationByPropertyOwnershipId(firstSuccessfulProperty.id)
        verify(lettingAgentAccessService).deleteDelegationByPropertyOwnershipId(secondSuccessfulProperty.id)
    }

    @Test
    fun `batch property deactivation does nothing when there are no properties created before the cut-off with provide later tasks`() {
        whenever(propertyOwnershipRepository.findActivePropertyIdsWithAnyProvideLaterTaskCreatedBefore(cutoff()))
            .thenReturn(emptyList())

        val failureCount = deactivationService.deactivateMissingDetailPropertiesPastDueDateReturningFailureCount()

        assertEquals(0, failureCount)
        verifyNoInteractions(transactionTemplate)
    }

    companion object {
        private const val PROPERTY_ID = 1L
        private const val LETTING_AGENT_EMAIL = "letting.agent@example.com"
        private val ORIGINAL_DATE = LocalDate.of(2026, 1, 1)

        private fun today() = LocalDate.now(DateTimeHelper.UK_ZONE)

        private fun cutoff() =
            LocalDate
                .now(DateTimeHelper.UK_ZONE)
                .minusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
                .atStartOfDay(DateTimeHelper.UK_ZONE)
                .toInstant()
    }
}
