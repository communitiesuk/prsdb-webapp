package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class LicensingTypeFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(
        page,
        urlSegment,
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
