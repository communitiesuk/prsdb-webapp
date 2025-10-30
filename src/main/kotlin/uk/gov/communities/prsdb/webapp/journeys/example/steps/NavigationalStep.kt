package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@PrsdbWebComponent
@Scope("prototype")
class NavigationalStepConfig : AbstractGenericStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override fun chooseTemplate(state: JourneyState): String = ""

    override val formModelClass = NoInputFormModel::class

    override fun mode(state: JourneyState): Complete = Complete.COMPLETE

    override fun getStepSpecificContent(state: JourneyState) = mapOf<String, String>()
}

@PrsdbWebComponent
@Scope("prototype")
class NavigationalStep<in TState : JourneyState>(
    stepConfig: NavigationalStepConfig,
) : JourneyStep<Complete, NoInputFormModel, TState>(stepConfig)
