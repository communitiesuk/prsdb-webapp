package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.APIResponse
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.FormData
import com.microsoft.playwright.options.RequestOptions
import org.springframework.util.ResourceUtils
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FileUpload
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm

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

    fun metadataOnlySubmission(
        name: String,
        size: Long,
        type: String,
    ): APIResponse {
        val csrfToken = form.csrfToken
        val formData =
            FormData.create().apply {
                append("_csrf", csrfToken)
                append("name", name)
                append("contentLength", size.toString())
                append("contentType", type)
            }

        return page
            .request()
            .post(
                page.url(),
                RequestOptions
                    .create()
                    .setForm(formData),
            )
    }

    class UploadCertificateForm(
        page: Page,
    ) : PostForm(page) {
        val certificateFileUpload = FileUpload(page)
    }
}
