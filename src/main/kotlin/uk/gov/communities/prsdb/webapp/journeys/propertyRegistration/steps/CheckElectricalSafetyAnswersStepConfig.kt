package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.ElectricalSafetyRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.UploadService

@JourneyFrameworkComponent
class CheckElectricalSafetyAnswersStepConfig(
    private val uploadService: UploadService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, ElectricalSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyState): Map<String, Any?> {
        val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(state, uploadService)
        return mapOf(
            "rows" to factory.createRows(),
            "insetTextKey" to factory.getInsetTextKey(),
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )
    }

    override fun chooseTemplate(state: ElectricalSafetyState) = "forms/checkElectricalSafetyAnswersForm"

    override fun mode(state: ElectricalSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

enum class ElectricalSafetyScenario {
    CERT_UPLOADED,
    PROVIDE_LATER,
    NO_CERT,
    CERT_EXPIRED,
}

@JourneyFrameworkComponent
final class CheckElectricalSafetyAnswersStep(
    stepConfig: CheckElectricalSafetyAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, ElectricalSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-electrical-safety-answers"
    }
}
