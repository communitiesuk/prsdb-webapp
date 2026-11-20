package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.services.AssociatedPartyNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class CompleteSuperiorLandlordsUpdateStepConfigTests {
    @Mock
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var associatedPartyNotificationService: AssociatedPartyNotificationService

    @Mock
    private lateinit var propertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var journeyState: UpdateSuperiorLandlordsJourneyState

    @Mock
    private lateinit var superiorLandlordsTask: SuperiorLandlordsTask

    @Mock
    private lateinit var anyoneInOwnershipChainStep: AnyoneInOwnershipChainStep

    private lateinit var stepConfig: CompleteSuperiorLandlordsUpdateStepConfig

    private val propertyId = 123L
    private val lastModifiedDate = Instant.parse("2025-01-01T00:00:00Z")
    private val superiorLandlord = SuperiorLandlordDataModel(detailsKnown = true, name = "New Superior", email = "sl@example.com")
    private val emailBullet = "People in the property’s ownership chain"

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteSuperiorLandlordsUpdateStepConfig(
                propertyOwnershipService,
                associatedPartyNotificationService,
                propertyUpdateEmailService,
            )
    }

    private fun stubAnswer(
        answer: Boolean?,
        superiorLandlordsMap: Map<Int, SuperiorLandlordDataModel>? = null,
    ) {
        whenever(journeyState.superiorLandlordsTask).thenReturn(superiorLandlordsTask)
        whenever(superiorLandlordsTask.anyoneInOwnershipChainStep).thenReturn(anyoneInOwnershipChainStep)
        whenever(anyoneInOwnershipChainStep.formModelIfReachableOrNull)
            .thenReturn(answer?.let { AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = it) })
        lenient().`when`(superiorLandlordsTask.superiorLandlordsMap).thenReturn(superiorLandlordsMap)
    }

    @Test
    fun `afterStepIsReached saves the superior landlords and sends the landlord update email`() {
        // Arrange
        whenever(journeyState.propertyId).thenReturn(propertyId)
        whenever(journeyState.lastModifiedDate).thenReturn(lastModifiedDate.toString())
        stubAnswer(true, mapOf(1 to superiorLandlord))

        // Act
        stepConfig.afterStepIsReached(journeyState)

        // Assert
        verify(propertyOwnershipService).updateSuperiorLandlords(propertyId, listOf(superiorLandlord), lastModifiedDate)
        verify(propertyOwnershipService, never()).getPropertyOwnership(any())
        verifyNoInteractions(associatedPartyNotificationService)
        verify(propertyUpdateEmailService).sendUpdateEmails(propertyId, listOf(emailBullet))
    }

    @Test
    fun `afterStepIsReached clears the superior landlords when the answer is no`() {
        // Arrange
        whenever(journeyState.propertyId).thenReturn(propertyId)
        whenever(journeyState.lastModifiedDate).thenReturn(lastModifiedDate.toString())
        stubAnswer(false, mapOf(1 to superiorLandlord))

        // Act
        stepConfig.afterStepIsReached(journeyState)

        // Assert
        verify(propertyOwnershipService).updateSuperiorLandlords(propertyId, emptyList(), lastModifiedDate)
        verify(propertyUpdateEmailService).sendUpdateEmails(propertyId, listOf(emailBullet))
    }

    @Test
    fun `afterStepIsReached notifies each added superior landlord, continuing if a notification fails`() {
        // Arrange
        val otherSuperiorLandlord = SuperiorLandlordDataModel(detailsKnown = true, name = "Other", email = "other@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        whenever(journeyState.propertyId).thenReturn(propertyId)
        whenever(journeyState.lastModifiedDate).thenReturn(lastModifiedDate.toString())
        stubAnswer(true, mapOf(1 to superiorLandlord, 2 to otherSuperiorLandlord))
        whenever(
            propertyOwnershipService.updateSuperiorLandlords(propertyId, listOf(superiorLandlord, otherSuperiorLandlord), lastModifiedDate),
        ).thenReturn(listOf(superiorLandlord, otherSuperiorLandlord))
        whenever(propertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(associatedPartyNotificationService.sendNotificationToSuperiorLandlordAddedToProperty(superiorLandlord, propertyOwnership))
            .thenThrow(RuntimeException("Notify failed"))

        // Act
        stepConfig.afterStepIsReached(journeyState)

        // Assert
        verify(associatedPartyNotificationService).sendNotificationToSuperiorLandlordAddedToProperty(superiorLandlord, propertyOwnership)
        verify(associatedPartyNotificationService)
            .sendNotificationToSuperiorLandlordAddedToProperty(otherSuperiorLandlord, propertyOwnership)
        verify(propertyUpdateEmailService).sendUpdateEmails(propertyId, listOf(emailBullet))
    }

    @Test
    fun `afterStepIsReached throws when the answer is yes but there are no superior landlords`() {
        // Arrange
        whenever(journeyState.journeyId).thenReturn("journey-id")
        stubAnswer(true, emptyMap())

        // Act & Assert
        assertThrows(UnrecoverableJourneyStateException::class.java) {
            stepConfig.afterStepIsReached(journeyState)
        }
        verifyNoInteractions(propertyOwnershipService, propertyUpdateEmailService)
    }

    @Test
    fun `afterStepIsReached deletes the journey and rethrows when there is an update conflict`() {
        // Arrange
        whenever(journeyState.propertyId).thenReturn(propertyId)
        whenever(journeyState.lastModifiedDate).thenReturn(lastModifiedDate.toString())
        stubAnswer(true, mapOf(1 to superiorLandlord))
        doThrow(UpdateConflictException("Conflict"))
            .whenever(propertyOwnershipService)
            .updateSuperiorLandlords(propertyId, listOf(superiorLandlord), lastModifiedDate)

        // Act & Assert
        assertThrows(UpdateConflictException::class.java) {
            stepConfig.afterStepIsReached(journeyState)
        }
        verify(journeyState).deleteJourney()
        verifyNoInteractions(associatedPartyNotificationService, propertyUpdateEmailService)
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        // Arrange
        val defaultDestination = Destination.ExternalUrl("/landlord/property-details/1#people-responsible")

        // Act
        val result = stepConfig.resolveNextDestination(journeyState, defaultDestination)

        // Assert
        verify(journeyState).deleteJourney()
        assertEquals(defaultDestination, result)
    }
}
