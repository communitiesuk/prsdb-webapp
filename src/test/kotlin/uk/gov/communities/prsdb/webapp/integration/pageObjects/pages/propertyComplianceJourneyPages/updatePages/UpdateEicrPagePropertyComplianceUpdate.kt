package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UpdateCertificateFormBasePage

class UpdateEicrPagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : UpdateCertificateFormBasePage(
        page,
        PropertyComplianceController
            .getUpdatePropertyComplianceStepPath(urlArguments["propertyOwnershipId"]!!.toLong(), PropertyComplianceStepId.UpdateEICR),
    ) {
    // TODO PRSD-1246 - remove this
    val continueButton = Button.byText(page, "Continue")
}
