package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationComplete
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.Task

open class SubJourneyBuilder<TState : JourneyState>(
    val journey: TState,
) : StepCollectionBuilder,
    JourneyBuilderDsl<TState> {
    var exitInitialiser: StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>? = null
        private set
    val exitStep = NavigationalStep(NavigationalStepConfig())
    lateinit var firstStep: JourneyStep<*, *, *>
        private set

    private lateinit var subJourneyParentage: Parentage

    fun exitStep(init: StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>.() -> Unit) {
        if (exitInitialiser != null) {
            throw JourneyInitialisationException("Sub-journey already has an exit step defined")
        }
        val stepInitialiser = StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>(null, exitStep, journey)
        stepInitialiser.init()
        exitInitialiser = stepInitialiser
    }

    fun subJourneyParent(parentage: Parentage) {
        if (::subJourneyParentage.isInitialized) {
            throw JourneyInitialisationException("Sub-journey exit step parentage has already been defined")
        }
        subJourneyParentage = parentage
    }

    fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> startingStep(
        segment: String,
        uninitialisedStep: JourneyStep.RequestableStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        firstStep = uninitialisedStep
        if (!::subJourneyParentage.isInitialized) {
            throw JourneyInitialisationException("Sub-journey parentage must be defined before the starting step")
        }
        return step<TMode, TStep>(segment, uninitialisedStep) {
            parents { subJourneyParentage }
            init()
        }
    }

    private val stepCollectionsUnderConstruction: MutableList<StepCollectionBuilder> = mutableListOf()

    private var unreachableStepDestination: (() -> Destination)? = null

    private var additionalConfiguration: StepInitialiser<*, *, *>.() -> Unit = {}

    override fun buildSteps() =
        (stepCollectionsUnderConstruction + listOfNotNull(exitInitialiser)).flatMap { subJourney ->
            subJourney.configureSteps {
                unreachableStepDestination?.let { fallback -> unreachableStepDestinationIfNotSet(fallback) }
                additionalConfiguration()
            }

            subJourney.buildSteps()
        }

    override fun configureSteps(configuration: StepInitialiser<*, *, *>.() -> Unit) {
        additionalConfiguration = configuration
    }

    override fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> step(
        segment: String,
        uninitialisedStep: JourneyStep.RequestableStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(segment, uninitialisedStep, journey)
        stepInitialiser.init()
        stepCollectionsUnderConstruction.add(stepInitialiser)
    }

    override fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> notionalStep(
        uninitialisedStep: JourneyStep.InternalStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(null, uninitialisedStep, journey)
        stepInitialiser.init()
        stepCollectionsUnderConstruction.add(stepInitialiser)
    }

    override fun task(
        uninitialisedTask: Task<TState>,
        init: TaskInitialiser<TState>.() -> Unit,
    ) {
        val taskInitialiser = TaskInitialiser(uninitialisedTask, journey)
        taskInitialiser.init()
        stepCollectionsUnderConstruction.add(taskInitialiser)
    }

    fun unreachableStepUrl(getUrl: () -> String) {
        if (unreachableStepDestination != null) {
            throw JourneyInitialisationException("unreachableStepDestination has already been set")
        }
        unreachableStepDestination = { Destination.ExternalUrl(getUrl()) }
    }

    fun unreachableStepStep(getStep: () -> JourneyStep<*, *, *>) {
        if (unreachableStepDestination != null) {
            throw JourneyInitialisationException("unreachableStepDestination has already been set")
        }
        unreachableStepDestination = { Destination(getStep()) }
    }
}
