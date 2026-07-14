package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel

@JourneyFrameworkComponent
class LeadTrusteeNameStepConfig : AbstractRequestableStepConfig<Complete, LeadTrusteeNameFormModel, JourneyState>() {
    override val formModelClass = LeadTrusteeNameFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.leadTrusteeName.fieldSetHeading",
            "fieldSetHint" to "registerAsALandlord.leadTrusteeName.fieldSetHint",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/nameForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class LeadTrusteeNameStep(
    stepConfig: LeadTrusteeNameStepConfig,
) : RequestableStep<Complete, LeadTrusteeNameFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "lead-trustee-name"
    }
}
