package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class GasSafetyUploadConfirmationConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            // TODO PDJB-340 - implement these properly
            "submitButtonText" to "forms.buttons.saveAndContinueToEICR",
            /*"submitButtonText" to
                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                    "forms.buttons.saveAndContinueToEICR",
                    isCheckingAnswers || isUpdateJourney,
                ),*/
        )

    override fun chooseTemplate(state: GasSafetyState): String = "forms/uploadCertificateConfirmationForm"

    override fun mode(state: GasSafetyState) = Complete.COMPLETE
}

@JourneyFrameworkComponent
final class GasSafetyUploadConfirmationStep(
    stepConfig: GasSafetyUploadConfirmationConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyState>(stepConfig)
