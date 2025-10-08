package uk.gov.communities.prsdb.webapp.theJourneyFramework

class JourneyBuilder<TState : DynamicJourneyState>(
    private val state: TState,
) {
    val stepsUnderConstruction: MutableList<StepBuilder<*, TState, *>> = mutableListOf()

    fun build() = stepsUnderConstruction.associate { sb -> sb.build(state).let { it.routeSegment to StepConductor(it) } }

    fun <TMode : Enum<TMode>, TStep : AbstractStep<TMode, *, TState, TStep>> step(
        segment: String,
        uninitialisedStep: TStep,
        init: StepBuilder<TStep, TState, *>.() -> Unit,
    ) {
        val stepBuilder = StepBuilder(segment, uninitialisedStep)
        stepBuilder.init()
        stepsUnderConstruction.add(stepBuilder)
    }

    companion object {
        fun <TState : DynamicJourneyState> journey(
            state: TState,
            init: JourneyBuilder<TState>.() -> Unit,
        ): Map<String, StepConductor> {
            val builder = JourneyBuilder(state)
            builder.init()
            return builder.build()
        }
    }
}

class StepBuilder<TStep : AbstractStep<TMode, *, TState, TStep>, TState : DynamicJourneyState, TMode : Enum<TMode>>(
    private val segment: String,
    private val step: TStep,
) {
    private var backUrlOverride: (() -> String)? = null
    private var redirectTo: ((mode: TMode) -> UsableStep<*>?)? = null
    private var parentage: (() -> Parentage)? = null
    private var additionalConfig: (TStep.() -> Unit)? = null

    fun redirectTo(nextStepProvider: (mode: TMode) -> UsableStep<*>?): StepBuilder<TStep, TState, TMode> {
        redirectTo = nextStepProvider
        return this
    }

    fun parents(currentParentage: () -> Parentage): StepBuilder<TStep, TState, TMode> {
        parentage = currentParentage
        return this
    }

    fun stepSpecificInitialisation(configure: TStep.() -> Unit): StepBuilder<TStep, TState, TMode> {
        additionalConfig = configure
        return this
    }

    fun backUrl(backUrlProvider: () -> String): StepBuilder<TStep, TState, TMode> {
        backUrlOverride = backUrlProvider
        return this
    }

    fun build(state: TState): TStep {
        val castedRedirectTo = redirectTo ?: throw Exception("Step $segment has no redirectTo defined")
        val castedParentage = parentage ?: { NoParents() }
        step.initialize1(segment, state, backUrlOverride, castedRedirectTo, castedParentage)
        additionalConfig?.let { configure -> step.configure() }
        return step
    }
}
