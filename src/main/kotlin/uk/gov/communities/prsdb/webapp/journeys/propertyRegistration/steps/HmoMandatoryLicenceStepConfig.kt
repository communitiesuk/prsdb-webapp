package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.HMOAdditionalDetailModel

@JourneyFrameworkComponent
class HmoMandatoryLicenceStepConfig : AbstractRequestableStepConfig<Complete, HmoMandatoryLicenceFormModel, JourneyState>() {
    override val formModelClass = HmoMandatoryLicenceFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.hmoMandatoryLicence.fieldSetHeading",
            "label" to "forms.hmoMandatoryLicence.label",
            "detailSummary" to "forms.hmoMandatoryLicence.detail.summary",
            "detailAdditionalContent" to
                HMOAdditionalDetailModel(
                    "forms.hmoMandatoryLicence.detail.paragraph.two",
                    "forms.hmoMandatoryLicence.detail.paragraph.three",
                    listOf(
                        "forms.hmoMandatoryLicence.detail.bullet.one",
                        "forms.hmoMandatoryLicence.detail.bullet.two",
                        "forms.hmoMandatoryLicence.detail.bullet.three",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/licenceNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class HmoMandatoryLicenceStep(
    stepConfig: HmoMandatoryLicenceStepConfig,
) : RequestableStep<Complete, HmoMandatoryLicenceFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "hmo-mandatory-licence"
    }
}
