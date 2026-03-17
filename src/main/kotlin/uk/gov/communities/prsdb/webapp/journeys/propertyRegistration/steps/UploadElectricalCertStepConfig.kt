package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-651: Implement Upload Electrical Cert page
@JourneyFrameworkComponent
class UploadElectricalCertStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "todoComment" to "TODO PDJB-651: Implement Upload Electrical Cert page",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UploadElectricalCertStep(
    stepConfig: UploadElectricalCertStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "electrical-safety-certificate-$FILE_UPLOAD_URL_SUBSTRING"
    }
}
