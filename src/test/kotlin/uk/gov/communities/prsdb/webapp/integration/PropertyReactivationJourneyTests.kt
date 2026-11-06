package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckAnswersPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ConfirmationPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ElectricalSafetyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EpcFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.GasSafetyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.InfoFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.LicensingFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.TenancyDetailsFormPagePropertyReactivation
import kotlin.test.assertEquals

class PropertyReactivationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    companion object {
        const val INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER = 52L
        const val INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER = 6L
        const val INACTIVE_PROPERTY_OWNED_BY_ANOTHER_LANDLORD = 2L
        const val ACTIVE_PROPERTY = 1L
    }

    @Test
    fun `a landlord can walk through every provide later step to the confirmation page`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        assertThat(infoPage.heading).containsText("PDJB-2105")
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments)
        assertThat(licensingPage.heading).containsText("PDJB-2037")
        licensingPage.submit()

        val gasSafetyPage = assertPageIs(page, GasSafetyFormPagePropertyReactivation::class, urlArguments)
        assertThat(gasSafetyPage.heading).containsText("PDJB-2038")
        gasSafetyPage.submit()

        val electricalSafetyPage = assertPageIs(page, ElectricalSafetyFormPagePropertyReactivation::class, urlArguments)
        assertThat(electricalSafetyPage.heading).containsText("PDJB-2039")
        electricalSafetyPage.submit()

        val epcPage = assertPageIs(page, EpcFormPagePropertyReactivation::class, urlArguments)
        assertThat(epcPage.heading).containsText("PDJB-2040")
        epcPage.submit()

        val tenancyDetailsPage = assertPageIs(page, TenancyDetailsFormPagePropertyReactivation::class, urlArguments)
        assertThat(tenancyDetailsPage.heading).containsText("PDJB-2041")
        tenancyDetailsPage.submit()

        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        assertThat(checkAnswersPage.heading).containsText("PDJB-2042")
        checkAnswersPage.submit()

        assertPageIs(page, ConfirmationPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord goes straight to check answers when there are no details to provide`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        checkAnswersPage.backLink.clickAndWait()

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back link on a provide later step returns to the previous step`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments)
        licensingPage.backLink.clickAndWait()

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord receives a 404 for an active property`() {
        val response = navigator.navigate(ReactivatePropertyController.getPropertyReactivationPath(ACTIVE_PROPERTY))

        assertEquals(404, response?.status())
    }

    @Test
    fun `a landlord receives a 404 for an inactive property they do not own`() {
        val response =
            navigator.navigate(ReactivatePropertyController.getPropertyReactivationPath(INACTIVE_PROPERTY_OWNED_BY_ANOTHER_LANDLORD))

        assertEquals(404, response?.status())
    }
}
