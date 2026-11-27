package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel

@JourneyFrameworkComponent
class HmoAdditionalLicenceStepConfig : AbstractRequestableStepConfig<Complete, HmoAdditionalLicenceFormModel, JourneyState>() {
    override val formModelClass = HmoAdditionalLicenceFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "licensing.hmoAdditionalLicence.fieldSetHeading",
            "detailSummary" to "licensing.licenceNumber.detail.summary",
            "detailText" to "licensing.licenceNumber.detail.text",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/licenceNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class HmoAdditionalLicenceStep(
    stepConfig: HmoAdditionalLicenceStepConfig,
) : RequestableStep<Complete, HmoAdditionalLicenceFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "hmo-additional-licence"
    }
}
