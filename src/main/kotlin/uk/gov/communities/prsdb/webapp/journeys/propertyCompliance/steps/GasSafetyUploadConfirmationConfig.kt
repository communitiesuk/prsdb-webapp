package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class GasSafetyUploadConfirmationConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "submitButtonText" to "forms.buttons.saveAndContinueToEICR",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/uploadCertificateConfirmationForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class GasSafetyUploadConfirmationStep(
    stepConfig: GasSafetyUploadConfirmationConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-upload-confirmation"
    }
}
