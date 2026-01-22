package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GOV_LEGAL_ADVICE_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class HasJointLandlordsConfig : AbstractGenericStepConfig<YesOrNo, HasJointLandlordsFormModel, JointLandlordsState>() {
    override val formModelClass = HasJointLandlordsFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "jointLandlords.hasJointLandlords.heading",
            "label" to "jointLandlords.hasJointLandlords.subHeading.two",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        labelMsgKey = "forms.radios.option.yes.label",
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        labelMsgKey = "jointLandlords.hasJointLandlords.radios.no",
                    ),
                ),
            "findLegalAdviceUrl" to GOV_LEGAL_ADVICE_URL,
        )

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/hasJointLandlordsForm"

    override fun mode(state: JointLandlordsState): YesOrNo? =
        getFormModelFromStateOrNull(state)?.hasJointLandlords?.let {
            when (it) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
            }
        }
}

@JourneyFrameworkComponent
final class HasJointLandlordsStep(
    stepConfig: HasJointLandlordsConfig,
) : RequestableStep<YesOrNo, HasJointLandlordsFormModel, JointLandlordsState>(stepConfig)
