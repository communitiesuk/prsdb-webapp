package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_ACTIONS_MAY2026_REDESIGN
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater

class PropertyComplianceTabFeatureFlagTests : IntegrationTest() {
    @Nested
    inner class FlagDisabled :
        NestedIntegrationTestWithImmutableData("data-local.sql") {
        @BeforeEach
        fun disableFlag() {
            featureFlagManager.disableFeature(COMPLIANCE_ACTIONS_MAY2026_REDESIGN)
        }

        @Test
        fun `notification banner is hidden`(page: Page) {
            // Property 9: unoccupied, gas expired, EPC expired
            val detailsPage = navigator.goToPropertyDetailsLandlordView(9)
            detailsPage.tabs.goToComplianceInformation()

            assertThat(detailsPage.notificationBanner).isHidden()
        }

        @Test
        fun `gas safety card has no certificate status row`(page: Page) {
            // Property 37: has gas cert, electrical cert, and EPC
            val detailsPage = navigator.goToPropertyDetailsLandlordView(37)
            detailsPage.tabs.goToComplianceInformation()

            assertThat(detailsPage.gasSafetyCard.summaryList.certificateStatusRow).isHidden()
        }

        @Test
        fun `electrical safety card has no certificate status row`(page: Page) {
            // Property 37: has gas cert, electrical cert, and EPC
            val detailsPage = navigator.goToPropertyDetailsLandlordView(37)
            detailsPage.tabs.goToComplianceInformation()

            assertThat(detailsPage.electricalSafetyCard.summaryList.certificateStatusRow).isHidden()
        }

        @Test
        fun `epc card has no certificate status row`(page: Page) {
            // Property 9: has expired EPC
            val detailsPage = navigator.goToPropertyDetailsLandlordView(9)
            detailsPage.tabs.goToComplianceInformation()

            assertThat(detailsPage.epcCard.summaryList.certificateStatusRow).isHidden()
        }
    }

    @Nested
    inner class FlagEnabled :
        NestedIntegrationTestWithImmutableData("data-local.sql") {
        @BeforeEach
        fun enableFlag() {
            FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(COMPLIANCE_ACTIONS_MAY2026_REDESIGN)
        }

        @Test
        fun `notification banner is visible when certs are expired`(page: Page) {
            // Property 9: unoccupied, gas expired, EPC expired
            val detailsPage = navigator.goToPropertyDetailsLandlordView(9)
            detailsPage.tabs.goToComplianceInformation()

            assertThat(detailsPage.notificationBanner).isVisible()
        }

        @Test
        fun `gas safety card has certificate status row`(page: Page) {
            // Property 37: has gas cert, electrical cert, and EPC
            val detailsPage = navigator.goToPropertyDetailsLandlordView(37)
            detailsPage.tabs.goToComplianceInformation()

            assertThat(detailsPage.gasSafetyCard.summaryList.certificateStatusRow).isVisible()
        }

        @Test
        fun `electrical safety card has certificate status row`(page: Page) {
            // Property 37: has gas cert, electrical cert, and EPC
            val detailsPage = navigator.goToPropertyDetailsLandlordView(37)
            detailsPage.tabs.goToComplianceInformation()

            assertThat(detailsPage.electricalSafetyCard.summaryList.certificateStatusRow).isVisible()
        }

        @Test
        fun `epc card has certificate status row`(page: Page) {
            // Property 9: has expired EPC
            val detailsPage = navigator.goToPropertyDetailsLandlordView(9)
            detailsPage.tabs.goToComplianceInformation()

            assertThat(detailsPage.epcCard.summaryList.certificateStatusRow).isVisible()
        }
    }
}
