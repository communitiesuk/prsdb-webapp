package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CorrespondenceEmailOption
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CorrespondenceEmailFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import java.time.Instant

@JourneyFrameworkComponent
class UpdateCorrespondenceEmailCyaConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
) : AbstractCheckYourAnswersStepConfig<UpdateCorrespondenceEmailJourneyState>() {
    override fun getStepSpecificContent(state: UpdateCorrespondenceEmailJourneyState): Map<String, Any> =
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "insetText" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "summaryName" to "forms.update.correspondenceEmail.summaryName",
            "summaryListData" to
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        fieldHeading = "forms.update.correspondenceEmail.emailAddress",
                        fieldValue = getCorrespondenceEmail(state),
                        destination =
                            Destination.VisitableStep(
                                state.correspondenceEmailStep,
                                state.getCyaJourneyId(state.correspondenceEmailStep),
                            ),
                    ),
                ),
        )

    override fun afterStepDataIsAdded(state: UpdateCorrespondenceEmailJourneyState) {
        try {
            propertyOwnershipService.updateCorrespondenceEmail(
                id = state.propertyId,
                email = getCorrespondenceEmail(state),
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

    private fun getCorrespondenceEmail(state: UpdateCorrespondenceEmailJourneyState): String {
        val formModel = state.correspondenceEmailStep.formModel
        return when (formModel.notNullValue(CorrespondenceEmailFormModel::correspondenceEmailOption)) {
            CorrespondenceEmailOption.ACCOUNT_EMAIL ->
                state.loggedInLandlordEmailAtStartOfJourney
                    ?: throw PrsdbWebException("Account email is missing from the correspondence email update journey")

            CorrespondenceEmailOption.DIFFERENT_EMAIL -> formModel.differentEmailAddress
        }
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
