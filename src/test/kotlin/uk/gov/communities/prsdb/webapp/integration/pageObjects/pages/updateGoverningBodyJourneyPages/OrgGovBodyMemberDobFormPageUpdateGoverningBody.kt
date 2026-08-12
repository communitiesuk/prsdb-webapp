package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController.Companion.UPDATE_GOVERNING_BODY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberDobStep

class OrgGovBodyMemberDobFormPageUpdateGoverningBody(
    page: Page,
) : DateFormPage(page, "$UPDATE_GOVERNING_BODY_ROUTE/${OrgGovBodyMemberDobStep.ROUTE_SEGMENT}")
