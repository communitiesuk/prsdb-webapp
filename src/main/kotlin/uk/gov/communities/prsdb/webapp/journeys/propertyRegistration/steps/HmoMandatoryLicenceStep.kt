package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.HMOAdditionalDetailModel

@Scope("prototype")
@PrsdbWebComponent
class HmoMandatoryLicenceStepConfig : AbstractGenericStepConfig<Complete, HmoMandatoryLicenceFormModel, JourneyState>() {
    override val formModelClass = HmoMandatoryLicenceFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "registerProperty.title",
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

@Scope("prototype")
@PrsdbWebComponent
final class HmoMandatoryLicenceStep(
    stepConfig: HmoMandatoryLicenceStepConfig,
) : RequestableStep<Complete, HmoMandatoryLicenceFormModel, JourneyState>(stepConfig)
