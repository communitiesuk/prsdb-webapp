package uk.gov.communities.prsdb.webapp.journeyMap.introspection

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IntrospectedParentageTests {
    private val providedLater = { stepId: String, outcome: String -> stepId == "has-epc" && outcome == "PROVIDE_LATER" }

    @Test
    fun `a step whose parent must have an outcome always needs that outcome`() {
        assertTrue(single("has-epc", "PROVIDE_LATER").alwaysNeedsOutcome(providedLater))
        assertFalse(single("has-epc", "HAS_EPC").alwaysNeedsOutcome(providedLater))
        assertFalse(single("has-epc", null).alwaysNeedsOutcome(providedLater))
    }

    @Test
    fun `all parents needing an outcome means it is always needed, but one alternative without it is enough to avoid it`() {
        val needed = single("has-epc", "PROVIDE_LATER")
        val other = single("occupied", "YES")

        assertTrue(IntrospectedParentage(IntrospectedParentage.AND, children = listOf(other, needed)).alwaysNeedsOutcome(providedLater))
        assertTrue(IntrospectedParentage(IntrospectedParentage.OR, children = listOf(needed, needed)).alwaysNeedsOutcome(providedLater))
        assertFalse(IntrospectedParentage(IntrospectedParentage.OR, children = listOf(needed, other)).alwaysNeedsOutcome(providedLater))
        assertFalse(IntrospectedParentage(IntrospectedParentage.NONE).alwaysNeedsOutcome(providedLater))
    }

    private fun single(
        stepId: String,
        label: String?,
    ) = IntrospectedParentage(IntrospectedParentage.SINGLE, stepId, label)
}
