package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class OrgTypeTrustInterruptionStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, OrgTypeUpdateState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: OrgTypeUpdateState) =
        mapOf("todoComment" to "TODO PDJB-1466: Organisation type trust interruption page")

    override fun chooseTemplate(state: OrgTypeUpdateState) = "forms/todo"

    override fun mode(state: OrgTypeUpdateState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgTypeTrustInterruptionStep(
    stepConfig: OrgTypeTrustInterruptionStepConfig,
) : RequestableStep<Complete, NoInputFormModel, OrgTypeUpdateState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-type-trust-interruption"
    }
}
