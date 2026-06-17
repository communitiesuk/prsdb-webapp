package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.InviteJointLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasJointLandlordsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep

class HasJointLandlordsFormPageInviteJointLandlord(
    page: Page,
    urlArguments: Map<String, String>,
) : HasJointLandlordsFormBasePage(
        page,
        "${InviteJointLandlordController.getInviteJointLandlordRoute(urlArguments["propertyOwnershipId"]!!.toLong())}/" +
            HasJointLandlordsStep.ROUTE_SEGMENT,
    )
