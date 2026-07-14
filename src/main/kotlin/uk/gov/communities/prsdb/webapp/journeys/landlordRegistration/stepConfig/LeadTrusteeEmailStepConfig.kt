package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel

@JourneyFrameworkComponent
class LeadTrusteeEmailStepConfig : AbstractRequestableStepConfig<Complete, LeadTrusteeEmailFormModel, JourneyState>() {
    override val formModelClass = LeadTrusteeEmailFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.leadTrusteeEmail.fieldSetHeading",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/emailForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class LeadTrusteeEmailStep(
    stepConfig: LeadTrusteeEmailStepConfig,
) : RequestableStep<Complete, LeadTrusteeEmailFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "lead-trustee-email"
    }
}
