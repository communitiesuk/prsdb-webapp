package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-1978: replace this placeholder with the real internal step routing back to the has superior landlords question when the last superior landlord is removed
@JourneyFrameworkComponent
class SuperiorLandlordsBackRoutingStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "todoComment" to
                "TODO (PDJB-1978): internal step routing back to the has superior landlords page when the last one is removed to be added",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    override fun mode(state: JourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class SuperiorLandlordsBackRoutingStep(
    stepConfig: SuperiorLandlordsBackRoutingStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "superior-landlords-back-routing"
    }
}
