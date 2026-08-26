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
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.CompleteOccupancyUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
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
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockState: UpdateOccupancyJourneyState

    @Mock
    private lateinit var mockOccupiedStep: OccupiedStep

    @Mock
    private lateinit var mockOccupancyFormModel: OccupancyFormModel

    @Mock
    private lateinit var mockLettingAgentAccess: LettingAgentAccess

    private lateinit var stepConfig: CompleteOccupancyUpdateStepConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteOccupancyUpdateStepConfig(
                propertyOwnershipService = mockPropertyOwnershipService,
                propertyUpdateEmailService = mockPropertyUpdateEmailService,
                lettingAgentAccessService = mockLettingAgentAccessService,
                featureFlagManager = mockFeatureFlagManager,
            )
    }

    @Test
    fun `afterStepIsReached calls updateIsOccupied on propertyOwnershipService`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateIsOccupied(
            id = propertyId,
            isOccupied = true,
            initialLastModifiedDate = initialLastModifiedDate,
        )
    }

    @Test
    fun `afterStepIsReached sends standard update email when becoming occupied`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(listOf("Whether the property is occupied by tenants")),
        )
    }

    @Test
    fun `afterStepIsReached sends letting agent removal email when becoming unoccupied and letting agent exists and flag is enabled`() {
        // Arrange
        val lettingAgentEmail = "agent@example.com"
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(false)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        whenever(mockLettingAgentAccessService.getInvitationByPropertyOwnershipId(propertyId))
            .thenReturn(mockLettingAgentAccess)
        whenever(mockLettingAgentAccess.invitedEmail).thenReturn(lettingAgentEmail)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyUpdateEmailService).sendUpdateWithLettingAgentRemovedEmails(
            eq(propertyId),
            eq("The property was made unoccupied"),
            eq(lettingAgentEmail),
        )
        verify(mockPropertyUpdateEmailService, never()).sendUpdateEmails(any(), any())
    }

    @Test
    fun `afterStepIsReached sends standard update email when becoming unoccupied but no letting agent exists and flag is enabled`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(false)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        whenever(mockLettingAgentAccessService.getInvitationByPropertyOwnershipId(propertyId))
            .thenReturn(null)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(listOf("Whether the property is occupied by tenants")),
        )
        verify(mockPropertyUpdateEmailService, never()).sendUpdateWithLettingAgentRemovedEmails(any(), any(), any())
    }

    @Test
    fun `afterStepIsReached sends standard update email when becoming unoccupied and flag is disabled`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(false)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(listOf("Whether the property is occupied by tenants")),
        )
        verify(mockPropertyUpdateEmailService, never()).sendUpdateWithLettingAgentRemovedEmails(any(), any(), any())
    }

    @Test
    fun `afterStepIsReached sends standard update email when becoming occupied and flag is enabled`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        whenever(mockLettingAgentAccessService.getInvitationByPropertyOwnershipId(propertyId))
            .thenReturn(mockLettingAgentAccess)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(listOf("Whether the property is occupied by tenants")),
        )
        verify(mockPropertyUpdateEmailService, never()).sendUpdateWithLettingAgentRemovedEmails(any(), any(), any())
    }

    @Test
    fun `afterStepIsReached deletes the journey then rethrows when it gets an UpdateConflictException`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)
        whenever(
            mockPropertyOwnershipService.updateIsOccupied(
                id = propertyId,
                isOccupied = true,
                initialLastModifiedDate = initialLastModifiedDate,
            ),
        ).thenThrow(UpdateConflictException::class.java)

        // Act, assert
        assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

        verify(mockState).deleteJourney()
    }

    @Test
    fun `resolveNextDestination calls deleteJourney on state`() {
        // Act
        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        // Assert
        verify(mockState).deleteJourney()
    }
}
