package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FreeholderEmailFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderEmailStep

class FreeholderEmailFormPagePropertyRegistration(
    page: Page,
) : FreeholderEmailFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${FreeholderEmailStep.ROUTE_SEGMENT}",
    )
