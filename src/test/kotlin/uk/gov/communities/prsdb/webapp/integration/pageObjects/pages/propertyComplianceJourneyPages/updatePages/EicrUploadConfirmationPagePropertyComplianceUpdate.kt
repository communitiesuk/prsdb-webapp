package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadConfirmationFormPage

class EicrUploadConfirmationPagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : UploadConfirmationFormPage(
        page,
        PropertyComplianceController.getUpdatePropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.EicrUploadConfirmation.urlPathSegment}",
    )
