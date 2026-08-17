package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.leadTrustee

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class UpdateLeadTrusteeCyaStepConfig : AbstractCheckYourAnswersStepConfig<UpdateLeadTrusteeJourneyState>() {
    override fun chooseTemplate(state: UpdateLeadTrusteeJourneyState) = "forms/checkLeadTrusteeAnswersForm"

    override fun getStepSpecificContent(state: UpdateLeadTrusteeJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "landlordDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "leadTrusteeCard" to getLeadTrusteeCard(state),
        )

    override fun resolveNextDestination(
        state: UpdateLeadTrusteeJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun getLeadTrusteeCard(state: UpdateLeadTrusteeJourneyState): SummaryCardViewModel {
        val leadTrusteeTask = state.leadTrusteeTask
        return SummaryCardViewModel(
            title = "landlordDetails.org.leadTrusteeHeading",
            summaryList =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "landlordDetails.org.leadTrusteeName",
                        leadTrusteeTask.leadTrusteeNameStep.formModel.notNullValue(LeadTrusteeNameFormModel::name),
                        Destination.VisitableStep(
                            leadTrusteeTask.leadTrusteeNameStep,
                            state.getCyaJourneyId(leadTrusteeTask.leadTrusteeNameStep),
                        ),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "landlordDetails.org.leadTrusteeDateOfBirth",
                        leadTrusteeTask.leadTrusteeDobStep.formModel.toLocalDateOrNull(),
                        Destination.VisitableStep(
                            leadTrusteeTask.leadTrusteeDobStep,
                            state.getCyaJourneyId(leadTrusteeTask.leadTrusteeDobStep),
                        ),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "landlordDetails.org.leadTrusteeEmail",
                        leadTrusteeTask.leadTrusteeEmailStep.formModel.notNullValue(LeadTrusteeEmailFormModel::emailAddress),
                        Destination.VisitableStep(
                            leadTrusteeTask.leadTrusteeEmailStep,
                            state.getCyaJourneyId(leadTrusteeTask.leadTrusteeEmailStep),
                        ),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "landlordDetails.org.leadTrusteePhone",
                        leadTrusteeTask.leadTrusteePhoneStep.formModel.notNullValue(LeadTrusteePhoneFormModel::phoneNumber),
                        Destination.VisitableStep(
                            leadTrusteeTask.leadTrusteePhoneStep,
                            state.getCyaJourneyId(leadTrusteeTask.leadTrusteePhoneStep),
                        ),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "landlordDetails.org.leadTrusteeAddress",
                        leadTrusteeTask.trusteeAddressTask
                            .getAddress()
                            .toMultiLineAddress()
                            .split("\n"),
                        Destination.VisitableStep(
                            leadTrusteeTask.trusteeAddressTask.lookupAddressStep,
                            state.getCyaJourneyId(leadTrusteeTask.trusteeAddressTask.lookupAddressStep),
                        ),
                    ),
                ),
        )
    }
}

@JourneyFrameworkComponent
final class UpdateLeadTrusteeCyaStep(
    stepConfig: UpdateLeadTrusteeCyaStepConfig,
) : AbstractCheckYourAnswersStep<UpdateLeadTrusteeJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "lead-trustee-check-your-answers"
    }
}
