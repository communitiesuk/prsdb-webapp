package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-2039: replace this placeholder with the real reactivation electrical safety task.
//  Rendered with the shared forms/todo placeholder page.
@JourneyFrameworkComponent
class ReactivationElectricalSafetyStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf("todoComment" to "TODO (PDJB-2039): Reactivation electrical safety task - pages to be added")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    override fun mode(state: JourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ReactivationElectricalSafetyStep(
    stepConfig: ReactivationElectricalSafetyStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "electrical-safety"
    }
}
