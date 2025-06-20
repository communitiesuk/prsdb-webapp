package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EmailBulletPointListTests {
    @Test
    fun `toString returns the bullet points as a template string`() {
        val expectedMarkdown =
            """
            * bullet one
            * bullet two
            * bullet three
            """.trimIndent()

        val returnedMarkdown = EmailBulletPointList(listOf("bullet one", "bullet two", "bullet three")).toString()

        assertEquals(expectedMarkdown, returnedMarkdown)
    }

    @Test
    fun `toString returns an empty string if there are no bullet points`() {
        val expectedMarkdown = ""

        val returnedMarkdown = EmailBulletPointList(emptyList()).toString()

        assertEquals(expectedMarkdown, returnedMarkdown)
    }
}
