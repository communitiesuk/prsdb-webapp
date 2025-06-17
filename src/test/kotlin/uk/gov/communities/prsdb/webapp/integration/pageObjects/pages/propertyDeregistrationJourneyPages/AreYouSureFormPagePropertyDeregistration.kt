package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage

class AreYouSureFormPagePropertyDeregistration(
    page: Page,
    urlArguments: Map<String, String>,
) : AreYouSureFormBasePage(
        page,
        "/$LANDLORD_PATH_SEGMENT/$DEREGISTER_PROPERTY_JOURNEY_URL/${urlArguments["propertyOwnershipId"]}" +
            "/${DeregisterPropertyStepId.AreYouSure.urlPathSegment}",
    )
