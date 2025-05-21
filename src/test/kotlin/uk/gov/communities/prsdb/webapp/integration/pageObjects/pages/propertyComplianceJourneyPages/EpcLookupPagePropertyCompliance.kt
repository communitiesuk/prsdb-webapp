package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class EpcLookupPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.EpcLookup.urlPathSegment}",
    ) {
    val form = EpcLookupForm(page)

    fun submitCurrentEpcNumber() {
        form.epcCertificateNumberInput.fill("0000-0000-0000-0554-8410")
        form.submit()
    }

    fun submitSupersededEpcNumber() {
        form.epcCertificateNumberInput.fill("0000-0000-0000-0000-8410")
        form.submit()
    }

    fun submitNonexistentEpcNumber() {
        form.epcCertificateNumberInput.fill("1234-0000-0000-0000-8410")
        form.submit()
    }

    fun submitInvalidEpcNumber() {
        form.epcCertificateNumberInput.fill("invalid-certificate-number")
        form.submit()
    }

    class EpcLookupForm(
        page: Page,
    ) : Form(page) {
        val epcCertificateNumberInput = TextInput.textByFieldName(locator, "certificateNumber")
    }
}
