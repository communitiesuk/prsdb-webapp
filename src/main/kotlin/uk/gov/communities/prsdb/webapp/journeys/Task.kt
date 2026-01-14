package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.constants.ReservedTagValues
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.builders.ConfigurableElement
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder

abstract class Task<in TState : JourneyState> {
    lateinit var subJourneyBuilder: SubJourneyBuilder<*>
        private set
    private lateinit var exitInit: StepInitialiser<TaskExitStepConfig, *, TaskComplete>.() -> Unit
    private var exitStepOverride: TaskExitStep? = null

    fun getTaskSubJourneyBuilder(
        state: TState,
        exitInit: StepInitialiser<TaskExitStepConfig, *, TaskComplete>.() -> Unit,
    ): SubJourneyBuilder<*> {
        this.exitInit = exitInit
        return makeSubJourney(state)
    }

    protected fun <TDslState : TState> subJourney(
        state: TDslState,
        init: SubJourneyBuilder<TDslState>.() -> Unit,
    ): SubJourneyBuilder<TDslState> {
        if (::subJourneyBuilder.isInitialized) {
            throw JourneyInitialisationException("Task sub-journey has already been initialised")
        }
        val localSubJourneyBuilder = SubJourneyBuilder(state, exitStepOverride)
        subJourneyBuilder = localSubJourneyBuilder
        localSubJourneyBuilder.exitStep {
            savable()
            exitInit()
        }
        localSubJourneyBuilder.init()
        return localSubJourneyBuilder
    }

    fun setCustomExitStep(step: TaskExitStep) {
        if (::subJourneyBuilder.isInitialized) {
            throw JourneyInitialisationException("Cannot set custom exit step after sub-journey has been initialised")
        }
        this.exitStepOverride = step
    }

    abstract fun makeSubJourney(state: TState): SubJourneyBuilder<*>

    fun taskStatus(): TaskStatus =
        when {
            notionalExitStep.isStepReachable -> TaskStatus.COMPLETED
            firstStep.outcome != null -> TaskStatus.IN_PROGRESS
            firstStep.isStepReachable -> TaskStatus.NOT_STARTED
            else -> TaskStatus.CANNOT_START
        }

    val notionalExitStep: TaskExitStep get() = subJourneyBuilder.exitStep
    val firstStep: JourneyStep<*, *, *> get() = subJourneyBuilder.firstStep

    protected fun ConfigurableElement<*>.savable() {
        taggedWith(SAVABLE)
    }

    companion object {
        fun SubJourneyBuilder<*>.configureSavable(init: ConfigurableElement<*>.() -> Unit) = configureTagged(SAVABLE, init)

        private const val SAVABLE = ReservedTagValues.SAVABLE
    }
}
