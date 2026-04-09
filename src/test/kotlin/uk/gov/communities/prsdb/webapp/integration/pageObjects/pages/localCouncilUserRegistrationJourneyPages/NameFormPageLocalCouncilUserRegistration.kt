package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NameFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep

class NameFormPageLocalCouncilUserRegistration(
    page: Page,
) : NameFormPage(
        page,
        "${RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}/${NameStep.ROUTE_SEGMENT}",
    )
