package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class EicrUploadConfirmationStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, EicrState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EicrState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            // TODO PDJB-467 - implement these properly / remove if not needed
            "submitButtonText" to "forms.buttons.saveAndContinueToEPC",
            /*"submitButtonText" to
                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                    "forms.buttons.saveAndContinueToEPC",
                    isCheckingAnswers || isUpdateJourney,
                ),*/
        )

    override fun chooseTemplate(state: EicrState): String = "forms/uploadCertificateConfirmationForm"

    override fun mode(state: EicrState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EicrUploadConfirmationStep(
    stepConfig: EicrUploadConfirmationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EicrState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr-upload-confirmation"
    }
}
