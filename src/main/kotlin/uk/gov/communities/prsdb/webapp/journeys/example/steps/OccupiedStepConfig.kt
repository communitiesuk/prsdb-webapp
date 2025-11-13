package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@Scope("prototype")
@PrsdbWebComponent
class OccupiedStepConfig : AbstractGenericStepConfig<YesOrNo, OccupancyFormModel, OccupiedJourneyState>() {
    override val formModelClass = OccupancyFormModel::class

    override fun getStepSpecificContent(state: OccupiedJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
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

    override fun chooseTemplate(state: OccupiedJourneyState): String = "forms/propertyOccupancyForm"

    override fun mode(state: OccupiedJourneyState): YesOrNo? =
        getFormModelFromStateOrNull(state)?.occupied?.let {
            when (it) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
            }
        }
}

@Scope("prototype")
@PrsdbWebComponent
final class OccupiedStep(
    stepConfig: OccupiedStepConfig,
) : RequestableStep<YesOrNo, OccupancyFormModel, OccupiedJourneyState>(stepConfig)
