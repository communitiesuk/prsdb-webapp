package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat as assertThatComponent

class PropertyReactivationSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    private val inactivePropertyOwnershipId = 52L

    @Nested
    inner class InfoStep {
        @Test
        fun `Page displays the heading, property address and what happens next`(page: Page) {
            val infoPage = navigator.goToPropertyReactivationInfoPage(inactivePropertyOwnershipId)

            assertThatComponent(infoPage.heading).containsText("Reactivate your property")
            assertThat(infoPage.propertyAddress).containsText("Inactive House")
            assertThat(infoPage.propertyAddress).containsText("IN1 1AA")
            assertThat(infoPage.bulletList).hasCount(5)
            assertThatComponent(infoPage.insetText).containsText("submit in one go")
        }
    }

    @Nested
    inner class AlreadyRegisteredStep {
        @Test
        fun `Page displays the heading and links to register a new property`(page: Page) {
            val alreadyRegisteredPage = navigator.skipToPropertyReactivationAlreadyRegisteredPage(inactivePropertyOwnershipId)

            assertThatComponent(alreadyRegisteredPage.heading).containsText("There is an active registration for this property")
            assertThat(alreadyRegisteredPage.registerNewPropertyLink.locator)
                .hasAttribute("href", RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
        }
    }
}
