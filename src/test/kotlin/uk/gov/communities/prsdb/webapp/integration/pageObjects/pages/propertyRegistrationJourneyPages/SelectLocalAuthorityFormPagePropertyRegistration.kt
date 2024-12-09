package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FormBasePage

class SelectLocalAuthorityFormPagePropertyRegistration(
    page: Page,
) : FormBasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.SelectLocalAuthority.urlPathSegment}",
    )
