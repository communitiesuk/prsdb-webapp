package uk.gov.communities.prsdb.webapp.journeys.example.steps

import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.NotionalStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

class NavigationalStepConfig : AbstractGenericStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override fun chooseTemplate(state: JourneyState): String = ""

    override val formModelClass = NoInputFormModel::class

    override fun mode(state: JourneyState): Complete = Complete.COMPLETE

    override fun getStepSpecificContent(state: JourneyState) = mapOf<String, String>()
}

class NavigationalStep<in TState : JourneyState>(
    stepConfig: NavigationalStepConfig,
) : NotionalStep<Complete, NoInputFormModel, TState>(stepConfig)
