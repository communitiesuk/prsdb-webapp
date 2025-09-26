package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.forms.newJourneys.Complete
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.FooJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@Scope("prototype")
@PrsdbWebComponent
class FooCheckAnswersStep(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : BackwardsDslInitialisableStep<Complete, NoInputFormModel, FooJourneyState>() {
    override val formModelClazz = NoInputFormModel::class

    override fun getStepContent(state: FooJourneyState) =
        mapOf(
            "title" to "propertyDetails.update.title",
            "summaryName" to "forms.update.checkOccupancy.summaryName",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to "forms.update.checkOccupancy.insetText",
            "summaryListData" to occupationRows() + getEpcStatusRow(state),
        )

    private fun occupationRows(): List<SummaryListRowViewModel> {
        val lineage = ancestry
        val occupiedStep = lineage.find { it is OccupiedStep } as? OccupiedStep
        return if (occupiedStep?.formModel?.occupied == true) {
            val householdsStep = lineage.find { it is HouseholdStep } as? HouseholdStep
            val tenantsStep = lineage.find { it is TenantsStep } as? TenantsStep
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

    private fun getEpcStatusRow(state: FooJourneyState): SummaryListRowViewModel {
        val epc = state.searchedEpc ?: state.automatchedEpc

        val fieldValue =
            if (epc == null) {
                "forms.checkComplianceAnswers.certificate.notAdded"
            } else {
                "forms.checkComplianceAnswers.epc.view"
            }

        val certificateNumber = epc?.certificateNumber
        val valueUrl =
            if (certificateNumber != null) {
                epcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)
            } else {
                null
            }

        val epcQuestionStep = ancestry.find { it is EpcQuestionStep } as? EpcQuestionStep

        return SummaryListRowViewModel.Companion.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.epc.certificate",
            fieldValue,
            epcQuestionStep?.routeSegment,
            valueUrl,
            valueUrlOpensNewTab = valueUrl != null,
        )
    }

    override fun chooseTemplate(state: FooJourneyState): String = "forms/checkAnswersForm"

    override fun mode(state: FooJourneyState): Complete? = getFormModelFromState(state)?.let { Complete.COMPLETE }
}
