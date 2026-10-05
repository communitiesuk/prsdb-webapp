package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CorrespondenceEmailOption
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CorrespondenceEmailFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import java.time.Instant
import kotlin.test.assertEquals

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
    fun `getStepSpecificContent shows the journey information with a change link`() {
        // Arrange
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.ACCOUNT_EMAIL

        // Act
        val content = stepConfig.getStepSpecificContent(state)

        // Assert
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
    }

    @Test
    fun `afterStepDataIsAdded updates the correspondence email with the email address from the form model`() {
        // Arrange
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.DIFFERENT_EMAIL
        formModel.differentEmailAddress = "different@example.com"
        val expectedEmail = formModel.getEmailAddress { state.loggedInLandlordEmailAtStartOfJourney }

        // Act
        stepConfig.afterStepDataIsAdded(state)

        // Assert
        verify(propertyOwnershipService).updateCorrespondenceEmail(1, expectedEmail, initialLastModifiedDate)
    }

    @Test
    fun `afterStepDataIsAdded sends the property update emails after updating the correspondence email`() {
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
    fun `afterStepDataIsAdded deletes the journey and rethrows when there is an update conflict`() {
        // Arrange
        formModel.correspondenceEmailOption = CorrespondenceEmailOption.ACCOUNT_EMAIL
        whenever(propertyOwnershipService.updateCorrespondenceEmail(any(), any(), any()))
            .thenThrow(UpdateConflictException::class.java)

        // Act & Assert
        assertThrows<UpdateConflictException> { stepConfig.afterStepDataIsAdded(state) }
        verify(state).deleteJourney()
        verifyNoInteractions(propertyUpdateEmailService)
    }
}
