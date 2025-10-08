package uk.gov.communities.prsdb.webapp.theJourneyFramework

interface Parentage {
    fun allowsChild(): Boolean

    val ancestry: List<AbstractStep<*, *, *, *>>
    val parentSteps: List<AbstractStep<*, *, *, *>>
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

    override val ancestry: List<AbstractStep<*, *, *, *>>
        get() = listOf()

    override val parentSteps: List<AbstractStep<*, *, *, *>>
        get() = listOf()
}

class SingleParent(
    val step: AbstractStep<*, *, *, *>? = null,
    private val condition: () -> Boolean,
) : Parentage {
    override fun allowsChild(): Boolean = condition()

    override val ancestry
        get() = step?.let { listOf(it) + it.ancestry }.orEmpty()

    override val parentSteps
        get() = listOfNotNull(step)
}

fun <TEnum : Enum<TEnum>> AbstractStep<TEnum, *, *, *>.hasOutcome(outcomeValue: TEnum): Parentage =
    SingleParent(this) { outcome() == outcomeValue }
