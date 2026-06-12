package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EmailFormatUtilsTests {
    @Test
    fun `formatAsBulletList formats single item as bullet`() {
        assertEquals("* item1", formatAsBulletList(listOf("item1")))
    }

    @Test
    fun `formatAsBulletList formats multiple items as newline-separated bullets`() {
        assertEquals("* item1\n* item2\n* item3", formatAsBulletList(listOf("item1", "item2", "item3")))
    }

    @Test
    fun `formatEmailList returns plain email for single entry`() {
        assertEquals("test@example.com", formatEmailList(listOf("test@example.com")))
    }

    @Test
    fun `formatEmailList returns bullet list for multiple entries`() {
        assertEquals(
            "* a@example.com\n* b@example.com",
            formatEmailList(listOf("a@example.com", "b@example.com")),
        )
    }
}
