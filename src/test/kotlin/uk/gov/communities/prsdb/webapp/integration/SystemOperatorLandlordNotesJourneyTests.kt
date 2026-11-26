package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_NOTES_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SystemOperatorLandlordNotesFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SystemOperatorViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class SystemOperatorLandlordNotesJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val landlordId = 1L
    private val urlArguments = mapOf("id" to landlordId.toString())

    @Test
    fun `a system operator can add, change and clear notes on a landlord record`(page: Page) {
        val detailsPage = navigator.goToLandlordDetailsAsASystemOperator(landlordId)
        val notesRow = detailsPage.personalDetailsSummaryList.systemOperatorNotesRow
        assertThat(notesRow.value).containsText("Not added")

        notesRow.clickNamedActionLinkAndWait("Add")
        val notesPage = assertPageIs(page, SystemOperatorLandlordNotesFormPage::class, urlArguments)
        notesPage.submitNotes("Changed email at the landlord's request\nTicket 123")

        val updatedDetailsPage = assertPageIs(page, SystemOperatorViewLandlordDetailsPage::class, urlArguments)
        val updatedNotesRow = updatedDetailsPage.personalDetailsSummaryList.systemOperatorNotesRow
        assertThat(updatedNotesRow.value).containsText("Changed email at the landlord's request")
        assertThat(updatedNotesRow.value).containsText("Ticket 123")

        updatedNotesRow.clickNamedActionLinkAndWait("Change")
        val prefilledNotesPage = assertPageIs(page, SystemOperatorLandlordNotesFormPage::class, urlArguments)
        assertThat(prefilledNotesPage.form.notesInput).hasValue("Changed email at the landlord's request\nTicket 123")
        prefilledNotesPage.submitNotes("")

        val clearedDetailsPage = assertPageIs(page, SystemOperatorViewLandlordDetailsPage::class, urlArguments)
        assertThat(clearedDetailsPage.personalDetailsSummaryList.systemOperatorNotesRow.value).containsText("Not added")
    }

    @Test
    fun `submitting notes over the maximum length shows an error`(page: Page) {
        val notesPage = navigator.goToSystemOperatorLandlordNotesForm(landlordId)

        notesPage.submitNotes("a".repeat(SYSTEM_OPERATOR_NOTES_MAX_LENGTH + 1))

        val notesPageWithError = assertPageIs(page, SystemOperatorLandlordNotesFormPage::class, urlArguments)
        assertThat(notesPageWithError.form.getErrorMessage("notes")).containsText("Notes must be 5,000 characters or fewer")
    }

    @Test
    fun `local council users do not see system operator notes`() {
        val detailsPage = navigator.goToLandlordDetailsAsALocalCouncilUser(landlordId)

        assertThat(detailsPage.personalDetailsSummaryList.systemOperatorNotesRow).hasCount(0)
    }
}
