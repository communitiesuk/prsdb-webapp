package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class FurnishedStepConfig : AbstractGenericStepConfig<Complete, FurnishedFormModel, OccupationState>() {
    override val formModelClass = FurnishedFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.isThePropertyFurnished.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = FurnishedStatus.FURNISHED,
                        labelMsgKey = "forms.isThePropertyFurnished.radios.options.furnished.label",
                        hintMsgKey = "forms.isThePropertyFurnished.radios.options.furnished.hint",
                    ),
                    RadiosButtonViewModel(
                        value = FurnishedStatus.PART_FURNISHED,
                        labelMsgKey = "forms.isThePropertyFurnished.radios.options.partFurnished.label",
                        hintMsgKey = "forms.isThePropertyFurnished.radios.options.partFurnished.hint",
                    ),
                    RadiosButtonViewModel(
                        value = FurnishedStatus.UNFURNISHED,
                        labelMsgKey = "forms.isThePropertyFurnished.radios.options.unfurnished.label",
                        hintMsgKey = "forms.isThePropertyFurnished.radios.options.unfurnished.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/propertyFurnishedForm"

    override fun mode(state: OccupationState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class FurnishedStep(
    stepConfig: FurnishedStepConfig,
) : RequestableStep<Complete, FurnishedFormModel, OccupationState>(stepConfig)
