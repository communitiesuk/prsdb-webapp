package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.AnyoneInOwnershipChainFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersBackRoutingFormPagePropertyRegistration

class PropertyRegistrationSuperiorLandlordsSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class AnyoneInOwnershipChainStep {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.form.submit()
            assertThat(anyoneInOwnershipChainPage.form.getErrorMessage())
                .containsText("Select if there is anyone else in the ownership chain")
        }

        // TODO PDJB-1961: update the expected page when the real freehold type page is added
        @Test
        fun `Back link returns to the freehold type page when freeholders were not asked about`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.backLink.clickAndWait()
            assertPageIs(page, FreeholdTypeFormPagePropertyRegistration::class)
        }

        // TODO PDJB-1969: update the expected page to the freeholders list page when freeholders back routing becomes an internal step
        @Test
        fun `Back link returns to the freeholders task when freeholders were asked about`(page: Page) {
            // TODO: PDJB-1969: Use state builder methods added in the freeholders list area to set up this test without manual navigation
            navigator.skipToPropertyRegistrationFreeholdersListPage(jointFreeholders = false).submit()
            assertPageIs(page, FreeholdersBackRoutingFormPagePropertyRegistration::class).submit()
            val anyoneInOwnershipChainPage = assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class)
            anyoneInOwnershipChainPage.backLink.clickAndWait()
            assertPageIs(page, FreeholdersBackRoutingFormPagePropertyRegistration::class)
        }
    }
}
