package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.CorrespondenceEmailOption
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ErrorSummary
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class CorrespondenceEmailFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = CorrespondenceEmailForm(page)
    val errorSummary = ErrorSummary(page)
    val backLink = BackLink.default(page)

    fun submitAccountEmail() {
        form.whichEmailRadios.selectValue(CorrespondenceEmailOption.ACCOUNT_EMAIL)
        form.submit()
    }

    fun submitDifferentEmail(email: String) {
        form.whichEmailRadios.selectValue(CorrespondenceEmailOption.DIFFERENT_EMAIL)
        form.differentEmailInput.fill(email)
        form.submit()
    }

    class CorrespondenceEmailForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val whichEmailRadios = Radios(locator)
        val selectedEmailOptions = locator.locator("input[name='correspondenceEmailOption']:checked")
        val differentEmailInput = TextInput.emailByFieldName(locator, "differentEmailAddress")
    }
}
