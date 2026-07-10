package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeDobFormModel

@JourneyFrameworkComponent
class LeadTrusteeDobStepConfig : AbstractRequestableStepConfig<Complete, LeadTrusteeDobFormModel, JourneyState>() {
    override val formModelClass = LeadTrusteeDobFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.leadTrusteeDob.fieldSetHeading",
            "fieldSetHint" to "forms.leadTrusteeDob.fieldSetHint",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/dateForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class LeadTrusteeDobStep(
    stepConfig: LeadTrusteeDobStepConfig,
) : RequestableStep<Complete, LeadTrusteeDobFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "lead-trustee-dob"
    }
}
