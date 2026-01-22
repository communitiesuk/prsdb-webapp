package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class OccupiedStepConfig : AbstractRequestableStepConfig<YesOrNo, OccupancyFormModel, OccupationState>() {
    override val formModelClass = OccupancyFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.occupancy.fieldSetHeading",
            "fieldSetHint" to "forms.occupancy.fieldSetHint",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        labelMsgKey = "forms.radios.option.yes.label",
                        hintMsgKey = "forms.occupancy.radios.option.yes.hint",
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        labelMsgKey = "forms.radios.option.no.label",
                        hintMsgKey = "forms.occupancy.radios.option.no.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/propertyOccupancyForm"

    override fun mode(state: OccupationState): YesOrNo? =
        getFormModelFromStateOrNull(state)?.occupied?.let {
            when (it) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
            }
        }
}

@JourneyFrameworkComponent
final class OccupiedStep(
    stepConfig: OccupiedStepConfig,
) : RequestableStep<YesOrNo, OccupancyFormModel, OccupationState>(stepConfig)
