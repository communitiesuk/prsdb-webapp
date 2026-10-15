package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HttpDestinationTests {
    private val origin = "http://localhost:8080"

    @Test
    fun `relative paths resolve against the origin`() {
        assertEquals("http://localhost:8080/form?x=1", HttpDestination.resolve("/form?x=1", origin))
    }

    @Test
    fun `relative paths resolve against the current page`() {
        assertEquals(
            "http://localhost:8080/a/c",
            HttpDestination.resolve("c", origin, relativeTo = "http://localhost:8080/a/b"),
        )
    }

    @Test
    fun `empty value resolves to the current page`() {
        assertEquals(
            "http://localhost:8080/a/b",
            HttpDestination.resolve("", origin, relativeTo = "http://localhost:8080/a/b"),
        )
    }

    @Test
    fun `absolute same-origin URLs are allowed`() {
        assertEquals("http://localhost:8080/form", HttpDestination.resolve("http://localhost:8080/form", origin))
    }

    @Test
    fun `off-origin destinations are rejected`() {
        listOf(
            "http://evil.example/form",
            "http://localhost:9090/form",
            "https://localhost:8080/form",
            "//evil.example/form",
        ).forEach { value ->
            assertFailsWith<IllegalArgumentException>(value) { HttpDestination.resolve(value, origin) }
        }
    }

    @Test
    fun `destinations containing credentials are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            HttpDestination.resolve("http://user:pass@localhost:8080/form", origin)
        }
    }
}
