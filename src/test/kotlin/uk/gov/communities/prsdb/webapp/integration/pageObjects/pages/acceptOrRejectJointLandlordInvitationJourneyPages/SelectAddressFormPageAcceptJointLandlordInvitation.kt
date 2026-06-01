package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

class SelectAddressFormPageAcceptJointLandlordInvitation(
    page: Page,
) : SelectAddressFormPage(
        page,
        "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${SelectAddressStep.ROUTE_SEGMENT}",
    )
