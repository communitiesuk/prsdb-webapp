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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.DeregisterPropertyInfoPage
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater
import java.net.URI
import java.util.regex.Pattern
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
                DeregisterPropertyInfoPage::class,
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
        fun `joint property with multiple landlords shows invite button on landlord tab`(page: Page) {
            FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)

            val detailsPage = navigator.goToPropertyDetailsLandlordView(8)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.inviteJointLandlordButton.locator).isVisible()
            assertThat(detailsPage.inviteJointLandlordIndividualText).isHidden()
            assertThat(detailsPage.markAsSingleLandlordInsetText).isHidden()
        }

        @Test
        fun `joint property with sole landlord shows mark as single landlord inset text on landlord tab and invite button`(page: Page) {
            FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)

            val detailsPage = navigator.goToPropertyDetailsLandlordView(13)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.markAsSingleLandlordInsetText).isVisible()
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

        @Nested
        inner class LandlordDetails {
            @Test
            fun `when joint landlords flag is enabled the landlord tab shows summary cards`(page: Page) {
                FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)

                val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
                detailsPage.tabs.goToLandlordDetails()

                val firstCard = detailsPage.landlordSummaryCards.first()
                assertThat(firstCard.summaryList.emailAddressRow.value).containsText("alex.surname@example.com")
                assertThat(firstCard.summaryList.registrationNumberRow.value).containsText("L-CKSQ-3SX9")
            }

            @Test
            fun `multiple landlord cards are displayed with logged in user first then alphabetically`(page: Page) {
                FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)

                val detailsPage = navigator.goToPropertyDetailsLandlordView(8)
                detailsPage.tabs.goToLandlordDetails()

                assertEquals(3, detailsPage.landlordSummaryCards.size)
                val firstCard = detailsPage.landlordSummaryCards[0]
                assertEquals("Alexander Smith (you)", firstCard.title.getText())
                assertThat(firstCard.summaryList.emailAddressRow.value).containsText("alex.surname@example.com")

                val secondCard = detailsPage.landlordSummaryCards[1]
                assertEquals("Alexandra Davies", secondCard.title.getText())
                assertThat(secondCard.summaryList.emailAddressRow.value).containsText("alexandra.q.davies@example.com")

                val thirdCard = detailsPage.landlordSummaryCards[2]
                assertEquals("Tobias Evans", thirdCard.title.getText())
                assertThat(thirdCard.summaryList.emailAddressRow.value).containsText("tobyevans@example.com")
            }
        }

        @Nested
        inner class LandlordDetailsJointLandlordsDisabled {
            @Test
            fun `in the landlord details section the landlord name link goes the landlord view of landlord details`(page: Page) {
                featureFlagManager.disableFeature(JOINT_LANDLORDS)
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
            fun `when joint landlords flag is disabled the landlord tab shows the old landlord details list`(page: Page) {
                featureFlagManager.disableFeature(JOINT_LANDLORDS)
                val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
                detailsPage.tabs.goToLandlordDetails()

                assertEquals(0, detailsPage.landlordSummaryCards.size)
                assertThat(detailsPage.landlordSummaryList.nameRow).isVisible()
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

        @Nested
        inner class LandlordDetails {
            @Test
            fun `when joint landlords flag is enabled the landlord tab shows summary cards sorted alphabetically`(page: Page) {
                FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)

                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(8)
                detailsPage.tabs.goToLandlordDetails()

                assertEquals(3, detailsPage.landlordSummaryCards.size)
                assertEquals("Alexander Smith", detailsPage.landlordSummaryCards[0].title.getText())
                assertEquals("Alexandra Davies", detailsPage.landlordSummaryCards[1].title.getText())
                assertEquals("Tobias Evans", detailsPage.landlordSummaryCards[2].title.getText())
            }

            @Test
            fun `when joint landlords flag is enabled the landlord cards contain LRN, email, phone, and address`(page: Page) {
                FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)

                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(8)
                detailsPage.tabs.goToLandlordDetails()

                val firstCard = detailsPage.landlordSummaryCards[0]
                assertThat(firstCard.summaryList.registrationNumberRow.value).not().isEmpty()
                assertThat(firstCard.summaryList.emailAddressRow.value).containsText("alex.surname@example.com")
                assertThat(firstCard.summaryList.contactNumberRow.value).containsText("7111111111")
                assertThat(firstCard.summaryList.contactAddressRow.value).containsText("FA1 1AA")
            }

            @Test
            fun `when joint landlords flag is enabled the landlord cards have a view landlord record action`(page: Page) {
                FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)

                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(8)
                detailsPage.tabs.goToLandlordDetails()

                val firstCard = detailsPage.landlordSummaryCards[0]
                val actionLink = firstCard.getAction("View landlord record").link
                assertThat(actionLink).hasAttribute(
                    "href",
                    Pattern.compile("/local-council/landlord-details/1.*"),
                )
                assertThat(actionLink).hasAttribute("target", "_blank")
                assertThat(actionLink).hasAttribute("rel", "noreferrer noopener")
            }
        }

        @Nested
        inner class LandlordDetailsJointLandlordsDisabled {
            @Test
            fun `in the landlord details section the landlord name link goes the local council view of landlord details`(page: Page) {
                featureFlagManager.disableFeature(JOINT_LANDLORDS)
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
