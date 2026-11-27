package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_NOTES_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageSystemOperatorView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SystemOperatorPropertyNotesFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class SystemOperatorPropertyNotesJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `a system operator can add, change and clear notes on a property record`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsAsASystemOperator(propertyOwnershipId)
        val notesRow = detailsPage.propertyDetailsSummaryList.systemOperatorNotesRow
        assertThat(notesRow.value).containsText("Not added")

        notesRow.clickNamedActionLinkAndWait("Change")
        val notesPage = assertPageIs(page, SystemOperatorPropertyNotesFormPage::class, urlArguments)
        notesPage.submitNotes("Changed bedrooms at the landlord's request\nTicket 123")

        val updatedDetailsPage = assertPageIs(page, PropertyDetailsPageSystemOperatorView::class, urlArguments)
        val updatedNotesRow = updatedDetailsPage.propertyDetailsSummaryList.systemOperatorNotesRow
        assertThat(updatedNotesRow.value).containsText("Changed bedrooms at the landlord's request")
        assertThat(updatedNotesRow.value).containsText("Ticket 123")

        updatedNotesRow.clickNamedActionLinkAndWait("Change")
        val prefilledNotesPage = assertPageIs(page, SystemOperatorPropertyNotesFormPage::class, urlArguments)
        assertThat(prefilledNotesPage.form.notesInput).hasValue("Changed bedrooms at the landlord's request\nTicket 123")
        prefilledNotesPage.submitNotes("")

        val clearedDetailsPage = assertPageIs(page, PropertyDetailsPageSystemOperatorView::class, urlArguments)
        assertThat(clearedDetailsPage.propertyDetailsSummaryList.systemOperatorNotesRow.value).containsText("Not added")
    }

    @Test
    fun `submitting notes over the maximum length shows an error`(page: Page) {
        val notesPage = navigator.goToSystemOperatorPropertyNotesForm(propertyOwnershipId)

        notesPage.submitNotes("a".repeat(SYSTEM_OPERATOR_NOTES_MAX_LENGTH + 1))

        val notesPageWithError = assertPageIs(page, SystemOperatorPropertyNotesFormPage::class, urlArguments)
        assertThat(notesPageWithError.form.getErrorMessage("notes")).containsText("Notes must be 5,000 characters or fewer")
    }

    @Test
    fun `local council users do not see system operator notes`() {
        val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId)

        assertThat(detailsPage.propertyDetailsSummaryList.systemOperatorNotesRow).hasCount(0)
    }
}
