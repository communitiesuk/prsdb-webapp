package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-1975: replace this placeholder with the real superior landlord email page
//  (content, input, validation and persistence). Rendered with the shared forms/todo placeholder page.
// TODO PDJB-1975: when changing a superior landlord, prefill this page with the email from
//  state.editingSuperiorLandlord by overriding resolvePageContent, as in OrgGovBodyMemberNameStepConfig.
//  Check when adding a second superior landlord that the page starts empty, as SaveSuperiorLandlordStepConfig clears the
//  form data after each save.
//  Also update SaveSuperiorLandlordStepConfig to save the email the user entered.
@JourneyFrameworkComponent
class SuperiorLandlordEmailStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf("todoComment" to "TODO (PDJB-1975): Superior landlord email address - content, input and validation to be added")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    override fun mode(state: JourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class SuperiorLandlordEmailStep(
    stepConfig: SuperiorLandlordEmailStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "superior-landlord-email"
    }
}
