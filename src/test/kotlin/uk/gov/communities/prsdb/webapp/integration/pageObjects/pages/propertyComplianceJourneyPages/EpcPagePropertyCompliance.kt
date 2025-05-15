package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class EpcPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.EPC.urlPathSegment}",
    ) {
    val form = EpcCertificateForm(page)

    fun submitHasCert() {
        form.hasCertRadios.selectValue(HasEpc.YES)
        form.submit()
    }

    fun submitHasNoCert() {
        form.hasCertRadios.selectValue(HasEpc.NO)
        form.submit()
    }

    fun submitCertNotRequired() {
        form.hasCertRadios.selectValue(HasEpc.NOT_REQUIRED)
        form.submit()
    }

    class EpcCertificateForm(
        page: Page,
    ) : Form(page) {
        val hasCertRadios = Radios(locator)
    }
}
