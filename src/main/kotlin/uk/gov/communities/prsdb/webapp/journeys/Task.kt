package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.builders.StepCollectionBuilder
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder

abstract class Task<in TState : JourneyState> {
    lateinit var subJourneyBuilder: SubJourneyBuilder<*>
    lateinit var subJourneyParentage: Parentage
    private lateinit var exitInit: StepInitialiser<NavigationalStepConfig, *, NavigationComplete>.() -> Unit

    fun getTaskSubJourneyBuilder(
        state: TState,
        entryPoint: Parentage,
        exitInit: StepInitialiser<NavigationalStepConfig, *, NavigationComplete>.() -> Unit,
    ): StepCollectionBuilder {
        this.subJourneyParentage = entryPoint
        this.exitInit = exitInit
        return makeSubJourney(state)
    }

    protected fun <TDslState : TState> subJourney(
        state: TDslState,
        init: SubJourneyBuilder<TDslState>.() -> Unit,
    ): StepCollectionBuilder {
        if (::subJourneyBuilder.isInitialized) {
            throw JourneyInitialisationException("Task sub-journey has already been initialised")
        }
        val localSubJourneyBuilder = SubJourneyBuilder(state)
        subJourneyBuilder = localSubJourneyBuilder
        localSubJourneyBuilder.subJourneyParent(subJourneyParentage)
        localSubJourneyBuilder.init()
        localSubJourneyBuilder.exitInitialiser?.exitInit()
        return localSubJourneyBuilder
    }

    abstract fun makeSubJourney(state: TState): StepCollectionBuilder

    fun taskStatus(): TaskStatus =
        when {
            notionalExitStep.isStepReachable -> TaskStatus.COMPLETED
            firstStep.outcome != null -> TaskStatus.IN_PROGRESS
            firstStep.isStepReachable -> TaskStatus.NOT_STARTED
            else -> TaskStatus.CANNOT_START
        }

    val notionalExitStep: NavigationalStep get() = subJourneyBuilder.exitStep
    val firstStep: JourneyStep<*, *, *> get() = subJourneyBuilder.firstStep
}
