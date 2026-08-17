package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberDobStep

class OrgGovBodyMemberDobFormPageLandlordRegistration(
    page: Page,
) : DateFormPage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgGovBodyMemberDobStep.ROUTE_SEGMENT}")
