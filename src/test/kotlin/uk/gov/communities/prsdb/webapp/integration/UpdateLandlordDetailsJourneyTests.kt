package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordUpdateDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.EmailFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.NameFormPageUpdateLandlordDetails

@Sql("/data-local.sql")
class UpdateLandlordDetailsJourneyTests : IntegrationTest() {
    private fun updateLandlordNameAndReturn(
        detailsPage: LandlordUpdateDetailsPage,
        newName: String,
    ): LandlordUpdateDetailsPage {
        val page = detailsPage.page
        detailsPage.personalDetailsSummaryList.nameRow.actions.actionLink
            .clickAndWait()
        val updateNamePage = assertPageIs(page, NameFormPageUpdateLandlordDetails::class)

        updateNamePage.nameInput.fill(newName)
        updateNamePage.form.submit()
        return assertPageIs(page, LandlordUpdateDetailsPage::class)
    }

    private fun updateLandlordEmailAndReturn(
        detailsPage: LandlordUpdateDetailsPage,
        newEmail: String,
    ): LandlordUpdateDetailsPage {
        val page = detailsPage.page
        detailsPage.personalDetailsSummaryList.emailRow.actions.actionLink
            .clickAndWait()
        val updateEmailPage = assertPageIs(page, EmailFormPageUpdateLandlordDetails::class)

        updateEmailPage.emailInput.fill(newEmail)
        updateEmailPage.form.submit()
        return assertPageIs(page, LandlordUpdateDetailsPage::class)
    }

    @Test
    fun `A Landlord can update all of their details on the Update Details Journey`(page: Page) {
        // Update details page
        var landlordDetailsUpdatePage = navigator.goToUpdateLandlordDetailsPage()
        assertThat(landlordDetailsUpdatePage.heading).containsText("Alexander Smith")

        val landlordName = "landlord name"
        landlordDetailsUpdatePage = updateLandlordNameAndReturn(landlordDetailsUpdatePage, landlordName)

        val landlordEmail = "new@email.test"
        landlordDetailsUpdatePage = updateLandlordEmailAndReturn(landlordDetailsUpdatePage, landlordEmail)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        landlordDetailsUpdatePage.submitButton.clickAndWait()
        val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

        // Check changes have occurred
        assertThat(landlordDetailsPage.personalDetailsSummaryList.nameRow.value).containsText(landlordName)
        assertThat(landlordDetailsPage.personalDetailsSummaryList.emailRow.value).containsText(landlordEmail)
    }

    @Test
    fun `A Landlord can update just their name on the Update Details Journey`(page: Page) {
        // Update details page
        var landlordDetailsUpdatePage = navigator.goToUpdateLandlordDetailsPage()
        assertThat(landlordDetailsUpdatePage.heading).containsText("Alexander Smith")

        val landlordName = "landlord name"
        landlordDetailsUpdatePage = updateLandlordNameAndReturn(landlordDetailsUpdatePage, landlordName)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        landlordDetailsUpdatePage.submitButton.clickAndWait()
        val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

        // Check changes have occurred
        assertThat(landlordDetailsPage.personalDetailsSummaryList.nameRow.value).containsText(landlordName)
    }

    @Test
    fun `A Landlord can update just their email on the Update Details Journey`(page: Page) {
        // Update details page
        var landlordDetailsUpdatePage = navigator.goToUpdateLandlordDetailsPage()
        assertThat(landlordDetailsUpdatePage.heading).containsText("Alexander Smith")
        val landlordEmail = "new@email.test"
        landlordDetailsUpdatePage = updateLandlordEmailAndReturn(landlordDetailsUpdatePage, landlordEmail)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        landlordDetailsUpdatePage.submitButton.clickAndWait()
        val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

        // Check changes have occurred
        assertThat(landlordDetailsPage.personalDetailsSummaryList.emailRow.value).containsText(landlordEmail)
    }
}
