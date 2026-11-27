package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.InternationalAddressTask

class FreeholderNonUkAddressFormPagePropertyRegistration(
    page: Page,
) : ManualAddressFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/" +
            "${FreeholderAddressTask.ROUTE_SEGMENT}/${InternationalAddressTask.INTERNATIONAL_MANUAL_ADDRESS_ROUTE_SEGMENT}",
    )
