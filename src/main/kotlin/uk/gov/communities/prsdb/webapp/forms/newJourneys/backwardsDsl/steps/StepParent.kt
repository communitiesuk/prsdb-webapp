package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

interface StepParent {
    fun allowsChild(): Boolean

    val ancestry: List<StepInitialiser<*, *>>
    val parentSteps: List<StepInitialiser<*, *>>
}

class AndParents(
    vararg val parents: StepParent,
) : StepParent {
    override fun allowsChild(): Boolean = parents.all { it.allowsChild() }

    override val ancestry
        get() = parents.filter { it.allowsChild() }.flatMap { it.ancestry }

    override val parentSteps
        get() = parents.filter { it.allowsChild() }.flatMap { it.parentSteps }
}

class OrParents(
    vararg val parents: StepParent,
) : StepParent {
    override fun allowsChild(): Boolean = parents.any { it.allowsChild() }

    override val ancestry
        get() = parents.filter { it.allowsChild() }.flatMap { it.ancestry }

    override val parentSteps
        get() = parents.filter { it.allowsChild() }.flatMap { it.parentSteps }
}

class NoParents : StepParent {
    override fun allowsChild(): Boolean = true

    override val ancestry: List<StepInitialiser<*, *>>
        get() = listOf()

    override val parentSteps: List<StepInitialiser<*, *>>
        get() = listOf()
}

class ConditionalParent(
    val parent: StepInitialiser<*, *>? = null,
    private val condition: () -> Boolean,
) : StepParent {
    override fun allowsChild(): Boolean = condition()

    override val ancestry: List<StepInitialiser<*, *>>
        get() = parent?.let { listOf(it) + it.ancestry }.orEmpty()

    override val parentSteps: List<StepInitialiser<*, *>>
        get() = listOfNotNull(parent)
}

fun StepInitialiser<*, *>.applyConditionToParent(condition: StepInitialiser<*, *>.() -> Boolean): StepParent =
    ConditionalParent { condition() }

fun <TEnum : Enum<TEnum>> StepInitialiser<TEnum, *>.hasOutcome(outcomeValue: TEnum): StepParent =
    ConditionalParent(this) { outcome() == outcomeValue }
