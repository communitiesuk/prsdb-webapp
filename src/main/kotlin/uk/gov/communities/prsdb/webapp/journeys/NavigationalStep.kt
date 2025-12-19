package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

open class NavigationalStepConfig : AbstractGenericStepConfig<NavigationComplete, NoInputFormModel, JourneyState>() {
    override fun chooseTemplate(state: JourneyState): String = ""

    override val formModelClass = NoInputFormModel::class

    override fun mode(state: JourneyState): NavigationComplete = NavigationComplete.COMPLETE

    override fun getStepSpecificContent(state: JourneyState) = mapOf<String, String>()
}

open class NavigationalStep(
    stepConfig: NavigationalStepConfig,
) : InternalStep<NavigationComplete, NoInputFormModel, JourneyState>(stepConfig)

enum class NavigationComplete {
    COMPLETE,
}
