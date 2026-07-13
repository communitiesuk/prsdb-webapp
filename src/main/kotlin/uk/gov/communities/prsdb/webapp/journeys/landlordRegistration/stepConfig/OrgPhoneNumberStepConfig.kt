package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgPhoneNumberFormModel

@JourneyFrameworkComponent
class OrgPhoneNumberStepConfig : AbstractRequestableStepConfig<Complete, OrgPhoneNumberFormModel, JourneyState>() {
    override val formModelClass = OrgPhoneNumberFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.orgPhoneNumber.fieldSetHeading",
            "submitButtonText" to "forms.buttons.continue",
            "hint" to "forms.phoneNumber.hint",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/phoneNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgPhoneNumberStep(
    stepConfig: OrgPhoneNumberStepConfig,
) : RequestableStep<Complete, OrgPhoneNumberFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-phone-number"
    }
}
