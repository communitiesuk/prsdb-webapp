package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertContentEquals

class ParentageTests {
    class AllowingParent(
        route: String = "yes-parent-step",
    ) : Parentage {
        override fun allowsChild(): Boolean = true

        override val ancestry: List<JourneyStep<*, *, *>> = listOf(mockStepWithRoute(route))
        override val allowingParentSteps: List<JourneyStep<*, *, *>> = listOf(mockStepWithRoute(route))
        override val potentialParents: List<JourneyStep<*, *, *>> = listOf(mockStepWithRoute(route))
    }

    class DisallowingParent(
        route: String = "no-parent-step",
    ) : Parentage {
        override fun allowsChild(): Boolean = false

        override val ancestry: List<JourneyStep<*, *, *>> = listOf(mockStepWithRoute(route))
        override val allowingParentSteps: List<JourneyStep<*, *, *>> = listOf(mockStepWithRoute(route))
        override val potentialParents: List<JourneyStep<*, *, *>> = listOf(mockStepWithRoute(route))
    }

    @Test
    fun `AndParents allows child only when all parents allow child`() {
        val allowingParent = AllowingParent()
        val disallowingParent = DisallowingParent()

        val andAllYes = AndParents(allowingParent, allowingParent, allowingParent)
        assertTrue(andAllYes.allowsChild())

        val andOneNo = AndParents(allowingParent, disallowingParent, allowingParent)
        assertFalse(andOneNo.allowsChild())

        val andAllNo = AndParents(disallowingParent, disallowingParent, disallowingParent)
        assertFalse(andAllNo.allowsChild())
    }

    @Test
    fun `AndParents aggregates ancestry and allowingParentSteps from allowing parents`() {
        // Arrange
        val allowingParentRoutes = listOf("yes-parent-1", "yes-parent-2")
        val allowingParents = allowingParentRoutes.map { AllowingParent(it) }.toTypedArray()
        val andParents = AndParents(DisallowingParent(), *allowingParents)

        // Act
        val actualAncestryRoutes = andParents.ancestry.map { it.getRouteSegmentOrNull() }
        val actualAllowingParentRoutes = andParents.allowingParentSteps.map { it.getRouteSegmentOrNull() }

        // Assert
        assertContentEquals(allowingParentRoutes, actualAncestryRoutes)
        assertContentEquals(allowingParentRoutes, actualAllowingParentRoutes)
    }

    @Test
    fun `AndParents aggregates potentialParents from all parents`() {
        // Arrange
        val allowingParentRoutes = listOf("yes-parent-1", "yes-parent-2")
        val disallowingParentRoutes = listOf("no-parent-1", "no-parent-2")
        val allowingParents = allowingParentRoutes.map { AllowingParent(it) }
        val disallowingParents = disallowingParentRoutes.map { DisallowingParent(it) }
        val andParents = AndParents(*(allowingParents + disallowingParents).toTypedArray())

        // Act
        val actualPotentialParentRoutes = andParents.potentialParents.map { it.getRouteSegmentOrNull() }

        // Assert
        val expectedRoutes = allowingParentRoutes + disallowingParentRoutes
        assertContentEquals(expectedRoutes, actualPotentialParentRoutes)
    }

    @Test
    fun `OrParents allows child when any parent allows child`() {
        val allowingParent = AllowingParent()
        val disallowingParent = DisallowingParent()

        val orAllYes = OrParents(allowingParent, allowingParent, allowingParent)
        assertTrue(orAllYes.allowsChild())

        val orOneYes = OrParents(allowingParent, disallowingParent, disallowingParent)
        assertTrue(orOneYes.allowsChild())

        val orAllNo = OrParents(disallowingParent, disallowingParent, disallowingParent)
        assertFalse(orAllNo.allowsChild())
    }

    @Test
    fun `OrParents aggregates ancestry and allowingParentSteps from allowing parents`() {
        // Arrange
        val allowingParentRoutes = listOf("yes-parent-1", "yes-parent-2")
        val allowingParents = allowingParentRoutes.map { AllowingParent(it) }.toTypedArray()
        val orParents = OrParents(DisallowingParent(), *allowingParents)

        // Act
        val actualAncestryRoutes = orParents.ancestry.map { it.getRouteSegmentOrNull() }
        val actualAllowingParentRoutes = orParents.allowingParentSteps.map { it.getRouteSegmentOrNull() }

        // Assert
        assertContentEquals(allowingParentRoutes, actualAncestryRoutes)
        assertContentEquals(allowingParentRoutes, actualAllowingParentRoutes)
    }

    @Test
    fun `OrParents aggregates potentialParents from all parents`() {
        // Arrange
        val allowingParentRoutes = listOf("yes-parent-1", "yes-parent-2")
        val disallowingParentRoutes = listOf("no-parent-1", "no-parent-2")
        val allowingParents = allowingParentRoutes.map { AllowingParent(it) }
        val disallowingParents = disallowingParentRoutes.map { DisallowingParent(it) }
        val orParents = OrParents(*(allowingParents + disallowingParents).toTypedArray())

        // Act
        val actualPotentialParentRoutes = orParents.potentialParents.map { it.getRouteSegmentOrNull() }

        // Assert
        val expectedRoutes = allowingParentRoutes + disallowingParentRoutes
        assertContentEquals(expectedRoutes, actualPotentialParentRoutes)
    }

    @Test
    fun `NoParents always allows child`() {
        val disallowingParents = NoParents()
        assertTrue(disallowingParents.allowsChild())
    }

    @Test
    fun `NoParents has empty ancestry, allowingParentSteps, and potentialParents`() {
        val disallowingParents = NoParents()
        assertTrue(disallowingParents.ancestry.isEmpty())
        assertTrue(disallowingParents.allowingParentSteps.isEmpty())
        assertTrue(disallowingParents.potentialParents.isEmpty())
    }

    @Test
    fun `SingleParent allows child based on condition`() {
        val allowingParent = SingleParent(mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) { true }
        val disallowingParent = SingleParent(mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) { false }
        assertTrue(allowingParent.allowsChild())
        assertFalse(disallowingParent.allowsChild())
    }

    @Test
    fun `SingleParent aggregates allowingParentSteps and potentialParents as at most itself`() {
        val allowingParent = SingleParent(mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) { true }
        val disallowingParent = SingleParent(mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) { false }

        assertSame(allowingParent.potentialParents.single(), allowingParent.step)
        assertSame(disallowingParent.potentialParents.single(), disallowingParent.step)
        assertSame(allowingParent.allowingParentSteps.single(), allowingParent.step)
        assertTrue(disallowingParent.allowingParentSteps.isEmpty())
    }

    @Test
    fun `SingleParent aggregates ancestry as itself plus parent's ancestry`() {
        // Arrange
        val grandparentStep = mockStepWithRoute("parent-ancestry-step")
        whenever(grandparentStep.parentage).thenReturn(NoParents())
        val grandparentParentage = SingleParent(grandparentStep) { true }

        val parentStep = mockStepWithRoute("parent")
        whenever(parentStep.parentage).thenReturn(grandparentParentage)
        val singleParent = SingleParent(parentStep) { true }

        // Act
        val ancestryRoutes = singleParent.ancestry.map { it.getRouteSegmentOrNull() }

        // Assert
        assertContentEquals(listOf("parent", "parent-ancestry-step"), ancestryRoutes)
    }

    @Test
    fun `hasOutcome returns a single parent with the condition checking that steps outcome matches`() {
        // Arrange
        val step = mock<JourneyStep.VisitableStep<TestEnum, *, *>>()

        // Act
        val parent = step.hasOutcome(TestEnum.ENUM_VALUE)

        // Assert
        whenever(step.outcome()).thenReturn(TestEnum.ENUM_VALUE)
        assertTrue(parent.allowsChild())

        whenever(step.outcome()).thenReturn(TestEnum.ALTERNATIVE_VALUE)
        assertFalse(parent.allowsChild())
    }

    @Test
    fun `isComplete returns a single parent with the condition checking that tasks final step is complete`() {
        // Arrange
        val task = mock<Task<*>>()
        val step = mock<NavigationalStep>()
        whenever(task.notionalExitStep).thenReturn(step)

        // Act
        val parent = task.isComplete()

        // Assert
        whenever(step.outcome()).thenReturn(NavigationComplete.COMPLETE)
        assertTrue(parent.allowsChild())

        whenever(step.outcome()).thenReturn(null)
        assertFalse(parent.allowsChild())
    }

    companion object {
        fun mockStepWithRoute(route: String): JourneyStep<*, *, *> {
            val step: JourneyStep<*, *, *> = mock<JourneyStep.VisitableStep<*, *, *>>()
            whenever(step.getRouteSegmentOrNull()).thenReturn(route)
            return step
        }
    }
}
