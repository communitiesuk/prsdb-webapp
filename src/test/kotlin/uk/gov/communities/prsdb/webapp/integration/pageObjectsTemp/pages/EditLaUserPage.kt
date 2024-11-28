package uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.BaseComponent.Companion.getButton
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.BaseComponent.Companion.getHeading
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.BaseComponent.Companion.getSubHeading
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage

class EditLaUserPage(
    page: Page,
) : BasePage(page, "/edit-user/") {
    val name = getHeading(page)
    val email = getSubHeading(page)
    val form = Form(page)
    val isManagerRadios = form.getRadios()
    val removeAccountButton = getButton(page, "Remove this account")
}
