package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalCouncilView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.AreYouSureFormPagePropertyDeregistration
import kotlin.test.assertEquals

class PropertyDetailsTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class PropertyDetailsLandlordView {
        @Test
        fun `the property details page loads with the property details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        fun `loading the landlord details page and clicking landlord details tab shows the landlords details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "landlord-details")
        }

        @Test
        fun `loading the landlord details page and clicking compliance information tab shows the compliance information tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToComplianceInformation()

            assertEquals(detailsPage.tabs.activeTabPanelId, COMPLIANCE_INFO_FRAGMENT)
        }

        @Test
        fun `when the landlord details tab is active clicking the property details tab shows property details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.tabs.goToPropertyDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        @Disabled("PDJB-299: the landlord-name-as-link UX was removed in the new Landlords tab design (see PR description)")
        fun `in the landlord details section the landlord name link goes the landlord view of landlord details`(page: Page) {
            // No-op: the landlord can now access their own details from the dashboard instead.
        }

        @Test
        fun `the landlords tab shows the registered landlords heading with a count of 1`(page: Page) {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.landlordsTab.registeredLandlordsHeading).containsText("Registered landlords (1)")
        }

        @Test
        fun `the landlords tab shows the current user's card with the '(you)' suffix`(page: Page) {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.landlordsTab.landlordCard().title).containsText("(you)")
        }

        @Test
        fun `the landlords tab does not show a Remove me link when the landlord is the only registered landlord`(page: Page) {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.landlordsTab.landlordCard().getAction("Remove me")).isHidden()
        }

        @Test
        fun `the landlords tab shows the sole-landlord inset with a confirm link`(page: Page) {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.landlordsTab.confirmSoleLandlordLink).isVisible()
        }

        @Test
        fun `the landlords tab shows the Invite a joint landlord button`(page: Page) {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                detailsPage.landlordsTab.inviteJointLandlordButton,
            ).isVisible()
        }

        @Test
        fun `the landlords tab does not show pending or expired invitation sections when empty`(page: Page) {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            org.assertj.core.api.Assertions.assertThat(detailsPage.landlordsTab.pendingInvitationsDetails.count()).isZero()
            org.assertj.core.api.Assertions.assertThat(detailsPage.landlordsTab.expiredInvitationsDetails.count()).isZero()
        }

        @Test
        fun `the landlords tab does not show the joint landlord requests section when empty`(page: Page) {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            org.assertj.core.api.Assertions.assertThat(detailsPage.landlordsTab.joinRequestsHeading.count()).isZero()
        }

        @Test
        fun `the landlords tab does not show the join-requests notification banner when there are no requests`(page: Page) {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)

            org.assertj.core.api.Assertions.assertThat(detailsPage.landlordsTab.joinRequestsBanner.count()).isZero()
        }

        @Test
        fun `the landlords tab falls back to the legacy summary list when the joint-landlords feature flag is disabled`(page: Page) {
            featureFlagManager.disableFeature(JOINT_LANDLORDS)
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            // The new fragment markers are absent...
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                detailsPage.landlordsTab.inviteJointLandlordButton,
            ).isHidden()
            org.assertj.core.api.Assertions.assertThat(detailsPage.landlordsTab.joinRequestsBanner.count()).isZero()
            // ...and the legacy "Registered landlord" heading is shown instead.
            assertThat(detailsPage.landlordsTab.registeredLandlordsHeading).containsText("Registered landlord")
        }

        @Test
        fun `the back link returns to the dashboard`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.backLink.clickAndWait()
            assertPageIs(page, LandlordDashboardPage::class)
        }

        @Test
        fun `the delete button redirects to the delete record page`(page: Page) {
            val propertyOwnershipId = 1
            val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
            detailsPage.deregisterPropertyLink.clickAndWait()
            assertPageIs(
                page,
                AreYouSureFormPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }

        @Test
        fun `the property details page displays the custom property type when set`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(37)

            assertThat(detailsPage.propertyDetailsSummaryList.propertyTypeRow).containsText("End terrace")
        }

        @Nested
        inner class NotificationBanner {
            // TODO PDJB-794: Reinstate notification banner assertions when notifications are re-enabled
            @Test
            fun `is visible and includes correct messages when all certs are missing`(page: Page) {
                val propertyOwnershipId = 8
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }

            // TODO PDJB-794: Reinstate notification banner assertions when notifications are re-enabled
            @Test
            fun `is visible and includes correct messages when all certs are expired`(page: Page) {
                val propertyOwnershipId = 9
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }

            // TODO PDJB-794: Reinstate notification banner assertions when notifications are re-enabled
            @Test
            fun `is visible and includes correct message when epc has a low rating and mees exemption is missing`(page: Page) {
                val propertyOwnershipId = 10
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }

            @Test
            fun `is not visible when all certs are compliant`(page: Page) {
                val propertyOwnershipId = 11
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }
        }
    }

    // TODO: PDJB-794: Re-enable these with the new update pages once update pages are created
    @Disabled
    @Nested
    inner class UpdateLinks {
        @Test
        fun `upload a gas safety cert when missing redirects to the update gas safety cert page`(page: Page) {
            val propertyOwnershipId = 8
            val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
            detailsPage.notificationBanner.updateMissingGasSafetyLink.clickAndWait()

//            assertPageIs(page, UpdateGasSafetyPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "8"))
        }

        @Test
        fun `upload a new gas safety cert when expired redirects to the update gas safety cert page`(page: Page) {
            val propertyOwnershipId = 9
            val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
            detailsPage.notificationBanner.updateExpiredGasSafetyLink.clickAndWait()

//            assertPageIs(page, UpdateGasSafetyPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "9"))
        }

        @Test
        fun `upload an eicr when missing redirects to the update eicr page`(page: Page) {
            val propertyOwnershipId = 8
            val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
            detailsPage.notificationBanner.updateMissingEicrLink.clickAndWait()

//            assertPageIs(page, UpdateEicrPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "8"))
        }

        @Test
        fun `upload a new eicr when expired redirects to the update eicr page`(page: Page) {
            val propertyOwnershipId = 9
            val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
            detailsPage.notificationBanner.updateExpiredEicrLink.clickAndWait()

//            assertPageIs(page, UpdateEicrPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "9"))
        }

        @Test
        fun `add an epc when missing redirects to the update epc page`(page: Page) {
            val propertyOwnershipId = 8
            val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
            detailsPage.notificationBanner.addEpcLink.clickAndWait()

//            assertPageIs(page, UpdateEpcPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "8"))
        }

        @Test
        fun `add an epc when expired redirects to the update epc page`(page: Page) {
            val propertyOwnershipId = 9
            val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
            detailsPage.notificationBanner.addEpcLink.clickAndWait()

//            assertPageIs(page, UpdateEpcPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "9"))
        }

        @Test
        fun `add an epc or mees exemption when epc has low rating redirects to the update epc page`(page: Page) {
            val propertyOwnershipId = 10
            val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
            detailsPage.notificationBanner.addEpcOrMeesExemptionLink.clickAndWait()

//            assertPageIs(page, UpdateEpcPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "10"))
        }
    }

    @Nested
    inner class PropertyDetailsLocalCouncilView {
        @Test
        fun `the property details page loads with the property details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        fun `loading the landlord details page and clicking landlord details tab shows the landlords details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "landlord-details")
        }

        @Test
        fun `loading the landlord details page and clicking compliance information tab shows the compliance information tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.tabs.goToComplianceInformation()

            assertEquals(detailsPage.tabs.activeTabPanelId, COMPLIANCE_INFO_FRAGMENT)
        }

        @Test
        fun `when the landlord details tab is active clicking the property details tab shows property details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.tabs.goToPropertyDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        fun `in the landlord details section the landlord name link goes the local council view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.landlordSummaryList.nameRow
                .valueLinkByText("Alexander Smith")
                .clickAndWait()

            val landlordDetailsPage = assertPageIs(page, LocalCouncilViewLandlordDetailsPage::class, mapOf("id" to "1"))

            landlordDetailsPage.backLink.clickAndWait()
            val detailsPageAfterBack =
                assertPageIs(
                    page,
                    PropertyDetailsPageLocalCouncilView::class,
                    mapOf("propertyOwnershipId" to "1"),
                )
            assertEquals(LANDLORD_DETAILS_FRAGMENT, detailsPageAfterBack.tabs.activeTabPanelId)
        }

        @Test
        fun `the back link returns to the dashboard`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.backLink.clickAndWait()
            assertPageIs(page, LocalCouncilDashboardPage::class)
        }

        @Test
        fun `the property details page displays the custom property type when set`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(37)

            assertThat(detailsPage.propertyDetailsSummaryList.propertyTypeRow).containsText("End terrace")
        }

        @Test
        fun `loading the landlord details page shows the last time the landlords record was updated`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)

            assertThat(detailsPage.insetText).containsText("updated these details on")
        }

        @Nested
        inner class NotificationBanner {
            // TODO PDJB-794: Reinstate notification banner assertions when notifications are re-enabled
            @Test
            fun `is visible and includes correct messages when all certs are missing`(page: Page) {
                val propertyOwnershipId = 8
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }

            // TODO PDJB-794: Reinstate notification banner assertions when notifications are re-enabled
            @Test
            fun `is visible and includes correct messages when all certs are expired`(page: Page) {
                val propertyOwnershipId = 9
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }

            // TODO PDJB-794: Reinstate notification banner assertions when notifications are re-enabled
            @Test
            fun `is visible and includes correct message when epc has a low rating and mees exemption is missing`(page: Page) {
                val propertyOwnershipId = 10
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }

            @Test
            fun `is not visible when all certs are compliant`(page: Page) {
                val propertyOwnershipId = 11
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }
        }
    }
}
