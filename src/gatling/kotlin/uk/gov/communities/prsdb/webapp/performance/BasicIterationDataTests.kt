package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BasicIterationDataTests {
    @Test
    fun `warm up and measured passes use distinct request labels`() {
        val warmUp = BasicIterationData.forPass("urn:example:run-1", null)
        val measured = BasicIterationData.forPass("urn:example:run-1", 1)

        assertEquals("Warm-up: ", warmUp.requestNamePrefix)
        assertEquals("", measured.requestNamePrefix)
    }

    @Test
    fun `profile values change on every pass and differ from seeded values`() {
        val passes = (0..10).map { BasicIterationData.forPass("urn:example:run-1", it.takeIf { index -> index > 0 }) }

        assertNotEquals("07111111111", passes.first().profilePhoneNumber)
        assertNotEquals("alex.surname@example.com", passes.first().profileEmailAddress)
        assertTrue(passes.zipWithNext().all { (first, second) -> first.profilePhoneNumber != second.profilePhoneNumber })
        assertTrue(passes.zipWithNext().all { (first, second) -> first.profileEmailAddress != second.profileEmailAddress })
    }

    @Test
    fun `registration pass identity and contact values are distinct`() {
        val passes = (0..10).map { BasicIterationData.forPass("urn:example:run-1", it.takeIf { index -> index > 0 }) }

        assertEquals(passes.size, passes.map { it.registrationSubject }.distinct().size)
        assertEquals(passes.size, passes.map { it.registrationEmailAddress }.distinct().size)
        assertEquals(passes.size, passes.map { it.registrationPhoneNumber }.distinct().size)
        assertTrue(passes.all { it.registrationEmailAddress.endsWith("@example.invalid") })
        assertTrue(passes.all { it.registrationPhoneNumber.matches(Regex("0207946\\d{4}")) })
    }

    @Test
    fun `run prefix changes registration identities and contact addresses`() {
        val firstRun = BasicIterationData.forPass("urn:example:run-1", 1)
        val nextRun = BasicIterationData.forPass("urn:example:run-2", 1)

        assertNotEquals(firstRun.registrationSubject, nextRun.registrationSubject)
        assertNotEquals(firstRun.registrationEmailAddress, nextRun.registrationEmailAddress)
    }
}
