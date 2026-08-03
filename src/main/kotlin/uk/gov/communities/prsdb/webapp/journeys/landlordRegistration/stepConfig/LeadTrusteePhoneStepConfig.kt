package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel

@JourneyFrameworkComponent
class LeadTrusteePhoneStepConfig : AbstractRequestableStepConfig<Complete, LeadTrusteePhoneFormModel, JourneyState>() {
    override val formModelClass = LeadTrusteePhoneFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.leadTrusteePhone.fieldSetHeading",
            "submitButtonText" to "forms.buttons.continue",
            "hint" to "registerAsALandlord.phoneNumber.hint",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/phoneNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class LeadTrusteePhoneStep(
    stepConfig: LeadTrusteePhoneStepConfig,
) : RequestableStep<Complete, LeadTrusteePhoneFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "lead-trustee-phone"
    }
}
