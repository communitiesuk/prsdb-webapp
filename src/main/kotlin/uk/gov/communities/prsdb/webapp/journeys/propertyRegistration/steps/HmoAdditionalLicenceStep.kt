package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.HMOAdditionalDetailModel

@Scope("prototype")
@PrsdbWebComponent
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

@Scope("prototype")
@PrsdbWebComponent
final class HmoAdditionalLicenceStep(
    stepConfig: HmoAdditionalLicenceStepConfig,
) : RequestableStep<Complete, HmoAdditionalLicenceFormModel, JourneyState>(stepConfig)
