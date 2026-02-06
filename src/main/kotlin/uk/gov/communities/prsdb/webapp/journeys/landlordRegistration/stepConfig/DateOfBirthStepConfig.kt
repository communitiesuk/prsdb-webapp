package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.IdentityState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel

@JourneyFrameworkComponent
class DateOfBirthStepConfig : AbstractRequestableStepConfig<Complete, DateOfBirthFormModel, IdentityState>() {
    override val formModelClass = DateOfBirthFormModel::class

    override fun getStepSpecificContent(state: IdentityState) =
        mapOf(
            "fieldSetHeading" to "forms.dateOfBirth.fieldSetHeading",
            "fieldSetHint" to "forms.dateOfBirth.fieldSetHint",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: IdentityState) = "forms/dateForm"

    override fun mode(state: IdentityState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class DateOfBirthStep(
    stepConfig: DateOfBirthStepConfig,
) : RequestableStep<Complete, DateOfBirthFormModel, IdentityState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "date-of-birth"
    }
}
