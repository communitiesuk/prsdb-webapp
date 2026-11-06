package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-1973: replace this placeholder with the real superior landlord name page
//  (content, input, validation and persistence). Rendered with the shared forms/todo placeholder page.
// TODO PDJB-1973: when changing a superior landlord, prefill this page with the name from
//  state.editingSuperiorLandlord by overriding resolvePageContent, as in FreeholderNameStepConfig.
//  Check when adding a second superior landlord that the page starts empty, as SaveSuperiorLandlordStepConfig clears the
//  form data after each save.
//  Also update SaveSuperiorLandlordStepConfig to save the name the user entered.
@JourneyFrameworkComponent
class SuperiorLandlordNameStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf("todoComment" to "TODO (PDJB-1973): Superior landlord name - content, input and validation to be added")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    override fun mode(state: JourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class SuperiorLandlordNameStep(
    stepConfig: SuperiorLandlordNameStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "superior-landlord-name"
    }
}
