package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.DeregisterPropertyInfoPage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
        fun `individual property shows invite text link and not invite button on landlord tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.inviteJointLandlordIndividualText).isVisible()
            assertThat(detailsPage.inviteJointLandlordLink.locator).isVisible()
            assertThat(detailsPage.inviteJointLandlordButton.locator).isHidden()
        }

        @Test
        fun `joint property with multiple landlords shows invite button on landlord tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(8)
            detailsPage.tabs.goToLandlordDetails()

            assertThat(detailsPage.inviteJointLandlordButton.locator).isVisible()
            assertThat(detailsPage.inviteJointLandlordIndividualText).isHidden()
            assertThat(detailsPage.markAsSingleLandlordInsetText).isHidden()
        }

        @Test
        fun `joint property with sole landlord shows mark as single landlord inset text on landlord tab and invite button`(page: Page) {
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
        inner class ComplianceTab {
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

        @Nested
        inner class LandlordDetails {
            @Test
            fun `when joint landlords flag is enabled the landlord tab shows summary cards`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
                detailsPage.tabs.goToLandlordDetails()

                val firstCard = detailsPage.landlordSummaryCards.first()
                assertThat(firstCard.summaryList.emailAddressRow.value).containsText("alex.surname@example.com")
                assertThat(firstCard.summaryList.registrationNumberRow.value).containsText("L-CKSQ-3SX9")
            }

            @Test
            fun `multiple landlord cards are displayed with logged in user first then alphabetically`(page: Page) {
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

        @Nested
        inner class LandlordDetails {
            @Test
            fun `when joint landlords flag is enabled the landlord tab shows summary cards sorted alphabetically`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(8)
                detailsPage.tabs.goToLandlordDetails()

                assertEquals(3, detailsPage.landlordSummaryCards.size)
                assertEquals("Alexander Smith", detailsPage.landlordSummaryCards[0].title.getText())
                assertEquals("Alexandra Davies", detailsPage.landlordSummaryCards[1].title.getText())
                assertEquals("Tobias Evans", detailsPage.landlordSummaryCards[2].title.getText())
            }

            @Test
            fun `when joint landlords flag is enabled the landlord cards contain LRN, email, phone, and address`(page: Page) {
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

        // Test properties used for notification banner tests:
        // - Property 8:  Occupied, has gas supply but no cert, no electrical, no EPC
        // - Property 9:  Unoccupied, gas expired (issued 1990-02-28), electrical missing, EPC expired (2021-03-16, rating 'c')
        // - Property 10: Occupied, no gas supply, electrical missing, EPC valid (expires 2031-02-28, rating 'g', no MEES exemption)
        // - Property 11: Unoccupied, no gas supply, electrical missing, EPC valid (expires 2031-02-28, rating 'g', has MEES exemption)
        @Nested
        inner class NotificationBanner {
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
    }

    @Nested
    inner class ResendInvitation :
        IntegrationTestWithMutableData.NestedIntegrationTestWithMutableData(
            "data-joint-landlord-invitation.sql",
        ) {
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

    @Nested
    inner class BeforePdjb939Layout {
        // Flag-off (legacy) property record layout. Delete this class when PDJB-939 is permanently on.
        @BeforeEach
        fun disableFlag() {
            featureFlagManager.disableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        }

        @Test
        fun `landlord view displays the custom property type when set`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(37)

            assertThat(detailsPage.beforePdjb939SummaryList.propertyTypeRow).containsText("End terrace")
        }

        @Test
        fun `local council view displays the custom property type when set`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(37)

            assertThat(detailsPage.beforePdjb939SummaryList.propertyTypeRow).containsText("End terrace")
        }
    }

    @Nested
    inner class PropertyDetailsTab {
        @Test
        fun `landlord view groups the property record into sections`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)

            assertThat(detailsPage.sectionHeading("Property details")).isVisible()
            assertThat(detailsPage.sectionHeading("Ownership")).isVisible()
            assertThat(detailsPage.sectionHeading("Occupied by tenants")).isVisible()
            assertThat(detailsPage.sectionHeading("Property licensing")).isVisible()
            assertThat(detailsPage.propertyDetailsSummaryList.ownershipTypeRow.value).isVisible()
            assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).isVisible()

            assertThat(detailsPage.sectionHeading("Licensing information")).not().isVisible()
            assertThat(detailsPage.sectionHeading("Tenancy and rental information")).not().isVisible()
        }

        @Test
        fun `landlord view displays the custom property type when set`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(37)

            assertThat(detailsPage.propertyDetailsSummaryList.propertyTypeRow).containsText("End terrace")
        }

        @Test
        fun `local council view displays the custom property type when set`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(37)

            assertThat(detailsPage.propertyDetailsSummaryList.propertyTypeRow).containsText("End terrace")
        }

        @Test
        fun `landlord view hides the tenancy section for an unoccupied property`(page: Page) {
            // Property 12: unoccupied, no licence, tenancy details not applicable.
            val detailsPage = navigator.goToPropertyDetailsLandlordView(12)

            assertThat(detailsPage.sectionHeading("Occupied by tenants")).isVisible()
            assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
            assertThat(detailsPage.sectionHeading("Tenancy details")).not().isVisible()
        }

        @Nested
        inner class OccupiedWithLicensingAndTenancySkipped {
            // Property 39: occupied (0 tenants, 0 households), no licence, no tenancy details.
            // last_occupied_date is seeded to 7 days ago, so the provide-later deadline is
            // (7 days ago + PROVIDE_LATER_DEADLINE_DAYS) days from today.
            private val expectedDeadline =
                LocalDate
                    .now()
                    .minusDays(7)
                    .plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK))

            @Test
            fun `landlord view shows provide later rows with the deadline date for both licensing and tenancy`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(39)

                assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("Yes")
                assertThat(detailsPage.sectionHeading("Tenancy details")).isVisible()
                assertThat(
                    detailsPage.propertyDetailsSummaryList.licensingRow.value,
                ).containsText("Provide this later (before $expectedDeadline)")
                assertThat(
                    detailsPage.propertyDetailsSummaryList.tenancyRow.value,
                ).containsText("Provide this later (before $expectedDeadline)")
            }

            @Test
            fun `local council view shows provide later paragraphs with the deadline date for both licensing and tenancy`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(39)

                assertThat(
                    detailsPage.bodyParagraph("The landlords must provide these details before $expectedDeadline"),
                ).hasCount(2)
            }

            @Test
            fun `landlord view shows the combined provide-later and compliance notification banner`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(39)

                assertThat(detailsPage.notificationBanner.content.heading)
                    .containsText("You must finish providing property and tenancy details and valid compliance certificates")
            }

            @Test
            fun `local council view shows the combined provide-later and compliance notification banner`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(39)

                assertThat(detailsPage.notificationBanner.content.heading)
                    .containsText("This registration is missing property and tenancy details and valid compliance certificates")
            }
        }

        @Nested
        inner class UnoccupiedWithLicensingSkipped {
            // Property 9: unoccupied (0 tenants), no licence.
            @Test
            fun `landlord view shows a provide later licensing row and hides the tenancy section`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(9)

                assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
                assertThat(detailsPage.propertyDetailsSummaryList.licensingRow.value)
                    .containsText("Provide this later (within 28 days of the property being occupied)")
                assertThat(detailsPage.sectionHeading("Tenancy details")).not().isVisible()
            }

            @Test
            fun `local council view shows a not provided licensing paragraph and hides the tenancy section`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(9)

                assertThat(detailsPage.sectionHeading("Tenancy details")).not().isVisible()
                assertThat(
                    detailsPage.bodyParagraph("These details have not been provided yet"),
                ).hasCount(1)
            }

            @Test
            fun `landlord view does not show a provide-later notification banner for an unoccupied property`(page: Page) {
                navigator.goToPropertyDetailsLandlordView(9)

                assertThat(page.getByText("You must finish adding")).not().isVisible()
                assertThat(page.getByText("You must finish providing")).not().isVisible()
            }
        }

        @Nested
        inner class OccupiedWithAllFieldsCompleted {
            // Property 40: occupied (2 tenants, 1 household), licence present, full tenancy details.
            @Test
            fun `landlord view shows the licensing type and the tenancy details`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(40)

                assertThat(detailsPage.sectionHeading("Tenancy details")).isVisible()
                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText("1")
            }

            @Test
            fun `local council view shows the licensing type and the tenancy details`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(40)

                assertThat(detailsPage.sectionHeading("Tenancy details")).isVisible()
                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText("1")
                assertThat(
                    detailsPage.bodyParagraph("These details have not been provided yet"),
                ).hasCount(0)
            }

            @Test
            fun `landlord view does not show a provide-later notification banner when all fields are completed`(page: Page) {
                navigator.goToPropertyDetailsLandlordView(40)

                assertThat(page.getByText("You must finish adding")).not().isVisible()
                assertThat(page.getByText("You must finish providing")).not().isVisible()
            }
        }

        @Nested
        inner class UnoccupiedWithAllFieldsCompleted {
            // Property 7: unoccupied (0 tenants), licence present.
            @Test
            fun `landlord view shows the licensing type and hides the tenancy section`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(7)

                assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(detailsPage.sectionHeading("Tenancy details")).not().isVisible()
            }

            @Test
            fun `local council view shows the licensing type and hides the tenancy section`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(7)

                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(detailsPage.sectionHeading("Tenancy details")).not().isVisible()
                assertThat(
                    detailsPage.bodyParagraph("These details have not been provided yet"),
                ).hasCount(0)
            }
        }

        @Nested
        inner class OccupiedWithTenancySkippedOnly {
            // Property 41: occupied, licence present, tenancy skipped, fully compliant (no compliance issue).
            private val expectedDeadline =
                LocalDate
                    .now()
                    .minusDays(7)
                    .plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK))

            @Test
            fun `landlord view shows the licensing type and a tenancy provide-later row with the deadline date`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(41)

                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(detailsPage.sectionHeading("Tenancy details")).isVisible()
                assertThat(
                    detailsPage.propertyDetailsSummaryList.tenancyRow.value,
                ).containsText("Provide this later (before $expectedDeadline)")
            }

            @Test
            fun `landlord view shows only the tenancy provide-later notification banner`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(41)

                assertThat(detailsPage.notificationBanner.content.heading).containsText("You must finish adding")
                assertThat(detailsPage.notificationBanner.content.heading).containsText("tenancy details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("licensing details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("compliance certificates")
            }

            @Test
            fun `local council view shows a tenancy provide-later paragraph and a tenancy-only notification banner`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(41)

                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(
                    detailsPage.bodyParagraph("The landlords must provide these details before $expectedDeadline"),
                ).hasCount(1)
                assertThat(detailsPage.notificationBanner.content.heading).containsText("This property is missing")
                assertThat(detailsPage.notificationBanner.content.heading).containsText("tenancy details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("licensing details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("compliance certificates")
            }
        }

        @Nested
        inner class OccupiedWithLicensingSkippedOnly {
            // Property 42: occupied, no licence, tenancy provided, fully compliant (no compliance issue).
            private val expectedDeadline =
                LocalDate
                    .now()
                    .minusDays(7)
                    .plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK))

            @Test
            fun `landlord view shows the tenancy details and a licensing provide-later row with the deadline date`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(42)

                assertThat(detailsPage.sectionHeading("Tenancy details")).isVisible()
                assertThat(detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText("1")
                assertThat(
                    detailsPage.propertyDetailsSummaryList.licensingRow.value,
                ).containsText("Provide this later (before $expectedDeadline)")
            }

            @Test
            fun `landlord view shows only the licensing provide-later notification banner`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(42)

                assertThat(detailsPage.notificationBanner.content.heading).containsText("You must finish adding")
                assertThat(detailsPage.notificationBanner.content.heading).containsText("licensing details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("tenancy details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("compliance certificates")
            }

            @Test
            fun `local council view shows a licensing provide-later paragraph and a licensing-only notification banner`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(42)

                assertThat(detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText("1")
                assertThat(
                    detailsPage.bodyParagraph("The landlords must provide these details before $expectedDeadline"),
                ).hasCount(1)
                assertThat(detailsPage.notificationBanner.content.heading).containsText("This property is missing")
                assertThat(detailsPage.notificationBanner.content.heading).containsText("licensing details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("tenancy details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("compliance certificates")
            }
        }

        @Nested
        inner class OccupiedWithBothSkippedAndCompliant {
            // Property 43: occupied, no licence, tenancy skipped, fully compliant (no compliance issue), so the
            // pure BOTH banner renders rather than COMBINED. The landlord view shows two inline links; the local
            // council view shows a single link (different copy).
            @Test
            fun `landlord view shows the both provide-later notification banner with links to licensing and tenancy`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(43)

                assertThat(detailsPage.notificationBanner.content.heading).containsText("You must finish adding")
                assertThat(detailsPage.notificationBanner.content.heading).containsText("licensing details")
                assertThat(detailsPage.notificationBanner.content.heading).containsText("tenancy details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("compliance certificates")
                assertThat(
                    page.locator(".govuk-notification-banner__heading a.govuk-notification-banner__link"),
                ).hasCount(2)
            }

            @Test
            fun `local council view shows the both provide-later notification banner with a single link`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(43)

                assertThat(detailsPage.notificationBanner.content.heading)
                    .containsText("This property is missing licensing and tenancy details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("compliance certificates")
                assertThat(
                    page.locator(".govuk-notification-banner__heading a.govuk-notification-banner__link"),
                ).hasCount(1)
            }
        }
    }
}
