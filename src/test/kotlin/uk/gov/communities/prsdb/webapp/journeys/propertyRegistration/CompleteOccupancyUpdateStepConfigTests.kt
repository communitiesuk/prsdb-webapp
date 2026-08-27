package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.CompleteOccupancyUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.services.DelegateToLettingAgentEmailService
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@ExtendWith(MockitoExtension::class)
class CompleteOccupancyUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var mockLettingAgentAccessService: LettingAgentAccessService

    @Mock
    private lateinit var mockDelegateToLettingAgentEmailService: DelegateToLettingAgentEmailService

    @Mock
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockState: UpdateOccupancyJourneyState

    @Mock
    private lateinit var mockOccupiedStep: OccupiedStep

    @Mock
    private lateinit var mockOccupancyFormModel: OccupancyFormModel

    @Mock
    private lateinit var mockLettingAgentAccess: LettingAgentAccess

    @Mock
    private lateinit var mockPropertyOwnership: PropertyOwnership

    private lateinit var stepConfig: CompleteOccupancyUpdateStepConfig

    private val propertyId = 123L
    private val lettingAgentEmail = "agent@example.com"
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteOccupancyUpdateStepConfig(
                propertyOwnershipService = mockPropertyOwnershipService,
                propertyUpdateEmailService = mockPropertyUpdateEmailService,
                lettingAgentAccessService = mockLettingAgentAccessService,
                delegateToLettingAgentEmailService = mockDelegateToLettingAgentEmailService,
                featureFlagManager = mockFeatureFlagManager,
            )
    }

    private fun stubStateWithOccupancy(occupied: Boolean) {
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(occupied)
    }

    @Test
    fun `afterStepIsReached calls updateIsOccupied on propertyOwnershipService`() {
        stubStateWithOccupancy(occupied = true)

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyOwnershipService).updateIsOccupied(
            id = propertyId,
            isOccupied = true,
            initialLastModifiedDate = initialLastModifiedDate,
        )
    }

    @Test
    fun `afterStepIsReached sends the standard update email and does not remove a delegation when becoming occupied`() {
        stubStateWithOccupancy(occupied = true)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        whenever(mockLettingAgentAccessService.getInvitationByPropertyOwnershipId(propertyId))
            .thenReturn(mockLettingAgentAccess)

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(listOf("Whether the property is occupied by tenants")),
        )
        verify(mockPropertyUpdateEmailService, never()).sendUpdateWithLettingAgentRemovedEmails(any(), any(), any())
        verify(mockLettingAgentAccessService, never()).deleteDelegationByPropertyOwnershipId(any())
        verify(mockDelegateToLettingAgentEmailService, never()).sendLettingAgentCancellationEmail(any(), any())
    }

    @Test
    fun `afterStepIsReached removes delegation and emails landlords and agent when becoming unoccupied with a delegation`() {
        stubStateWithOccupancy(occupied = false)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        whenever(mockLettingAgentAccessService.getInvitationByPropertyOwnershipId(propertyId))
            .thenReturn(mockLettingAgentAccess)
        whenever(mockLettingAgentAccess.invitedEmail).thenReturn(lettingAgentEmail)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(mockPropertyOwnership)

        stepConfig.afterStepIsReached(mockState)

        verify(mockLettingAgentAccessService).deleteDelegationByPropertyOwnershipId(propertyId)
        verify(mockPropertyUpdateEmailService).sendUpdateWithLettingAgentRemovedEmails(
            eq(propertyId),
            eq("The property was made unoccupied"),
            eq(lettingAgentEmail),
        )
        verify(mockDelegateToLettingAgentEmailService)
            .sendLettingAgentCancellationEmail(mockPropertyOwnership, lettingAgentEmail)
        verify(mockPropertyUpdateEmailService, never()).sendUpdateEmails(any(), any())
    }

    @Test
    fun `afterStepIsReached sends standard email and removes no delegation when becoming unoccupied with no delegation`() {
        stubStateWithOccupancy(occupied = false)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        whenever(mockLettingAgentAccessService.getInvitationByPropertyOwnershipId(propertyId)).thenReturn(null)

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(listOf("Whether the property is occupied by tenants")),
        )
        verify(mockPropertyUpdateEmailService, never()).sendUpdateWithLettingAgentRemovedEmails(any(), any(), any())
        verify(mockLettingAgentAccessService, never()).deleteDelegationByPropertyOwnershipId(any())
        verify(mockDelegateToLettingAgentEmailService, never()).sendLettingAgentCancellationEmail(any(), any())
    }

    @Test
    fun `afterStepIsReached sends standard email and removes no delegation when becoming unoccupied with the flag disabled`() {
        stubStateWithOccupancy(occupied = false)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(listOf("Whether the property is occupied by tenants")),
        )
        verify(mockPropertyUpdateEmailService, never()).sendUpdateWithLettingAgentRemovedEmails(any(), any(), any())
        verify(mockLettingAgentAccessService, never()).deleteDelegationByPropertyOwnershipId(any())
        verify(mockDelegateToLettingAgentEmailService, never()).sendLettingAgentCancellationEmail(any(), any())
    }

    @Test
    fun `afterStepIsReached deletes the journey then rethrows when it gets an UpdateConflictException`() {
        stubStateWithOccupancy(occupied = true)
        whenever(
            mockPropertyOwnershipService.updateIsOccupied(
                id = propertyId,
                isOccupied = true,
                initialLastModifiedDate = initialLastModifiedDate,
            ),
        ).thenThrow(UpdateConflictException::class.java)

        assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

        verify(mockState).deleteJourney()
    }

    @Test
    fun `resolveNextDestination calls deleteJourney on state`() {
        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        verify(mockState).deleteJourney()
    }
}
