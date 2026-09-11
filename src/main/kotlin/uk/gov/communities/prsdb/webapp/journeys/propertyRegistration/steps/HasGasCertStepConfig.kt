package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasGasCertFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class HasGasCertStepConfig : AbstractRequestableStepConfig<HasGasCertMode, HasGasCertFormModel, GasSafetyDetailState>() {
    override val formModelClass = HasGasCertFormModel::class

    override fun getStepSpecificContent(state: GasSafetyDetailState) =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.gasSafetyTask.gasCert.heading",
            "fieldSetHint" to "propertyCompliance.gasSafetyTask.gasCert.hint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(
                    yesHint = "propertyCompliance.gasSafetyTask.gasCert.radios.yesHint",
                ),
        )

    override fun chooseTemplate(state: GasSafetyDetailState) = "forms/hasGasCertForm"

    override fun mode(state: GasSafetyDetailState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.hasCert) {
                true -> HasGasCertMode.HAS_CERTIFICATE
                false -> HasGasCertMode.NO_CERTIFICATE
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class HasGasCertStep(
    stepConfig: HasGasCertStepConfig,
) : RequestableStep<HasGasCertMode, HasGasCertFormModel, GasSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-gas-safety"
    }
}

enum class HasGasCertMode {
    HAS_CERTIFICATE,
    NO_CERTIFICATE,
}
