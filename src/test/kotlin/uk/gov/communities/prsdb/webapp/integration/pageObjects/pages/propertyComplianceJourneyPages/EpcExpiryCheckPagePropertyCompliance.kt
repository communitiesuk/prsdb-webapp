package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class EpcExpiryCheckPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment}",
    ) {
    val form = EpcExpiryCheckForm(page)

    fun submitTenancyStartedBeforeExpiry() {
        form.tenancyStartedBeforeExpiryRadios.selectValue("true")
        form.submit()
    }

    fun submitTenancyStartedAfterExpiry() {
        form.tenancyStartedBeforeExpiryRadios.selectValue("false")
        form.submit()
    }

    class EpcExpiryCheckForm(
        page: Page,
    ) : PostForm(page) {
        val tenancyStartedBeforeExpiryRadios = Radios(locator)
    }
}
