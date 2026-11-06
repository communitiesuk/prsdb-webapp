package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RegistrationFixtureTests {
    private val first =
        """{"subject":"owner-a","address":"1 Fixture Road, FA1 1AA","postcode":"FA1 1AA","building":"1","localCouncilId":1,"uprn":901}"""
    private val second =
        first.replace(
            "owner-a",
            "owner-b",
        ).replace("1 Fixture", "2 Fixture").replace("\"1\"", "\"2\"").replace("901", "902")

    @Test
    fun `allocates actors and free addresses once without cycling`() {
        val fixtures = RegistrationFixture.parse("[$first,$second]", 2)
        assertEquals(listOf("owner-a", "owner-b"), fixtures.map { it.subject })
        assertEquals(listOf(901L, 902L), fixtures.map { it.uprn })
    }

    @Test
    fun `rejects exhausted shared malformed and unsafe allocations before mutation`() {
        for (json in listOf(
            "[$first]", "[$first,$first]", "[$first,${second.replace("owner-b", "owner-a")}]",
            "[$first,${second.replace("902", "901")}]", "[$first,${second.replace("2 Fixture", "1 Fixture")}]",
            "[$first,${second.replace(":1,", ":0,")}]", "[$first,${second.replace("902", "1.5")}]",
            "[$first,${second.replace("FA1 1AA", "")}]", "[$first,${second.replace("owner-b", "../escape")}]",
        )) {
            assertFailsWith<IllegalArgumentException>(json) { RegistrationFixture.parse(json, 2) }
        }
    }
}
