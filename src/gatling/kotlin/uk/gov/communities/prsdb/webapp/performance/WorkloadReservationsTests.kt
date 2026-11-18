package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkloadReservationsTests {
    @Test
    fun `different families cannot share a subject or property`() {
        val reservations = WorkloadReservations()
        reservations.reserve(subjects = listOf("owner", "invitee"), properties = listOf(42))

        assertFailsWith<IllegalArgumentException> { reservations.reserve(subjects = listOf("invitee")) }
        assertFailsWith<IllegalArgumentException> { reservations.reserve(properties = listOf(42)) }
    }

    @Test
    fun `a family cannot reuse an actor or a property within its own allocation`() {
        assertFailsWith<IllegalArgumentException> {
            WorkloadReservations().reserve(subjects = listOf("same", "same"))
        }
        assertFailsWith<IllegalArgumentException> {
            WorkloadReservations().reserve(properties = listOf(42, 42))
        }
    }

    @Test
    fun `different families cannot mutate the same organisation`() {
        val reservations = WorkloadReservations()
        reservations.reserve(organisations = listOf(123))

        assertFailsWith<IllegalArgumentException> { reservations.reserve(organisations = listOf(123)) }
    }

    @Test
    fun `paid allocations reject normalized addresses and UPRNs shared across families`() {
        val reservations = WorkloadReservations()
        reservations.reserve(addresses = listOf(" 1  STREET, AB1 2CD "), uprns = listOf(100))

        assertFailsWith<IllegalArgumentException> { reservations.reserve(addresses = listOf("1 street, ab1 2cd")) }
        assertFailsWith<IllegalArgumentException> { reservations.reserve(uprns = listOf(100)) }
    }

    @Test
    fun `council observed owners and properties cannot be mutated in either reservation order`() {
        val readsFirst = WorkloadReservations()
        readsFirst.observe(subjects = listOf("owner"), properties = listOf(42))
        assertFailsWith<IllegalArgumentException> { readsFirst.reserve(subjects = listOf("owner")) }
        assertFailsWith<IllegalArgumentException> { readsFirst.reserve(properties = listOf(42)) }

        val mutationsFirst = WorkloadReservations()
        mutationsFirst.reserve(subjects = listOf("owner"), properties = listOf(42))
        assertFailsWith<IllegalArgumentException> { mutationsFirst.observe(subjects = listOf("owner")) }
        assertFailsWith<IllegalArgumentException> { mutationsFirst.observe(properties = listOf(42)) }
    }

    @Test
    fun `paid allocations cannot reuse council observed addresses or UPRNs`() {
        val reservations = WorkloadReservations()
        reservations.observe(addresses = listOf("1 STREET, AB1 2CD"), uprns = listOf(100))
        assertFailsWith<IllegalArgumentException> { reservations.reserve(addresses = listOf(" 1  street, ab1 2cd ")) }
        assertFailsWith<IllegalArgumentException> { reservations.reserve(uprns = listOf(100)) }

        val mutationsFirst = WorkloadReservations()
        mutationsFirst.reserve(addresses = listOf("1 STREET, AB1 2CD"), uprns = listOf(100))
        assertFailsWith<IllegalArgumentException> { mutationsFirst.observe(addresses = listOf("1 street, ab1 2cd")) }
        assertFailsWith<IllegalArgumentException> { mutationsFirst.observe(uprns = listOf(100)) }
    }

    @Test
    fun `failed allocations do not consume otherwise exclusive actors`() {
        val reservations = WorkloadReservations()
        reservations.reserve(properties = listOf(42))
        assertFailsWith<IllegalArgumentException> {
            reservations.reserve(subjects = listOf("fresh"), properties = listOf(42))
        }
        reservations.reserve(subjects = listOf("fresh"))
    }

    @Test
    fun `invitation allocations reject missing consumed and reused tokens`() {
        val reservations = WorkloadReservations()
        reservations.reserve(invitations = listOf("pending-token"))
        for (tokens in listOf(listOf(null), listOf(""), listOf("pending-token"), listOf("new", "new"))) {
            assertFailsWith<IllegalArgumentException> { reservations.reserve(invitations = tokens) }
        }
    }

    @Test
    fun `fixture windows include warmup and preparation offsets and reject exhausted pools`() {
        val pool = (1..456).toList()
        assertEquals((2..456).toList(), WorkloadReservations.window(pool, 1, 455))
        assertEquals((23..92).toList(), WorkloadReservations.window(pool, 22, 70))
        assertFailsWith<IllegalArgumentException> { WorkloadReservations.window(pool.dropLast(1), 1, 455) }
        assertFailsWith<IllegalArgumentException> { WorkloadReservations.window(pool, -1, 1) }
        assertFailsWith<IllegalArgumentException> { WorkloadReservations.window(pool, 0, 0) }
        assertFailsWith<IllegalArgumentException> { WorkloadReservations.window(pool, Int.MAX_VALUE, 1) }
    }
}
