package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.InterruptionPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmChangeToLettingAgentStep

class WhoProvidesChangeInterruptionPagePropertyRegistration(
    page: Page,
) : InterruptionPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${ConfirmChangeToLettingAgentStep.ROUTE_SEGMENT}",
    )
