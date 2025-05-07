package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import org.springframework.util.ResourceUtils
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FileUpload
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form

abstract class UploadCertificateFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = UploadCertificateForm(page)

    fun uploadCertificate(fileName: String) {
        val filePath = ResourceUtils.getFile("classpath:data/certificates/$fileName").path
        form.certificateFileUpload.stageFile(filePath)
        form.submit()
    }

    class UploadCertificateForm(
        page: Page,
    ) : Form(page) {
        val certificateFileUpload = FileUpload(page)
    }
}
