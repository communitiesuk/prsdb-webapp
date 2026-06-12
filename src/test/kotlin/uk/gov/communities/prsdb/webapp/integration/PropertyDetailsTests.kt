package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_ACTIONS_MAY2026_REDESIGN
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalCouncilView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.AreYouSureFormPagePropertyDeregistration
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater
import java.net.URI
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
        fun `in the landlord details section the landlord name link goes the landlord view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.landlordSummaryList.nameRow
                .valueLinkByText("Alexander Smith")
                .clickAndWait()

            val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

            landlordDetailsPage.backLink.clickAndWait()
            val detailsPageAfterBack =
                assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "1"))
            assertEquals(LANDLORD_DETAILS_FRAGMENT, detailsPageAfterBack.tabs.activeTabPanelId)
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

        @Test
        fun `individual property shows invite text link and not invite button on landlord tab`(page: Page) {
            FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)

            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.inviteJointLandlordIndividualText).isVisible()
            assertThat(detailsPage.inviteJointLandlordLink.locator).isVisible()
            assertThat(detailsPage.inviteJointLandlordButton.locator).isHidden()
        }

        @Test
        fun `joint property shows invite button and not invite text on landlord tab`(page: Page) {
            FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)

            val detailsPage = navigator.goToPropertyDetailsLandlordView(8)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.inviteJointLandlordButton.locator).isVisible()
            assertThat(detailsPage.inviteJointLandlordIndividualText).isHidden()
        }

        // Test properties used for notification banner tests:
        // - Property 8:  Occupied, has gas supply but no cert, no electrical, no EPC
        // - Property 9:  Unoccupied, gas expired (issued 1990-02-28), electrical missing, EPC expired (2021-03-16, rating 'c')
        // - Property 10: Occupied, no gas supply, electrical missing, EPC valid (expires 2031-02-28, rating 'g', no MEES exemption)
        // - Property 11: Unoccupied, no gas supply, electrical missing, EPC valid (expires 2031-02-28, rating 'g', has MEES exemption)
        @Nested
        inner class NotificationBanner {
            @BeforeEach
            fun enableFlag() {
                FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(COMPLIANCE_ACTIONS_MAY2026_REDESIGN)
            }

            @Test
            fun `is visible and includes correct messages when all certs are missing`(page: Page) {
                val propertyOwnershipId = 8
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("You must add compliance certificates for this property")
            }

            @Test
            fun `is visible and includes correct messages when all certs are expired`(page: Page) {
                val propertyOwnershipId = 9
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("Multiple compliance certificates for this property have expired")
            }

            @Test
            fun `is visible and includes correct message when epc has a low rating and mees exemption is missing`(page: Page) {
                val propertyOwnershipId = 10
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("You must add compliance certificates for this property")
            }

            @Test
            fun `is not visible when all certs are compliant`(page: Page) {
                val propertyOwnershipId = 11
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }

            @Test
            fun `includes a link to the compliance information tab`(page: Page) {
                val propertyOwnershipId = 8
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner.viewComplianceCertificatesLink).isVisible()
                assertThat(detailsPage.notificationBanner.viewComplianceCertificatesLink).hasAttribute(
                    "href",
                    "#$COMPLIANCE_INFO_FRAGMENT",
                )
            }
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
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.lastModifiedInsetText).containsText("updated these details on")
        }

        // Test properties used for notification banner tests:
        // - Property 8:  Occupied, has gas supply but no cert, no electrical, no EPC
        // - Property 9:  Unoccupied, gas expired (issued 1990-02-28), electrical missing, EPC expired (2021-03-16, rating 'c')
        // - Property 10: Occupied, no gas supply, electrical missing, EPC valid (expires 2031-02-28, rating 'g', no MEES exemption)
        // - Property 11: Unoccupied, no gas supply, electrical missing, EPC valid (expires 2031-02-28, rating 'g', has MEES exemption)
        @Nested
        inner class NotificationBanner {
            @BeforeEach
            fun enableFlag() {
                FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(COMPLIANCE_ACTIONS_MAY2026_REDESIGN)
            }

            @Test
            fun `is visible and includes correct messages when all certs are missing`(page: Page) {
                val propertyOwnershipId = 8
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("You must add compliance certificates for this property")
            }

            @Test
            fun `is visible and includes correct messages when all certs are expired`(page: Page) {
                val propertyOwnershipId = 9
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("Multiple compliance certificates for this property have expired")
            }

            @Test
            fun `is visible and includes correct message when epc has a low rating and mees exemption is missing`(page: Page) {
                val propertyOwnershipId = 10
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("You must add compliance certificates for this property")
            }

            @Test
            fun `is not visible when all certs are compliant`(page: Page) {
                val propertyOwnershipId = 11
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }
        }
    }

    @Nested
    inner class PropertyDetailsInvitations : NestedIntegrationTestWithImmutableData("data-joint-landlord-invitation.sql") {
        @BeforeEach
        fun enableJointLandlordsFlag() {
            FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)
        }

        @Test
        fun `property details page shows pending invitations section with correct email`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.pendingInvitationsDetails).isVisible()
            assertThat(detailsPage.pendingInvitationsDetails).containsText("Pending invitations (1)")
            assertThat(detailsPage.pendingInvitationsDetails).containsText("pending@example.com")
        }

        @Test
        fun `property details page shows expired invitations section with correct email`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.expiredInvitationsDetails).isVisible()
            assertThat(detailsPage.expiredInvitationsDetails).containsText("Expired invitations (1)")
            assertThat(detailsPage.expiredInvitationsDetails).containsText("expired@example.com")
        }

        @Test
        fun `pending invitation shows expiry and sent date details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.pendingInvitationsDetails).containsText("Expires in")
            assertThat(detailsPage.pendingInvitationsDetails).containsText("Sent on")
        }

        @Test
        fun `expired invitation shows expired date`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.expiredInvitationsDetails).containsText("Expired on")
        }

        @Test
        fun `invitation sections are not shown when feature flag is disabled`(page: Page) {
            featureFlagManager.disableFeature(JOINT_LANDLORDS)

            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.pendingInvitationsDetails).hasCount(0)
            assertThat(detailsPage.expiredInvitationsDetails).hasCount(0)
        }
    }

    @Nested
    inner class ResendInvitation :
        IntegrationTestWithMutableData.NestedIntegrationTestWithMutableData(
            "data-joint-landlord-invitation.sql",
        ) {
        @BeforeEach
        fun setup() {
            FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)
            whenever(absoluteUrlProvider.buildJointLandlordInvitationUri(any()))
                .thenReturn(URI("http://localhost:$port/invite/test-token"))
        }

        @Test
        fun `clicking send new invitation email on a pending invitation shows success banner`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.pendingInvitationsDetails.locator("summary").click()
            detailsPage.pendingInvitationsDetails.getByText("Send a new email invitation").click()
            page.waitForLoadState()

            val successBanner = page.locator(".govuk-notification-banner--success")
            assertThat(successBanner).isVisible()
            assertThat(successBanner).containsText("pending@example.com")
        }

        @Test
        fun `clicking send new invitation email on an expired invitation shows success banner`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.expiredInvitationsDetails.locator("summary").click()
            detailsPage.expiredInvitationsDetails.getByText("Send a new invitation email").click()
            page.waitForLoadState()

            val successBanner = page.locator(".govuk-notification-banner--success")
            assertThat(successBanner).isVisible()
            assertThat(successBanner).containsText("expired@example.com")
        }
    }
}
