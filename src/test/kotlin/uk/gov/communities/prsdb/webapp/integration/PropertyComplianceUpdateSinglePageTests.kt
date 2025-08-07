package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.FireSafetyDeclarationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.KeepPropertySafePagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.ResponsibilityToTenantsPagePropertyComplianceUpdate
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PropertyComplianceUpdateSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @Test
    fun `Submitting UpdateGasSafetyStep with no option selected returns an error`() {
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
        updateGasSafetyPage.form.submit()
        assertThat(
            updateGasSafetyPage.form.getErrorMessage(),
        ).containsText("Select whether you want to add a new certificate or exemption")
    }

    @Test
    fun `Submitting UpdateEicrStep with no option selected returns an error`() {
        val updateEicrPage = navigator.goToPropertyComplianceUpdateUpdateEicrPage(PROPERTY_OWNERSHIP_ID)
        updateEicrPage.form.submit()
        assertThat(updateEicrPage.form.getErrorMessage())
            .containsText("Select whether you want to add a new EICR or exemption")
    }

    @Test
    fun `Submitting UpdateEpcStep with no value entered returns an error`() {
        val updateEpcPage = navigator.goToPropertyComplianceUpdateUpdateEpcPage(PROPERTY_OWNERSHIP_ID)
        updateEpcPage.form.submit()
        assertThat(updateEpcPage.form.getErrorMessage())
            .containsText("Select whether you want to add a new certificate or exemption")
    }

    @Test
    fun `User can review their fire safety declaration`(page: Page) {
        // Go to property compliance tab of property record
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()

        // Review fire safety declaration
        propertyDetailsPage.propertyComplianceSummaryList.fireSafetyRow.clickActionLinkAndWait()
        val reviewFireSafetyPage = assertPageIs(page, FireSafetyDeclarationPagePropertyComplianceUpdate::class, urlArguments)
        assertContains(reviewFireSafetyPage.heading.getText(), "Fire safety in your property")

        // Go back to property record
        reviewFireSafetyPage.returnToRecordButton.clickAndWait()
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `User can review their property safety declaration`(page: Page) {
        // Go to property compliance tab of property record
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()

        // Review property safety declaration
        propertyDetailsPage.propertyComplianceSummaryList.propertySafetyRow.clickActionLinkAndWait()
        val reviewPropertySafetyPage = assertPageIs(page, KeepPropertySafePagePropertyComplianceUpdate::class, urlArguments)
        assertContains(reviewPropertySafetyPage.heading.getText(), "Keeping this property safe")

        // Go back to property record
        reviewPropertySafetyPage.returnToRecordButton.clickAndWait()
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `User can review their responsibility to tenants declaration`(page: Page) {
        // Go to property compliance tab of property record
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()

        // Review responsibility to tenants declaration
        propertyDetailsPage.propertyComplianceSummaryList.responsibilityToTenantsRow.clickActionLinkAndWait()
        val reviewResponsibilityToTenantsPage = assertPageIs(page, ResponsibilityToTenantsPagePropertyComplianceUpdate::class, urlArguments)
        assertContains(
            reviewResponsibilityToTenantsPage.heading.getText(),
            "Make sure you follow your legal responsibilities to your tenants",
        )

        // Go back to property record
        reviewResponsibilityToTenantsPage.returnToRecordButton.clickAndWait()
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 8L

        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())
    }
}
