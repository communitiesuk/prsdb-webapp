package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO: PDJB-1168 this shouldn't really be a separate task at all, this should extend the existing landlord registration CYA page.
// this was added as a separate page for easier scaffolding
@JourneyFrameworkComponent
class OrgLandlordCyaStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = mapOf("todoComment" to "TODO: PDJB-1168 - Check your answers")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgLandlordCyaStep(
    stepConfig: OrgLandlordCyaStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        // only needed so this can coexist on the same path as the single landlord CYA page
        const val ROUTE_SEGMENT = "org-check-answers"
    }
}
