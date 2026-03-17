package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FurnishedStatusState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class FurnishedStatusStepConfig : AbstractRequestableStepConfig<Complete, FurnishedStatusFormModel, FurnishedStatusState>() {
    override val formModelClass = FurnishedStatusFormModel::class

    override fun getStepSpecificContent(state: FurnishedStatusState) =
        mapOf(
            "fieldSetHeading" to "forms.furnishedStatus.fieldSetHeading",
            "fieldSetHint" to "forms.furnishedStatus.fieldSetHint",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = FurnishedStatus.FURNISHED,
                        labelMsgKey = "forms.furnishedStatus.radios.options.furnished.label",
                        hintMsgKey = "forms.furnishedStatus.radios.options.furnished.hint",
                    ),
                    RadiosButtonViewModel(
                        value = FurnishedStatus.PART_FURNISHED,
                        labelMsgKey = "forms.furnishedStatus.radios.options.partFurnished.label",
                        hintMsgKey = "forms.furnishedStatus.radios.options.partFurnished.hint",
                    ),
                    RadiosButtonViewModel(
                        value = FurnishedStatus.UNFURNISHED,
                        labelMsgKey = "forms.furnishedStatus.radios.options.unfurnished.label",
                        hintMsgKey = "forms.furnishedStatus.radios.options.unfurnished.hint",
                    ),
                ),
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: FurnishedStatusState): String = "forms/furnishedStatusForm"

    override fun mode(state: FurnishedStatusState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class FurnishedStatusStep(
    stepConfig: FurnishedStatusStepConfig,
) : RequestableStep<Complete, FurnishedStatusFormModel, FurnishedStatusState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "property-furnished"
    }
}
