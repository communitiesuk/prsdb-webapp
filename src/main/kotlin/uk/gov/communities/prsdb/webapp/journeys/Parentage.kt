package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

interface Parentage {
    fun allowsChild(): Boolean

    val ancestry: List<JourneyStep<*, *, *>>
    val allowingParentSteps: List<JourneyStep<*, *, *>>

    val potentialParents: List<JourneyStep<*, *, *>>
}

class AndParents(
    vararg val parents: Parentage,
) : Parentage {
    override fun allowsChild(): Boolean = parents.all { it.allowsChild() }

    override val ancestry
        get() = parents.filter { it.allowsChild() }.flatMap { it.ancestry }

    override val allowingParentSteps
        get() = parents.filter { it.allowsChild() }.flatMap { it.allowingParentSteps }

    override val potentialParents
        get() = parents.flatMap { it.potentialParents }
}

class OrParents(
    vararg val parents: Parentage,
) : Parentage {
    override fun allowsChild(): Boolean = parents.any { it.allowsChild() }

    override val ancestry
        get() = parents.filter { it.allowsChild() }.flatMap { it.ancestry }

    override val allowingParentSteps
        get() = parents.filter { it.allowsChild() }.flatMap { it.allowingParentSteps }

    override val potentialParents
        get() = parents.flatMap { it.potentialParents }
}

class NoParents : Parentage {
    override fun allowsChild(): Boolean = true

    override val ancestry: List<JourneyStep<*, *, *>>
        get() = listOf()

    override val allowingParentSteps: List<JourneyStep<*, *, *>>
        get() = listOf()

    override val potentialParents: List<JourneyStep<*, *, *>>
        get() = listOf()
}

class SingleParent(
    val step: JourneyStep<*, *, *>,
    private val condition: () -> Boolean,
) : Parentage {
    override fun allowsChild(): Boolean = condition()

    override val ancestry
        get() = listOf(step) + step.parentage.ancestry

    override val allowingParentSteps
        get() = if (condition()) listOf(step) else listOf()

    override val potentialParents
        get() = listOf(step)
}

fun <TEnum : Enum<TEnum>> JourneyStep<TEnum, *, *>.hasOutcome(outcomeValue: TEnum): Parentage =
    SingleParent(this) { outcome == outcomeValue }

fun <TEnum : Enum<TEnum>> JourneyStep<TEnum, *, *>.notHasOutcome(outcomeValue: TEnum): Parentage =
    SingleParent(this) { outcome != outcomeValue }

fun Task<*>.isComplete() =
    SingleParent(exitStep) {
        exitStep.outcome == SubjourneyComplete.COMPLETE
    }

fun JourneyStep<Complete, *, *>.isComplete() = this.hasOutcome(Complete.COMPLETE)

fun JourneyStep<*, *, *>.always(): Parentage = SingleParent(this) { true }
