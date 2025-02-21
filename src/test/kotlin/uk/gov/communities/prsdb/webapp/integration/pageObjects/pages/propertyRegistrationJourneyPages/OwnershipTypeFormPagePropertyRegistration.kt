package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class OwnershipTypeFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.OwnershipType.urlPathSegment}",
    ) {
    val form = OwnershipTypeForm(page)

    fun submitOwnershipType(ownershipType: OwnershipType) {
        form.ownershipTypeRadios.selectValue(ownershipType)
        form.submit()
    }

    class OwnershipTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val ownershipTypeRadios = Radios(locator)
    }
}
