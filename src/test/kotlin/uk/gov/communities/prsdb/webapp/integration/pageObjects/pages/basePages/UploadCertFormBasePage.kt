package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import java.nio.file.Path

open class UploadCertFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val form = FormWithSectionHeader(page)

    fun uploadGasCertificate(filePath: Path) {
        page.setInputFiles("input[type=\"file\"]", filePath)
        form.submit()
    }
}
