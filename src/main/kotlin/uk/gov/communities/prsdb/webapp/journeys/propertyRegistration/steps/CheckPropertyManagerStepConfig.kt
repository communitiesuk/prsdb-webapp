package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyManagerState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class CheckPropertyManagerStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyManagerState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyManagerState): Map<String, Any?> =
        mapOf(
            "addAnotherTitle" to "registerProperty.propertyManager.check.heading",
            "insetText" to "registerProperty.propertyManager.check.insetText",
            "summaryCards" to listOf(getPropertyManagerCard(state)),
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    private fun getPropertyManagerCard(state: PropertyManagerState): SummaryCardViewModel {
        val details = state.propertyManagerDetailsStep.formModel
        return SummaryCardViewModel(
            title = details.notNullValue(PropertyManagerDetailsFormModel::name),
            summaryList =
                listOf(
                    SummaryListRowViewModel(
                        fieldHeading = "registerProperty.propertyManager.check.emailAddress",
                        fieldValue = details.notNullValue(PropertyManagerDetailsFormModel::emailAddress),
                    ),
                ),
            actions =
                listOfNotNull(
                    Destination(state.propertyManagerDetailsStep).toUrlStringOrNull()?.let {
                        SummaryCardActionViewModel(text = "forms.links.change", url = it)
                    },
                    Destination(state.removePropertyManagerStep).toUrlStringOrNull()?.let {
                        SummaryCardActionViewModel(text = "forms.links.remove", url = it)
                    },
                ),
        )
    }

    override fun chooseTemplate(state: PropertyManagerState) = "forms/addAnotherCardsForm"

    override fun mode(state: PropertyManagerState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class CheckPropertyManagerStep(
    stepConfig: CheckPropertyManagerStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyManagerState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-property-manager"
    }
}
