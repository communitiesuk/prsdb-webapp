package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.NotionalStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

class NavigationalStepConfig : AbstractGenericStepConfig<NavigationComplete, NoInputFormModel, JourneyState>() {
    override fun chooseTemplate(state: JourneyState): String = ""

    override val formModelClass = NoInputFormModel::class

    override fun mode(state: JourneyState): NavigationComplete = NavigationComplete.COMPLETE

    override fun getStepSpecificContent(state: JourneyState) = mapOf<String, String>()
}

class NavigationalStep(
    stepConfig: NavigationalStepConfig,
) : NotionalStep<NavigationComplete, NoInputFormModel, JourneyState>(stepConfig)

enum class NavigationComplete {
    COMPLETE,
}
