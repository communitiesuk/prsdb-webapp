package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import java.time.Instant

@JourneyFrameworkComponent
class UpdateCorrespondenceEmailCyaConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
) : AbstractCheckYourAnswersStepConfig<UpdateCorrespondenceEmailJourneyState>() {
    override fun getStepSpecificContent(state: UpdateCorrespondenceEmailJourneyState): Map<String, Any> {
        val email =
            state.correspondenceEmailStep.formModel.getEmailAddress { state.loggedInLandlordEmailAtStartOfJourney }
        return mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "insetText" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "summaryName" to "forms.update.correspondenceEmail.summaryName",
            "summaryListData" to
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        fieldHeading = "forms.update.correspondenceEmail.emailAddress",
                        fieldValue = email,
                        destination =
                            Destination.VisitableStep(
                                state.correspondenceEmailStep,
                                state.getCyaJourneyId(state.correspondenceEmailStep),
                            ),
                    ),
                ),
        )
    }

    override fun afterStepDataIsAdded(state: UpdateCorrespondenceEmailJourneyState) {
        val email =
            state.correspondenceEmailStep.formModel.getEmailAddress { state.loggedInLandlordEmailAtStartOfJourney }
        try {
            propertyOwnershipService.updateCorrespondenceEmail(
                id = state.propertyId,
                email = email,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
        propertyUpdateEmailService.sendUpdateEmails(
            state.propertyId,
            listOf("The email address the council should contact"),
        )
    }
}

@JourneyFrameworkComponent
class UpdateCorrespondenceEmailCyaStep(
    stepConfig: UpdateCorrespondenceEmailCyaConfig,
) : AbstractCheckYourAnswersStep<UpdateCorrespondenceEmailJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "correspondence-email-check-your-answers"
    }
}
