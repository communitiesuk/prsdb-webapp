package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgGovBodyMemberListFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep

class OrgGovBodyMemberListFormPageLandlordRegistration(
    page: Page,
) : OrgGovBodyMemberListFormPage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgGovBodyMemberListStep.ROUTE_SEGMENT}")
