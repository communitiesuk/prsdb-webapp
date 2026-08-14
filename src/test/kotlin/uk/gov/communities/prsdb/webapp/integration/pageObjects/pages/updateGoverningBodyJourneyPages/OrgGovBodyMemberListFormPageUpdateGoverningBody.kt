package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController.Companion.UPDATE_GOVERNING_BODY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgGovBodyMemberListFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep

class OrgGovBodyMemberListFormPageUpdateGoverningBody(
    page: Page,
) : OrgGovBodyMemberListFormPage(page, "$UPDATE_GOVERNING_BODY_ROUTE/${OrgGovBodyMemberListStep.ROUTE_SEGMENT}")
