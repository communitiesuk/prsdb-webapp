package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.FooJourney
import uk.gov.communities.prsdb.webapp.journeys.example.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@Scope("prototype")
@PrsdbWebComponent
class FooCheckAnswersStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractGenericStepConfig<Complete, NoInputFormModel, FooJourney>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: FooJourney) =
        mapOf(
            "title" to "propertyDetails.update.title",
            "summaryName" to "forms.update.checkOccupancy.summaryName",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to "forms.update.checkOccupancy.insetText",
            "summaryListData" to occupationRows(state) + getEpcStatusRow(state),
        )

    private fun occupationRows(state: OccupiedJourneyState): List<SummaryListRowViewModel> {
        val occupiedStep = state.occupied
        return if (occupiedStep?.formModel?.occupied == true) {
            val householdsStep = state.households
            val tenantsStep = state.tenants
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.occupancy.fieldSetHeading",
                    true,
                    occupiedStep.routeSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.numberOfHouseholds.fieldSetHeading",
                    householdsStep?.formModel?.numberOfHouseholds,
                    householdsStep?.routeSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.numberOfPeople.fieldSetHeading",
                    tenantsStep?.formModel?.numberOfPeople,
                    tenantsStep?.routeSegment,
                ),
            )
        } else {
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.occupancy.fieldSetHeading",
                    false,
                    occupiedStep?.routeSegment,
                ),
            )
        }
    }

    private fun getEpcStatusRow(state: FooJourney): SummaryListRowViewModel {
        val fieldValue =
            when (state.epcQuestion.outcome()) {
                EpcStatus.AUTOMATCHED -> "forms.checkComplianceAnswers.epc.view"
                EpcStatus.NOT_AUTOMATCHED -> "forms.checkComplianceAnswers.epc.view"
                EpcStatus.NO_EPC -> "forms.checkComplianceAnswers.certificate.notAdded"
                null -> throw IllegalStateException("EPC status should be set if we are on the check your answers page")
            }

        val epc = state.searchedEpc ?: state.automatchedEpc
        val certificateNumber = epc?.certificateNumber
        val valueUrl =
            if (certificateNumber != null) {
                epcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)
            } else {
                null
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.epc.certificate",
            fieldValue,
            state.epcQuestion.routeSegment,
            valueUrl,
            valueUrlOpensNewTab = valueUrl != null,
        )
    }

    override fun chooseTemplate(state: FooJourney): String = "forms/checkAnswersForm"

    override fun mode(state: FooJourney): Complete? = getFormModelFromState(state)?.let { Complete.COMPLETE }
}
