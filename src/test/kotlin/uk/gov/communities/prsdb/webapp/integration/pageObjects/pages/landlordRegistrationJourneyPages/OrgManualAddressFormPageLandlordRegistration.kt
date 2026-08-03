package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.OrgAddressTask

class OrgManualAddressFormPageLandlordRegistration(
    page: Page,
) : ManualAddressFormPage(
        page,
        RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE +
            "/${OrgAddressTask.ORGANISATION_ADDRESS_ROUTE_SEGMENT}" +
            "/${ManualAddressStep.ROUTE_SEGMENT}",
    )
