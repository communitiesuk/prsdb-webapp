package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.stepConfig.EmailStep

class EmailFormPageLocalCouncilUserRegistration(
    page: Page,
) : EmailFormPage(
        page,
        RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE +
            "/${EmailStep.ROUTE_SEGMENT}",
    )
