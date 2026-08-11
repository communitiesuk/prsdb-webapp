package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class OrgTypeCyaStepConfig(
    private val messageSource: MessageSource,
) : AbstractCheckYourAnswersStepConfig<UpdateOrganisationTypeJourneyState>() {
    override fun chooseTemplate(state: UpdateOrganisationTypeJourneyState) = "forms/checkOrganisationTypeAnswersForm"

    override fun getStepSpecificContent(state: UpdateOrganisationTypeJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "landlordDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "summaryListData" to getOrgTypeRows(state),
            "leadTrusteeCard" to getLeadTrusteeCard(state),
        )

    // override this as the next step will handle deleting the journey
    override fun resolveNextDestination(
        state: UpdateOrganisationTypeJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun getOrgTypeRows(state: UpdateOrganisationTypeJourneyState): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "landlordDetails.org.organisationType",
                state.orgTypeStep.formModel
                    .getSelectedOrgTypes()
                    .joinToString(", ") { messageSource.getMessageForKey(MessageKeyConverter.convert(it)) },
                Destination.VisitableStep(state.orgTypeStep, state.getCyaJourneyId(state.orgTypeStep)),
            ),
        )

    private fun getLeadTrusteeCard(state: UpdateOrganisationTypeJourneyState): SummaryCardViewModel? {
        if (state.orgTypeUpdateRoutingStep.outcome != OrgTypeUpdateRouteMode.ADDING_TRUST) return null

        return SummaryCardViewModel(
            title = "updateLandlordDetails.checkOrganisationType.leadTrustee.heading",
            summaryList = getLeadTrusteeRows(state),
        )
    }

    private fun getLeadTrusteeRows(state: UpdateOrganisationTypeJourneyState): List<SummaryListRowViewModel> {
        val leadTrusteeTask = state.leadTrusteeTask
        return listOf(
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
        )
    }
}

@JourneyFrameworkComponent
final class OrgTypeCyaStep(
    stepConfig: OrgTypeCyaStepConfig,
) : AbstractCheckYourAnswersStep<UpdateOrganisationTypeJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-type-check-your-answers"
    }
}
