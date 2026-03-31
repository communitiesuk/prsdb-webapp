package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel

@JourneyFrameworkComponent
class EmailStepConfig : AbstractRequestableStepConfig<Complete, EmailFormModel, JourneyState>() {
    override val formModelClass = EmailFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.email.fieldSetHeading",
            "fieldSetHint" to "forms.email.fieldSetHint",
            "label" to "forms.email.label",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/emailForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EmailStep(
    stepConfig: EmailStepConfig,
) : RequestableStep<Complete, EmailFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "email"
    }
}
