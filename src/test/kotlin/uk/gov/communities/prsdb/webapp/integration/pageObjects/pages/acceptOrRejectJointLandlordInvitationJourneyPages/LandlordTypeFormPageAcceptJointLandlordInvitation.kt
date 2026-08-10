package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordTypeFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep

class LandlordTypeFormPageAcceptJointLandlordInvitation(
    page: Page,
) : LandlordTypeFormBasePage(
        page,
        "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${LandlordTypeStep.ROUTE_SEGMENT}",
    )
