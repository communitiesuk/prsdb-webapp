package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage

class AreYouSureFormPageLandlordDeregistration(
    page: Page,
) : AreYouSureFormBasePage(
        page,
        "/$DEREGISTER_LANDLORD_JOURNEY_URL/${DeregisterLandlordStepId.AreYouSure.urlPathSegment}",
    )
