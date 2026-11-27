package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextArea

abstract class SystemOperatorNotesFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = NotesForm(page)

    fun submitNotes(notes: String) {
        form.notesInput.fill(notes)
        form.submit()
    }

    class NotesForm(
        page: Page,
    ) : PostForm(page) {
        val notesInput = TextArea.textByFieldName(locator, "notes")
    }
}
