package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateSystemOperatorLandlordNotesController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextArea
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class SystemOperatorLandlordNotesFormPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(page, UpdateSystemOperatorLandlordNotesController.getUpdateNotesStepPath(urlArguments["id"]!!.toLong())) {
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
