package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.GovBodyMemberAddressTask

class OrgGovBodyMemberManualAddressFormPageLandlordRegistration(
    page: Page,
) : ManualAddressFormPage(
        page,
        "$LANDLORD_REGISTRATION_ROUTE/${GovBodyMemberAddressTask.ROUTE_SEGMENT}/${ManualAddressStep.ROUTE_SEGMENT}",
    )
