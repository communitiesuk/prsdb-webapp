package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.example.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@PrsdbWebComponent
@Scope("prototype")
class NotionalStepConfig : AbstractGenericStepConfig<Complete, NoInputFormModel, OccupiedJourneyState>() {
    override fun chooseTemplate(state: OccupiedJourneyState): String = ""

    override val formModelClass = NoInputFormModel::class

    override fun mode(state: OccupiedJourneyState): Complete = Complete.COMPLETE

    override fun getStepSpecificContent(state: OccupiedJourneyState) = mapOf<String, String>()
}

@PrsdbWebComponent
@Scope("prototype")
class NotionalStep(
    stepConfig: NotionalStepConfig,
) : JourneyStep<Complete, NoInputFormModel, OccupiedJourneyState>(stepConfig)
