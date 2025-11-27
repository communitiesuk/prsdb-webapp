package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationComplete
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStepConfig
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

    fun exitStep(init: StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>.() -> Unit) {
        if (exitInitialiser != null) {
            throw JourneyInitialisationException("Sub-journey already has an exit step defined")
        }
        val stepInitialiser = StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>(null, exitStep, journey)
        stepInitialiser.init()
        exitInitialiser = stepInitialiser
    }

    private val stepCollectionsUnderConstruction: MutableList<StepCollectionBuilder> = mutableListOf()

    private var unreachableStepDestination: (() -> Destination)? = null

    private var additionalStepsConfiguration: MutableList<StepInitialiser<*, *, *>.() -> Unit> = mutableListOf()
    private var additionalElementsConfiguration: MutableList<StepLikeInitialiser<*>.() -> Unit> = mutableListOf()
    private var additionalFirstElementConfiguration: MutableList<StepLikeInitialiser<*>.() -> Unit> = mutableListOf()

    override fun buildSteps() =
        (stepCollectionsUnderConstruction + listOfNotNull(exitInitialiser)).flatMap { subJourney ->
            subJourney.configureSteps {
                unreachableStepDestination?.let { fallback -> unreachableStepDestinationIfNotSet(fallback) }
                additionalStepsConfiguration.forEach { it() }
            }
            subJourney.configureElements {
                additionalElementsConfiguration.forEach { it() }
            }

            subJourney.buildSteps()
        }

    fun configureTagged(
        tag: String,
        configuration: StepLikeInitialiser<*>.() -> Unit,
    ) {
        configureElements {
            if (tags.contains(tag)) {
                configuration()
            }
        }
    }

    override fun configureSteps(configuration: StepInitialiser<*, *, *>.() -> Unit) {
        additionalStepsConfiguration.add(configuration)
    }

    override fun configureElements(configuration: StepLikeInitialiser<*>.() -> Unit) {
        additionalElementsConfiguration.add(configuration)
    }

    override fun configureFirstStep(configuration: StepLikeInitialiser<*>.() -> Unit) {
        val firstStep = stepCollectionsUnderConstruction.firstOrNull()
        if (firstStep == null) {
            additionalFirstElementConfiguration.add(configuration)
        } else {
            firstStep.configureFirstStep(configuration)
        }
    }

    override fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> step(
        segment: String,
        uninitialisedStep: JourneyStep.RequestableStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(segment, uninitialisedStep, journey)
        stepInitialiser.init()
        if (!::firstStep.isInitialized) {
            firstStep = uninitialisedStep
            stepInitialiser.configureFirstStep {
                additionalFirstElementConfiguration.forEach { it() }
            }
        }
        stepCollectionsUnderConstruction.add(stepInitialiser)
    }

    override fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> notionalStep(
        uninitialisedStep: JourneyStep.InternalStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(null, uninitialisedStep, journey)
        stepInitialiser.init()
        if (!::firstStep.isInitialized) {
            firstStep = uninitialisedStep
            stepInitialiser.configureFirstStep {
                additionalFirstElementConfiguration.forEach { it() }
            }
        }
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
