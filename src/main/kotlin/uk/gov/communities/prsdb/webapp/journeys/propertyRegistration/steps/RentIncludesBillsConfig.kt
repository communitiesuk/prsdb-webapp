package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentIncludesBillsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@Scope("prototype")
@PrsdbWebComponent
class RentIncludesBillsStepConfig : AbstractGenericStepConfig<YesOrNo, RentIncludesBillsFormModel, OccupationState>() {
    override val formModelClass = RentIncludesBillsFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.rentIncludesBills.fieldSetHeading",
            "fieldSetHint" to "forms.rentIncludesBills.fieldSetHint",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        labelMsgKey = "forms.radios.option.yes.label",
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        labelMsgKey = "forms.radios.option.no.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/rentIncludesBillsForm"

    override fun mode(state: OccupationState): YesOrNo? =
        getFormModelFromStateOrNull(state)?.rentIncludesBills?.let {
            when (it) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
            }
        }
}

@Scope("prototype")
@PrsdbWebComponent
final class RentIncludesBillsStep(
    stepConfig: RentIncludesBillsStepConfig,
) : RequestableStep<YesOrNo, RentIncludesBillsFormModel, OccupationState>(stepConfig)
