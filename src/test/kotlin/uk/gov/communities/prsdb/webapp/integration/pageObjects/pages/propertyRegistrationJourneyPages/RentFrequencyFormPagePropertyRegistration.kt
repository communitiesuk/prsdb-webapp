package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentFrequencyFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep

class RentFrequencyFormPagePropertyRegistration(
    page: Page,
) : RentFrequencyFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RentFrequencyStep.ROUTE_SEGMENT}",
    )
