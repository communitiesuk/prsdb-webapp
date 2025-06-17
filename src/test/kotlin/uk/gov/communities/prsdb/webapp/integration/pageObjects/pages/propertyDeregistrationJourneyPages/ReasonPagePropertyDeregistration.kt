package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ReasonFormPage

class ReasonPagePropertyDeregistration(
    page: Page,
    urlArguments: Map<String, String>,
) : ReasonFormPage(
        page,
        "/$LANDLORD_PATH_SEGMENT/$DEREGISTER_PROPERTY_JOURNEY_URL/${urlArguments["propertyOwnershipId"]}" +
            "/${DeregisterPropertyStepId.Reason.urlPathSegment}",
    )
