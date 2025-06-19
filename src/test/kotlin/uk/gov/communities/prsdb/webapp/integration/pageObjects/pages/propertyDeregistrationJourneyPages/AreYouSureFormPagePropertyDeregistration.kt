package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage

class AreYouSureFormPagePropertyDeregistration(
    page: Page,
    urlArguments: Map<String, String>,
) : AreYouSureFormBasePage(
        page,
        DeregisterPropertyController.getPropertyDeregistrationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${DeregisterPropertyStepId.AreYouSure.urlPathSegment}",
    )
