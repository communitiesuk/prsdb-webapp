package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel

@JourneyFrameworkComponent
class PhoneNumberStepConfig : AbstractRequestableStepConfig<Complete, PhoneNumberFormModel, JourneyState>() {
    override val formModelClass = PhoneNumberFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.phoneNumber.fieldSetHeading",
            "fieldSetHint" to "forms.phoneNumber.fieldSetHint",
            "label" to "forms.phoneNumber.label",
            "submitButtonText" to "forms.buttons.continue",
            "hint" to "forms.phoneNumber.hint",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/phoneNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class PhoneNumberStep(
    stepConfig: PhoneNumberStepConfig,
) : RequestableStep<Complete, PhoneNumberFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "phone-number"
    }
}
