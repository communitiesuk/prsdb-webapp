package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgGovBodyWhoToProvideFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep

class OrgGovBodyWhoToProvideFormPageLandlordRegistration(
    page: Page,
) : OrgGovBodyWhoToProvideFormPage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT}")
