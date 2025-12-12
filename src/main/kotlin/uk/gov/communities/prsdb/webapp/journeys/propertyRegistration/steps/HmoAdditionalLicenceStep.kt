package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.HMOAdditionalDetailModel

@JourneyFrameworkComponent
class HmoAdditionalLicenceStepConfig : AbstractGenericStepConfig<Complete, HmoAdditionalLicenceFormModel, JourneyState>() {
    override val formModelClass = HmoAdditionalLicenceFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.hmoAdditionalLicence.fieldSetHeading",
            "label" to "forms.hmoAdditionalLicence.label",
            "detailSummary" to "forms.hmoAdditionalLicence.detail.summary",
            "detailAdditionalContent" to
                HMOAdditionalDetailModel(
                    "forms.hmoAdditionalLicence.detail.paragraph.two",
                    "forms.hmoAdditionalLicence.detail.paragraph.three",
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/licenceNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class HmoAdditionalLicenceStep(
    stepConfig: HmoAdditionalLicenceStepConfig,
) : RequestableStep<Complete, HmoAdditionalLicenceFormModel, JourneyState>(stepConfig)
