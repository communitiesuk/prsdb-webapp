package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgMainContactFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep

class OrgMainContactFormPageLandlordRegistration(
    page: Page,
) : OrgMainContactFormBasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgMainContactStep.ROUTE_SEGMENT}",
    )
