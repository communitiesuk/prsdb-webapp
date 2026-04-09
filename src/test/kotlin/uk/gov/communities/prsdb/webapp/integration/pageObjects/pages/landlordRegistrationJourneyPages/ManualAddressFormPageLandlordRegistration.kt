package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep

class ManualAddressFormPageLandlordRegistration(
    page: Page,
) : ManualAddressFormPage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${ManualAddressStep.ROUTE_SEGMENT}",
    )
