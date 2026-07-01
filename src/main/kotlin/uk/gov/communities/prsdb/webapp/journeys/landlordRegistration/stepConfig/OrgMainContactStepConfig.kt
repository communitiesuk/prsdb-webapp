package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgMainContactFormModel

@JourneyFrameworkComponent
class OrgMainContactStepConfig : AbstractRequestableStepConfig<Complete, OrgMainContactFormModel, JourneyState>() {
    override val formModelClass = OrgMainContactFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.mainContact.fieldSetHeading",
            "nameLabel" to "forms.mainContact.name.label",
            "emailLabel" to "forms.mainContact.email.label",
            "phoneNumberLabel" to "forms.mainContact.phoneNumber.label",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/mainContactForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgMainContactStep(
    stepConfig: OrgMainContactStepConfig,
) : RequestableStep<Complete, OrgMainContactFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-main-contact"
    }
}
