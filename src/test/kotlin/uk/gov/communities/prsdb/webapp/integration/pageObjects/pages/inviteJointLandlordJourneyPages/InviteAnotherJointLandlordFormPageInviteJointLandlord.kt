package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.InviteJointLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep

class InviteAnotherJointLandlordFormPageInviteJointLandlord(
    page: Page,
    urlArguments: Map<String, String>,
) : EmailFormPage(
        page,
        InviteJointLandlordController.getInviteJointLandlordRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
}
