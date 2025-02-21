package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LicensingTypeFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.LicensingType.urlPathSegment}",
    ) {
    val form = LicensingTypeForm(page)

    fun submitLicensingType(licensingType: LicensingType) {
        form.licensingTypeRadios.selectValue(licensingType)
        form.submit()
    }

    class LicensingTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val licensingTypeRadios = Radios(locator)
    }
}
