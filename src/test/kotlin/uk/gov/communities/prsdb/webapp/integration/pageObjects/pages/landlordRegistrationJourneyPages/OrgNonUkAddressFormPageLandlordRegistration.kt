package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.InternationalAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.OrgInternationalAddressTask

class OrgNonUkAddressFormPageLandlordRegistration(
    page: Page,
) : ManualAddressFormPage(
        page,
        RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE +
            "/${OrgInternationalAddressTask.ROUTE_SEGMENT}" +
            "/${InternationalAddressTask.INTERNATIONAL_MANUAL_ADDRESS_ROUTE_SEGMENT}",
    )
