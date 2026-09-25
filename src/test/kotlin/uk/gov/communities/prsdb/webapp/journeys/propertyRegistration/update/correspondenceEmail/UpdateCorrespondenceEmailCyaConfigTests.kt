package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CorrespondenceEmailOption
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CorrespondenceEmailFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class UpdateCorrespondenceEmailCyaConfigTests {
    private val propertyOwnershipService = mock<PropertyOwnershipService>()
    private val propertyUpdateEmailService = mock<PropertyUpdateEmailService>()
    private val state = mock<UpdateCorrespondenceEmailJourneyState>()
    private val emailStep = mock<CorrespondenceEmailStep>()
    private val formModel = CorrespondenceEmailFormModel()
    private val initialLastModifiedDate = Instant.parse("2026-09-01T12:00:00Z")
    private val stepConfig = UpdateCorrespondenceEmailCyaConfig(propertyOwnershipService, propertyUpdateEmailService)

    init {
        whenever(state.propertyId).thenReturn(1)
        whenever(state.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(state.loggedInLandlordEmailAtStartOfJourney).thenReturn("account@example.com")
        whenever(state.correspondenceEmailStep).thenReturn(emailStep)
        whenever(emailStep.formModel).thenReturn(formModel)
        whenever(emailStep.urlPath).thenReturn(CorrespondenceEmailStep.ROUTE_SEGMENT)
        whenever(emailStep.isStepReachable).thenReturn(true)
        whenever(state.getCyaJourneyId(emailStep)).thenReturn("child-journey")
    }

    @Test
    fun `CYA shows the account snapshot with a child journey change link and does not save`() {
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.ACCOUNT_EMAIL
        formModel.differentEmailAddress = "not-an-email"

        val content = stepConfig.getStepSpecificContent(state)
        val row = (content["summaryListData"] as List<*>).single() as SummaryListRowViewModel

        assertEquals("account@example.com", row.fieldValue)
        assertEquals("forms.update.correspondenceEmail.emailAddress", row.fieldHeading)
        assertEquals(
            Destination.VisitableStep(emailStep, "child-journey").toUrlStringOrNull(),
            row.actions.single().url,
        )
        assertEquals("forms.update.correspondenceEmail.summaryName", content["summaryName"])
        assertEquals("forms.buttons.confirmAndSubmitUpdate", content["submitButtonText"])
        assertEquals(true, content["showWarning"])
        assertEquals(true, content["insetText"])
        verifyNoInteractions(propertyOwnershipService, propertyUpdateEmailService)
    }

    @Test
    fun `CYA shows the different email address`() {
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.DIFFERENT_EMAIL
        formModel.differentEmailAddress = "different@example.com"

        val content = stepConfig.getStepSpecificContent(state)
        val row = (content["summaryListData"] as List<*>).single() as SummaryListRowViewModel

        assertEquals("different@example.com", row.fieldValue)
        verifyNoInteractions(propertyOwnershipService, propertyUpdateEmailService)
    }

    @Test
    fun `confirming the account option saves the snapshot rather than a stale custom value`() {
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.ACCOUNT_EMAIL
        formModel.differentEmailAddress = "stale@example.com"

        stepConfig.afterStepDataIsAdded(state)

        verify(propertyOwnershipService).updateCorrespondenceEmail(1, "account@example.com", initialLastModifiedDate)
    }

    @Test
    fun `confirming the different option saves the entered email`() {
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.DIFFERENT_EMAIL
        formModel.differentEmailAddress = "different@example.com"

        stepConfig.afterStepDataIsAdded(state)

        verify(propertyOwnershipService).updateCorrespondenceEmail(1, "different@example.com", initialLastModifiedDate)
    }

    @Test
    fun `confirming the update sends the property update emails after saving`() {
        // Arrange
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.DIFFERENT_EMAIL
        formModel.differentEmailAddress = "different@example.com"

        // Act
        stepConfig.afterStepDataIsAdded(state)

        // Assert
        val inOrder = inOrder(propertyOwnershipService, propertyUpdateEmailService)
        inOrder.verify(propertyOwnershipService)
            .updateCorrespondenceEmail(1, "different@example.com", initialLastModifiedDate)
        inOrder.verify(propertyUpdateEmailService)
            .sendUpdateEmails(1, listOf("The email address the council should contact"))
    }

    @Test
    fun `a missing account snapshot throws instead of saving a fallback`() {
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.ACCOUNT_EMAIL
        whenever(state.loggedInLandlordEmailAtStartOfJourney).thenReturn(null)

        assertThrows<PrsdbWebException> { stepConfig.afterStepDataIsAdded(state) }

        verifyNoInteractions(propertyOwnershipService, propertyUpdateEmailService)
    }

    @Test
    fun `a conflict deletes the stale journey and rethrows`() {
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.ACCOUNT_EMAIL
        whenever(propertyOwnershipService.updateCorrespondenceEmail(any(), any(), any()))
            .thenThrow(UpdateConflictException::class.java)

        assertThrows<UpdateConflictException> { stepConfig.afterStepDataIsAdded(state) }

        verify(state).deleteJourney()
        verifyNoInteractions(propertyUpdateEmailService)
    }

    @Test
    fun `an unexpected save failure propagates without deleting the journey`() {
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.ACCOUNT_EMAIL
        val failure = IllegalStateException("Save failed")
        whenever(propertyOwnershipService.updateCorrespondenceEmail(any(), any(), any())).thenThrow(failure)

        assertSame(failure, assertThrows<IllegalStateException> { stepConfig.afterStepDataIsAdded(state) })
        verify(state, never()).deleteJourney()
        verifyNoInteractions(propertyUpdateEmailService)
    }

    @Test
    fun `successful submission deletes the completed journey and preserves the return destination`() {
        val destination = Destination.ExternalUrl("/landlord/property-details/1")

        assertSame(destination, stepConfig.resolveNextDestination(state, destination))
        verify(state).deleteJourney()
        assertTrue(destination.toUrlStringOrNull().endsWith("/1"))
    }
}
