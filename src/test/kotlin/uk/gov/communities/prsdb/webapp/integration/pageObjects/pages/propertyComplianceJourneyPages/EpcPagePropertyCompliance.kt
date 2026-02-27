package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep

class EpcPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${EpcQuestionStep.ROUTE_SEGMENT}",
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
    ) : PostForm(page) {
        val hasCertRadios = Radios(locator)
    }
}
