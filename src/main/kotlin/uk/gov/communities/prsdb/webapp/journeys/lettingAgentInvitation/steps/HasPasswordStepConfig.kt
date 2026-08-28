package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasPasswordFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

enum class PasswordStatus {
    HAS_PASSWORD,
    NO_PASSWORD,
}

@JourneyFrameworkComponent
class HasPasswordStepConfig : AbstractRequestableStepConfig<PasswordStatus, HasPasswordFormModel, JourneyState>() {
    override val formModelClass = HasPasswordFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to HasPasswordFormModel::hasPassword.name,
            "fieldSetHeading" to "lettingAgentInvitation.hasPassword.fieldSetHeading",
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(
                    yesLabel = "lettingAgentInvitation.hasPassword.radios.hasPassword",
                    noLabel = "lettingAgentInvitation.hasPassword.radios.noPassword",
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/todoWithRadios"

    override fun mode(state: JourneyState): PasswordStatus? =
        getFormModelFromStateOrNull(state)?.hasPassword?.let {
            if (it) PasswordStatus.HAS_PASSWORD else PasswordStatus.NO_PASSWORD
        }
}

@JourneyFrameworkComponent
final class HasPasswordStep(
    stepConfig: HasPasswordStepConfig,
) : RequestableStep<PasswordStatus, HasPasswordFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-password"
    }
}
