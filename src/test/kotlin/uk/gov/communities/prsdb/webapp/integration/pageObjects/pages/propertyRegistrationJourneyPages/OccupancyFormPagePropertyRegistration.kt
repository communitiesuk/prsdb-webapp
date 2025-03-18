package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OccupancyFormPage

class OccupancyFormPagePropertyRegistration(
    page: Page,
) : OccupancyFormPage(page, "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.Occupancy.urlPathSegment}")
