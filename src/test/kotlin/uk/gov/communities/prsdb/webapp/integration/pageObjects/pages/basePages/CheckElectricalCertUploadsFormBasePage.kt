package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckElectricalCertUploadsFormPagePropertyRegistration.CheckUploadsForm

open class CheckElectricalCertUploadsFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    override val expectedTitleHeading: String
        get() = table.rows.count().let { "You’ve uploaded $it electrical safety certificate file${if (it == 1) "" else "s"}" }

    val backLink = BackLink.default(page)
    val table = Table(page)
    val form = CheckUploadsForm(page)
}
