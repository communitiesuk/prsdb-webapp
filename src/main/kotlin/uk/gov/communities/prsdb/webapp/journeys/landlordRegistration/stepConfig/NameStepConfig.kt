package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.IdentityState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel

@JourneyFrameworkComponent
class NameStepConfig : AbstractRequestableStepConfig<Complete, NameFormModel, IdentityState>() {
    override val formModelClass = NameFormModel::class

    override fun getStepSpecificContent(state: IdentityState) =
        mapOf(
            "fieldSetHeading" to "forms.name.fieldSetHeading",
            "fieldSetHint" to "forms.name.fieldSetHint",
            "label" to "forms.name.label",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: IdentityState) = "forms/nameForm"

    override fun mode(state: IdentityState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class NameStep(
    stepConfig: NameStepConfig,
) : RequestableStep<Complete, NameFormModel, IdentityState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "name"
    }
}
