package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep

class OrgIsRegisteredCharityFormPageAcceptJointLandlordInvitation(
    page: Page,
) : PageWithYesNoRadios(
        page,
        "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${OrgIsRegisteredCharityStep.ROUTE_SEGMENT}",
    )
