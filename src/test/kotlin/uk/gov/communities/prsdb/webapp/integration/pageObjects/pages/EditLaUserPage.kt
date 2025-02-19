package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SubHeading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class EditLaUserPage(
    page: Page,
) : BasePage(page, "/edit-user/") {
    val name = Heading.default(page)
    val email = SubHeading(page)
    val form = Form(page)
    val isManagerRadios = form.getRadios()
    val removeAccountButton = Button.byText(page, "Remove this account")
}
