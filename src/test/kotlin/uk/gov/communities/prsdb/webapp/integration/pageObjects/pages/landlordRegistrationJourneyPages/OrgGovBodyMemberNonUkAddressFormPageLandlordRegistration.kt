package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.GovBodyMemberAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.InternationalAddressTask

class OrgGovBodyMemberNonUkAddressFormPageLandlordRegistration(
    page: Page,
) : ManualAddressFormPage(
        page,
        "$LANDLORD_REGISTRATION_ROUTE/${GovBodyMemberAddressTask.ROUTE_SEGMENT}/" +
            InternationalAddressTask.INTERNATIONAL_MANUAL_ADDRESS_ROUTE_SEGMENT,
    )
