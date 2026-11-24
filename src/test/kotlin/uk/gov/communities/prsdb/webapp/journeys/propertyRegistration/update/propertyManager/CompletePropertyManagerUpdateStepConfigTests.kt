package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyManagerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyManagerDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.services.AssociatedPartyNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
class CompletePropertyManagerUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var mockAssociatedPartyNotificationService: AssociatedPartyNotificationService

    @Mock
    private lateinit var mockState: UpdatePropertyManagerJourneyState

    @Mock
    private lateinit var mockPropertyManagerTask: PropertyManagerTask

    @Mock
    private lateinit var mockPropertyManagerStep: PropertyManagerStep

    @Mock
    private lateinit var mockPropertyManagerDetailsStep: PropertyManagerDetailsStep

    private lateinit var stepConfig: CompletePropertyManagerUpdateStepConfig

    private val propertyId = 123L

    private val updatedBullets = listOf("The property manager responsible for the property")

    private val propertyOwnership = MockLandlordData.createPropertyOwnership()

    @BeforeEach
    fun createStepConfig() {
        stepConfig =
            CompletePropertyManagerUpdateStepConfig(
                mockPropertyOwnershipService,
                mockPropertyUpdateEmailService,
                mockAssociatedPartyNotificationService,
            )
    }

    @Nested
    inner class AfterStepIsReached {
        @BeforeEach
        fun setUp() {
            whenever(mockState.propertyManagerTask).thenReturn(mockPropertyManagerTask)
            whenever(mockPropertyManagerTask.propertyManagerStep).thenReturn(mockPropertyManagerStep)
        }

        @Test
        fun `afterStepIsReached updates the property manager and sends the update email when the answer is yes`() {
            givenTheRecordWillBeUpdated()
            givenUsingPropertyManager("New Agent", "new@example.com")

            stepConfig.afterStepIsReached(mockState)

            verify(mockPropertyOwnershipService).updatePropertyManager(
                eq(propertyId),
                eq(PropertyManagerDataModel("New Agent", "new@example.com")),
                any(),
            )
            verify(mockPropertyUpdateEmailService).sendUpdateEmails(propertyId, updatedBullets)
        }

        @Test
        fun `afterStepIsReached emails the property manager when one is added`() {
            givenTheRecordWillBeUpdated()
            givenOriginalPropertyManager(null)
            givenUsingPropertyManager("New Agent", "new@example.com")
            givenThePropertyOwnershipCanBeRetrieved()

            stepConfig.afterStepIsReached(mockState)

            verify(mockAssociatedPartyNotificationService).sendNotificationToPropertyManagerAddedToProperty(
                PropertyManagerDataModel("New Agent", "new@example.com"),
                propertyOwnership,
            )
        }

        @Test
        fun `afterStepIsReached emails the property manager when their email address changes`() {
            givenTheRecordWillBeUpdated()
            givenOriginalPropertyManager(PropertyManagerDataModel("Agent Ltd", "agent@example.com"))
            givenUsingPropertyManager("Agent Ltd", "new@example.com")
            givenThePropertyOwnershipCanBeRetrieved()

            stepConfig.afterStepIsReached(mockState)

            verify(mockAssociatedPartyNotificationService).sendNotificationToPropertyManagerAddedToProperty(
                PropertyManagerDataModel("Agent Ltd", "new@example.com"),
                propertyOwnership,
            )
        }

        @Test
        fun `afterStepIsReached emails the property manager when only their name changes`() {
            givenTheRecordWillBeUpdated()
            givenOriginalPropertyManager(PropertyManagerDataModel("Agent Ltd", "agent@example.com"))
            givenUsingPropertyManager("Agent Limited", "agent@example.com")
            givenThePropertyOwnershipCanBeRetrieved()

            stepConfig.afterStepIsReached(mockState)

            verify(mockAssociatedPartyNotificationService).sendNotificationToPropertyManagerAddedToProperty(
                PropertyManagerDataModel("Agent Limited", "agent@example.com"),
                propertyOwnership,
            )
        }

        @Test
        fun `afterStepIsReached does not email the property manager when their details are unchanged`() {
            givenTheRecordWillBeUpdated()
            givenOriginalPropertyManager(PropertyManagerDataModel("Agent Ltd", "agent@example.com"))
            givenUsingPropertyManager("Agent Ltd", "agent@example.com")

            stepConfig.afterStepIsReached(mockState)

            verifyNoInteractions(mockAssociatedPartyNotificationService)
        }

        @Test
        fun `afterStepIsReached still completes when the property manager email fails to send`() {
            givenTheRecordWillBeUpdated()
            givenOriginalPropertyManager(null)
            givenUsingPropertyManager("New Agent", "new@example.com")
            givenThePropertyOwnershipCanBeRetrieved()
            whenever(mockAssociatedPartyNotificationService.sendNotificationToPropertyManagerAddedToProperty(any(), any()))
                .thenThrow(RuntimeException("Email failed to send"))

            assertDoesNotThrow { stepConfig.afterStepIsReached(mockState) }

            verify(mockPropertyUpdateEmailService).sendUpdateEmails(propertyId, updatedBullets)
        }

        @Test
        fun `afterStepIsReached removes the property manager and sends the update email when the answer is no`() {
            givenTheRecordWillBeUpdated()
            whenever(mockPropertyManagerStep.outcome).thenReturn(YesOrNo.NO)

            stepConfig.afterStepIsReached(mockState)

            verify(mockPropertyOwnershipService).removePropertyManager(eq(propertyId), any())
            verify(mockPropertyOwnershipService, never()).updatePropertyManager(any(), any(), any())
            // TODO PDJB-2171: Assert the removed property manager email is sent only when there was a property manager
            verify(mockPropertyUpdateEmailService).sendUpdateEmails(propertyId, updatedBullets)
        }

        @Test
        fun `afterStepIsReached does not send the property manager added email when the answer is no`() {
            givenTheRecordWillBeUpdated()
            whenever(mockPropertyManagerStep.outcome).thenReturn(YesOrNo.NO)

            stepConfig.afterStepIsReached(mockState)

            verify(mockAssociatedPartyNotificationService, never()).sendNotificationToPropertyManagerAddedToProperty(any(), any())
        }

        @Test
        fun `afterStepIsReached deletes the journey and rethrows when updating the property manager conflicts`() {
            givenTheRecordWillBeUpdated()
            givenUsingPropertyManager("New Agent", "new@example.com")
            whenever(mockPropertyOwnershipService.updatePropertyManager(any(), any(), any()))
                .thenThrow(UpdateConflictException::class.java)

            assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

            verify(mockState).deleteJourney()
            verify(mockPropertyUpdateEmailService, never()).sendUpdateEmails(any(), any())
            verifyNoInteractions(mockAssociatedPartyNotificationService)
        }

        @Test
        fun `afterStepIsReached deletes the journey and rethrows when removing the property manager conflicts`() {
            givenTheRecordWillBeUpdated()
            whenever(mockPropertyManagerStep.outcome).thenReturn(YesOrNo.NO)
            whenever(mockPropertyOwnershipService.removePropertyManager(any(), any()))
                .thenThrow(UpdateConflictException::class.java)

            assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

            verify(mockState).deleteJourney()
            verify(mockPropertyUpdateEmailService, never()).sendUpdateEmails(any(), any())
            verifyNoInteractions(mockAssociatedPartyNotificationService)
        }
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        val defaultDestination = Destination.Nowhere()

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        assertSame(defaultDestination, result)
        verify(mockState).deleteJourney()
    }

    private fun givenOriginalPropertyManager(propertyManager: PropertyManagerDataModel?) {
        whenever(mockState.originalPropertyManager).thenReturn(propertyManager)
    }

    private fun givenThePropertyOwnershipCanBeRetrieved() {
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
    }

    private fun givenTheRecordWillBeUpdated() {
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn("2024-01-01T00:00:00Z")
    }

    private fun givenUsingPropertyManager(
        name: String,
        email: String,
    ) {
        whenever(mockPropertyManagerStep.outcome).thenReturn(YesOrNo.YES)
        whenever(mockPropertyManagerTask.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)
        whenever(mockPropertyManagerDetailsStep.formModel).thenReturn(
            PropertyManagerDetailsFormModel().apply {
                this.name = name
                emailAddress = email
            },
        )
    }
}
