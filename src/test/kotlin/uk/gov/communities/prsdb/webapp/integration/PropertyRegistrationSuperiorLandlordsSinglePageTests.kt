package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasAnySuperiorLandlordsFormPagePropertyRegistration

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

        @Test
        fun `Back link returns to the freeholders list page when freeholders were asked about`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage(afterFreeholders = true)
            anyoneInOwnershipChainPage.backLink.clickAndWait()
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class SuperiorLandlordsListStep {
        @Test
        fun `Superior landlords list shows the number of people in the ownership chain`() {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            assertThat(superiorLandlordsListPage.heading).containsText("You’ve added 3 people in the ownership chain")
        }

        // TODO PDJB-1978: update the expected page when has any superior landlords becomes an internal step
        @Test
        fun `Back link returns to the has any superior landlords page`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage.backLink.clickAndWait()
            assertPageIs(page, HasAnySuperiorLandlordsFormPagePropertyRegistration::class)
        }
    }
}
