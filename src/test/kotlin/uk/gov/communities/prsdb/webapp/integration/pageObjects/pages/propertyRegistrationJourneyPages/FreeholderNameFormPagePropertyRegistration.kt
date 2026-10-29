package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.TextFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonNameStep

class FreeholderNameFormPagePropertyRegistration(
    page: Page,
) : TextFormPage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PersonNameStep.ROUTE_SEGMENT}")
