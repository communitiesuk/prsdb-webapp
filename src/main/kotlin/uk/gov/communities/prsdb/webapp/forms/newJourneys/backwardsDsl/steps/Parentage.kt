package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

interface Parentage {
    fun allowsChild(): Boolean

    val ancestry: List<StepInitialiser<*, *>>
    val parentSteps: List<StepInitialiser<*, *>>
}

class AndParents(
    vararg val parents: Parentage,
) : Parentage {
    override fun allowsChild(): Boolean = parents.all { it.allowsChild() }

    override val ancestry
        get() = parents.filter { it.allowsChild() }.flatMap { it.ancestry }

    override val parentSteps
        get() = parents.filter { it.allowsChild() }.flatMap { it.parentSteps }
}

class OrParents(
    vararg val parents: Parentage,
) : Parentage {
    override fun allowsChild(): Boolean = parents.any { it.allowsChild() }

    override val ancestry
        get() = parents.filter { it.allowsChild() }.flatMap { it.ancestry }

    override val parentSteps
        get() = parents.filter { it.allowsChild() }.flatMap { it.parentSteps }
}

class NoParents : Parentage {
    override fun allowsChild(): Boolean = true

    override val ancestry: List<StepInitialiser<*, *>>
        get() = listOf()

    override val parentSteps: List<StepInitialiser<*, *>>
        get() = listOf()
}

class SingleParent(
    val step: StepInitialiser<*, *>? = null,
    private val condition: () -> Boolean,
) : Parentage {
    override fun allowsChild(): Boolean = condition()

    override val ancestry: List<StepInitialiser<*, *>>
        get() = step?.let { listOf(it) + it.ancestry }.orEmpty()

    override val parentSteps: List<StepInitialiser<*, *>>
        get() = listOfNotNull(step)
}

fun StepInitialiser<*, *>.applyConditionToParent(condition: StepInitialiser<*, *>.() -> Boolean): Parentage = SingleParent { condition() }

fun <TEnum : Enum<TEnum>> StepInitialiser<TEnum, *>.hasOutcome(outcomeValue: TEnum): Parentage =
    SingleParent(this) { outcome() == outcomeValue }
