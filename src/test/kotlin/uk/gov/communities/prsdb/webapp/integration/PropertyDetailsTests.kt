package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
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
    inner class FreeholderPropertyRecord : IntegrationTestWithMutableData.NestedIntegrationTestWithMutableData("data-local.sql") {
        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @ParameterizedTest
        @CsvSource(
            "NONE, false",
            "LEASEHOLD_ONLY, false",
            "JOINT_TENANTS, true",
            "TENANTS_IN_COMMON, true",
            "SHARE_OF_JOINT_TENANTS, true",
            "SHARE_OF_TENANTS_IN_COMMON, true",
        )
        fun `the property record displays the appropriate freeholders and their contact details`(
            ownershipType: OwnershipType,
            joint: Boolean,
        ) {
            jdbcTemplate.update("UPDATE property_ownership SET ownership_type = ? WHERE id = 20000", ownershipType.ordinal)

            val detailsPage = navigator.goToPropertyDetailsLandlordView(20000)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.freeholderSection).isVisible()
            assertThat(detailsPage.freeholderSection.locator("h3"))
                .hasText(if (joint) "Joint freeholders" else "Freeholders")
            assertThat(detailsPage.freeholderUpdateLink.locator)
                .hasText(if (joint) "Add, change or remove joint freeholders" else "Add, change or remove freeholders")
            assertThat(detailsPage.freeholderUpdateLink.locator)
                .hasAttribute("href", "#")

            assertEquals(3, detailsPage.freeholderCards.size)
            val cards = detailsPage.freeholderCards.associateBy { it.title.getText() }
            assertEquals(setOf("Jordan Joint", "Casey Joint", "Unknown freeholder"), cards.keys)
            assertThat(cards.getValue("Jordan Joint").summaryList).containsText("Email address")
            assertThat(cards.getValue("Jordan Joint").summaryList).containsText("joint.freeholder@example.com")
            assertThat(cards.getValue("Casey Joint").summaryList).hasCount(0)
            assertThat(cards.getValue("Casey Joint").bodyText).hasText(
                "A postal address was provided for this freeholder. We’ve used this to tell them that they’re named on this registration.",
            )
            assertThat(cards.getValue("Casey Joint").bodyText.locator("strong")).hasText("postal address")
            assertEquals(0, cards.getValue("Jordan Joint").bodyText.count())
            val unknownFreeholderCard = cards.getValue("Unknown freeholder")
            assertThat(unknownFreeholderCard.bodyText).hasText(
                "You told us there was another freeholder, but you cannot currently provide their details. " +
                    "Return and add these details when you can.",
            )
            assertEquals(0, unknownFreeholderCard.bodyText.locator("strong").count())
            cards.values.forEach { assertEquals(0, it.actionLinks.count()) }
        }

        @ParameterizedTest
        @EnumSource(
            value = OwnershipType::class,
            names = ["SOLE_FREEHOLD", "COMMONHOLD", "SHARE_OF_SOLE_FREEHOLD", "SHARE_OF_COMMONHOLD"],
        )
        fun `the property record hides freeholders for sole ownership and commonhold`(ownershipType: OwnershipType) {
            jdbcTemplate.update("UPDATE property_ownership SET ownership_type = ? WHERE id = 20000", ownershipType.ordinal)

            val detailsPage = navigator.goToPropertyDetailsLandlordView(20000)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.freeholderSection).hasCount(0)
        }
    }

    @Nested
    inner class ExpiredEicPropertyRecord :
        NestedIntegrationTestWithImmutableData(
            listOf("data-mockuser-landlord-with-properties.sql", "data-overlay-expired-eic-property-record.sql"),
        ) {
        @Test
        fun `property record shows banner for an expired EIC`() {
            val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(2)

            assertThat(
                propertyDetailsPage.notificationBanner,
            ).containsText("This property’s electrical safety certificate has expired.")
        }
    }

    @Nested
    inner class PropertyDetailsLandlordView {
        @Test
        fun `the people responsible tab shows the saved freeholder as a read only card`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.freeholderSection).containsText("Freeholders")
            val freeholderCard = detailsPage.freeholderCards.single()
            assertEquals("Unknown freeholder", freeholderCard.title.getText())
            assertThat(freeholderCard.bodyText).containsText("You told us there was another freeholder")
            assertThat(detailsPage.freeholderUpdateLink.locator).hasAttribute(
                "href",
                "#",
            )
            assertEquals(0, freeholderCard.actionLinks.count())
        }

        @Test
        fun `the property details page loads with the property details tab selected by default`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)

            assertEquals("property-details", detailsPage.tabs.activeTabPanelId)
        }

        @Test
        fun `loading the landlord details page and clicking landlord details tab shows the landlords details tab`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToPeopleResponsible()

            assertEquals("people-responsible", detailsPage.tabs.activeTabPanelId)
        }

        @Test
        fun `loading the landlord details page and clicking compliance information tab shows the compliance information tab`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToComplianceInformation()

            assertEquals(COMPLIANCE_INFO_FRAGMENT, detailsPage.tabs.activeTabPanelId)
        }

        @Test
        fun `when the landlord details tab is active clicking the property details tab shows property details tab`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToPeopleResponsible()

            detailsPage.tabs.goToPropertyDetails()

            assertEquals("property-details", detailsPage.tabs.activeTabPanelId)
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
        fun `individual property shows invite text link and not invite button on landlord tab`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.inviteJointLandlordIndividualText).isVisible()
            assertThat(detailsPage.inviteJointLandlordLink.locator).isVisible()
            assertThat(detailsPage.inviteJointLandlordButton.locator).isHidden()
        }

        @Test
        fun `joint property with multiple landlords shows invite button on landlord tab`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(8)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.inviteJointLandlordButton.locator).isVisible()
            assertThat(detailsPage.inviteJointLandlordIndividualText).isHidden()
            assertThat(detailsPage.markAsSingleLandlordInsetText).isHidden()
        }

        @Test
        fun `joint property with sole landlord shows mark as single landlord inset text on landlord tab and invite button`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(13)
            detailsPage.tabs.goToPeopleResponsible()

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
            fun `is visible and includes correct messages when all certs are missing`() {
                val propertyOwnershipId = 8
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(
                    detailsPage.notificationBanner,
                ).containsText("You must finish adding this property’s compliance certificates")
            }

            @Test
            fun `is visible and includes correct messages when all certs are expired`() {
                val propertyOwnershipId = 9
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("Multiple compliance certificates for this property have expired")
            }

            @Test
            fun `is visible and includes correct message when epc has a low rating and mees exemption is missing`() {
                val propertyOwnershipId = 10
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(
                    detailsPage.notificationBanner,
                ).containsText("You must finish adding this property’s compliance certificates")
            }

            @Test
            fun `is not visible when all certs are compliant`() {
                val propertyOwnershipId = 11
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }

            @Test
            fun `includes a link to the compliance information tab`() {
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
            fun `notification banner is visible when certs are expired`() {
                // Property 9: unoccupied, gas expired, EPC expired
                val detailsPage = navigator.goToPropertyDetailsLandlordView(9)
                detailsPage.tabs.goToComplianceInformation()

                assertThat(detailsPage.notificationBanner).isVisible()
            }

            @Test
            fun `gas safety card has certificate status row`() {
                // Property 37: has gas cert, electrical cert, and EPC
                val detailsPage = navigator.goToPropertyDetailsLandlordView(37)
                detailsPage.tabs.goToComplianceInformation()

                assertThat(detailsPage.gasSafetyCard.summaryList.certificateStatusRow).isVisible()
            }

            @Test
            fun `electrical safety card has certificate status row`() {
                // Property 37: has gas cert, electrical cert, and EPC
                val detailsPage = navigator.goToPropertyDetailsLandlordView(37)
                detailsPage.tabs.goToComplianceInformation()

                assertThat(detailsPage.electricalSafetyCard.summaryList.certificateStatusRow).isVisible()
            }

            @Test
            fun `epc card has certificate status row`() {
                // Property 9: has expired EPC
                val detailsPage = navigator.goToPropertyDetailsLandlordView(9)
                detailsPage.tabs.goToComplianceInformation()

                assertThat(detailsPage.epcCard.summaryList.certificateStatusRow).isVisible()
            }

            @Test
            fun `gas card shows a single provide-later row and hides the cert row`() {
                // Property 39: occupied, gas cert "provide later" (with deadline)
                val detailsPage = navigator.goToPropertyDetailsLandlordView(39)
                detailsPage.tabs.goToComplianceInformation()

                assertThat(detailsPage.gasSafetyCard.summaryList.hasGasSupplyRow.value).containsText("Provide this later")
                assertThat(detailsPage.gasSafetyCard.summaryList.hasCertRow).isHidden()
            }
        }

        @Nested
        inner class LandlordDetails {
            @Test
            fun `when joint landlords flag is enabled the landlord tab shows summary cards`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
                detailsPage.tabs.goToPeopleResponsible()

                val firstCard = detailsPage.landlordSummaryCards.first()
                assertThat(firstCard.summaryList.emailAddressRow.value).containsText("alex.surname@example.com")
                assertThat(firstCard.summaryList.registrationNumberRow.value).containsText("L-CKSQ-3SX9")
            }

            @Test
            fun `the ownership answers are shown, omitting the rows for questions that were not asked`() {
                // Property 1 is seeded with OwnershipType.LEASEHOLD_ONLY, so freeholder type is not asked
                val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
                detailsPage.tabs.goToPeopleResponsible()

                assertThat(detailsPage.peopleResponsibleOwnershipSummaryList.ownsPropertyRow.value).containsText("Yes")
                assertThat(detailsPage.peopleResponsibleOwnershipSummaryList.ownershipTypeRow.value).containsText("Leasehold")
                assertThat(detailsPage.peopleResponsibleOwnershipSummaryList.freeholderTypeRow).not().isVisible()
                // TODO PDJB-1992: assert the real update route once the ownership update journey exists.
                assertThat(detailsPage.peopleResponsibleOwnershipSummaryList.ownsPropertyRow.actions.firstActionLink.locator)
                    .hasAttribute("href", "#")
            }

            @Test
            fun `the ownership answers include the freeholder type for a freehold property`() {
                // Property 37 is seeded with OwnershipType.SOLE_FREEHOLD, so all three rows are shown
                val detailsPage = navigator.goToPropertyDetailsLandlordView(37)
                detailsPage.tabs.goToPeopleResponsible()

                assertThat(detailsPage.peopleResponsibleOwnershipSummaryList.ownsPropertyRow.value).containsText("Yes")
                assertThat(detailsPage.peopleResponsibleOwnershipSummaryList.ownershipTypeRow.value).containsText("Freehold")
                assertThat(detailsPage.peopleResponsibleOwnershipSummaryList.freeholderTypeRow.value).containsText("Sole ownership")
            }

            @Test
            fun `multiple landlord cards are displayed with logged in user first then alphabetically`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(8)
                detailsPage.tabs.goToPeopleResponsible()

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

            @Test
            fun `joint property with org landlord shows org landlord card with LRN and email`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(48)
                detailsPage.tabs.goToPeopleResponsible()

                assertEquals(2, detailsPage.landlordSummaryCards.size)
                val currentUserCard = detailsPage.landlordSummaryCards[0]
                assertEquals("Alexander Smith (you)", currentUserCard.title.getText())

                val orgCard = detailsPage.landlordSummaryCards[1]
                assertEquals("Local Organisation Landlord", orgCard.title.getText())
                assertThat(orgCard.summaryList.emailAddressRow.value).containsText("local-org-landlord@example.com")
                assertThat(orgCard.summaryList.registrationNumberRow.value).not().isEmpty()
            }
        }

        @Nested
        inner class LettingAgentPanel {
            @Test
            fun `occupied property without letting agent shows delegate copy`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(4)

                assertThat(detailsPage.lettingAgentPanel).containsText(
                    "They can provide this property’s licensing, compliance certificates and tenancy details",
                )
                assertThat(detailsPage.delegateToLettingAgentLink).isVisible()
            }

            @Test
            fun `occupied property with letting agent shows cancel delegation copy`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(1)

                assertThat(detailsPage.lettingAgentPanel).containsText(
                    "They can keep this property’s licensing, compliance certificates and tenancy details up to date",
                )
                assertThat(detailsPage.removeLettingAgentLink).isVisible()
            }

            @Test
            fun `unoccupied property shows unoccupied letting agent copy`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(7)

                assertThat(detailsPage.lettingAgentPanel).containsText(
                    "Once your property’s occupied, they can keep details updated for you",
                )
                assertThat(detailsPage.delegateToLettingAgentLink).not().isVisible()
            }
        }
    }

    @Nested
    inner class PropertyDetailsLocalCouncilView {
        @Test
        fun `the people responsible tab does not show saved freeholders`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.freeholderSection).isHidden()
        }

        @Test
        fun `the property details page loads with the property details tab selected by default`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)

            assertEquals("property-details", detailsPage.tabs.activeTabPanelId)
        }

        @Test
        fun `loading the landlord details page and clicking landlord details tab shows the landlords details tab`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.tabs.goToPeopleResponsible()

            assertEquals("people-responsible", detailsPage.tabs.activeTabPanelId)
        }

        @Test
        fun `loading the landlord details page and clicking compliance information tab shows the compliance information tab`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.tabs.goToComplianceInformation()

            assertEquals(COMPLIANCE_INFO_FRAGMENT, detailsPage.tabs.activeTabPanelId)
        }

        @Nested
        inner class ComplianceTab {
            @Test
            fun `gas card shows a single provide-later row and hides the cert row`() {
                // Property 39: occupied, gas cert "provide later" (with deadline)
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(39)
                detailsPage.tabs.goToComplianceInformation()

                assertThat(detailsPage.gasSafetyCard.summaryList.hasGasSupplyRow.value).containsText("Provide this later")
                assertThat(detailsPage.gasSafetyCard.summaryList.hasCertRow).isHidden()
            }
        }

        @Test
        fun `when the landlord details tab is active clicking the property details tab shows property details tab`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.tabs.goToPeopleResponsible()

            detailsPage.tabs.goToPropertyDetails()

            assertEquals("property-details", detailsPage.tabs.activeTabPanelId)
        }

        @Test
        fun `the back link returns to the dashboard`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)
            detailsPage.backLink.clickAndWait()
            assertPageIs(page, LocalCouncilDashboardPage::class)
        }

        @Test
        fun `the property record has no change links`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)

            assertThat(detailsPage.propertyDetailsSummaryList.actionLinks).hasCount(0)
        }

        @Nested
        inner class LandlordDetails {
            @Test
            fun `when joint landlords flag is enabled the landlord tab shows summary cards sorted alphabetically`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(8)
                detailsPage.tabs.goToPeopleResponsible()

                assertEquals(3, detailsPage.landlordSummaryCards.size)
                assertEquals("Alexander Smith", detailsPage.landlordSummaryCards[0].title.getText())
                assertEquals("Alexandra Davies", detailsPage.landlordSummaryCards[1].title.getText())
                assertEquals("Tobias Evans", detailsPage.landlordSummaryCards[2].title.getText())
            }

            @Test
            fun `when joint landlords flag is enabled the landlord cards contain LRN, email, phone, and address`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(8)
                detailsPage.tabs.goToPeopleResponsible()

                val firstCard = detailsPage.landlordSummaryCards[0]
                assertThat(firstCard.summaryList.registrationNumberRow.value).not().isEmpty()
                assertThat(firstCard.summaryList.emailAddressRow.value).containsText("alex.surname@example.com")
                assertThat(firstCard.summaryList.contactNumberRow.value).containsText("7111111111")
                assertThat(firstCard.summaryList.contactAddressRow.value).containsText("FA1 1AA")
            }

            @Test
            fun `when joint landlords flag is enabled the landlord cards have a view landlord record action`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(8)
                detailsPage.tabs.goToPeopleResponsible()

                val firstCard = detailsPage.landlordSummaryCards[0]
                val actionLink = firstCard.getAction("View landlord record").link
                assertThat(actionLink).hasAttribute(
                    "href",
                    Pattern.compile("/local-council/landlord-details/1.*"),
                )
                assertThat(actionLink).hasAttribute("target", "_blank")
                assertThat(actionLink).hasAttribute("rel", "noreferrer noopener")
            }

            @Test
            fun `an org landlord is shown as a card with name, LRN, email and a view landlord record link`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(48)
                detailsPage.tabs.goToPeopleResponsible()

                val orgCard =
                    detailsPage.landlordSummaryCards.single { it.title.getText() == "Local Organisation Landlord" }
                assertThat(orgCard.summaryList.registrationNumberRow.value).not().isEmpty()
                assertThat(orgCard.summaryList.emailAddressRow.value).containsText("local-org-landlord@example.com")
                assertThat(orgCard.getAction("View landlord record").link).isVisible()
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
            fun `is visible and includes correct messages when all certs are missing`() {
                val propertyOwnershipId = 8
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("This property is missing compliance certificates")
            }

            @Test
            fun `is visible and includes correct messages when all certs are expired`() {
                val propertyOwnershipId = 9
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("Multiple compliance certificates for this property have expired")
            }

            @Test
            fun `is visible and includes correct message when epc has a low rating and mees exemption is missing`() {
                val propertyOwnershipId = 10
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner).containsText("This property is missing compliance certificates")
            }

            @Test
            fun `is not visible when all certs are compliant`() {
                val propertyOwnershipId = 11
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }
        }
    }

    @Nested
    inner class PropertyDetailsInvitations : NestedIntegrationTestWithImmutableData("data-joint-landlord-invitation.sql") {
        @Test
        fun `property details page shows pending invitations section with correct email`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.pendingInvitationsDetails).isVisible()
            assertThat(detailsPage.pendingInvitationsDetails).containsText("Pending invitations (1)")
            assertThat(detailsPage.pendingInvitationsDetails).containsText("pending@example.com")
        }

        @Test
        fun `property details page shows expired invitations section with correct email`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.expiredInvitationsDetails).isVisible()
            assertThat(detailsPage.expiredInvitationsDetails).containsText("Expired invitations (1)")
            assertThat(detailsPage.expiredInvitationsDetails).containsText("expired@example.com")
        }

        @Test
        fun `pending invitation shows expiry and sent date details`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.pendingInvitationsDetails).containsText("Expires in")
            assertThat(detailsPage.pendingInvitationsDetails).containsText("Sent on")
        }

        @Test
        fun `expired invitation shows expired date`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(2)
            detailsPage.tabs.goToPeopleResponsible()

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
            detailsPage.tabs.goToPeopleResponsible()

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
            detailsPage.tabs.goToPeopleResponsible()

            detailsPage.expiredInvitationsDetails.locator("summary").click()
            detailsPage.expiredInvitationsDetails.getByText("Send a new invitation email").click()
            page.waitForLoadState()

            val successBanner = page.locator(".govuk-notification-banner--success")
            assertThat(successBanner).isVisible()
            assertThat(successBanner).containsText("expired@example.com")
        }

        @Test
        fun `letting agent panel is not shown on local council view`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)

            assertThat(detailsPage.lettingAgentPanel).not().isVisible()
        }
    }

    @Nested
    // TODO PDJB-2016: delete this class along with the ASSOCIATED_PARTIES flag. PDJB-1999 moves the
    //  local council view onto the people responsible tab, after which no view shows the legacy row.
    inner class OwnershipBeforePdjb955 {
        @Test
        fun `local council view keeps the ownership row on the property record`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(1)

            assertThat(detailsPage.propertyDetailsSummaryList.ownershipTypeRow.value).isVisible()
        }
    }

    @Nested
    inner class PropertyDetailsTab {
        @Test
        fun `landlord view groups the property record into sections`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)

            assertThat(detailsPage.sectionHeading("Property details")).isVisible()
            assertThat(detailsPage.sectionHeading("Who the council should contact")).isVisible()
            assertThat(detailsPage.sectionHeading("Occupied by tenants")).isVisible()
            assertThat(detailsPage.sectionHeading("Property licensing")).isVisible()
            assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).isVisible()
            // The ownership section is shown on the people responsible tab instead
            // TODO PDJB-2016: remove these two assertions with the flag, once the legacy row is gone
            assertThat(detailsPage.sectionHeading("Ownership")).isHidden()
            assertThat(detailsPage.propertyDetailsSummaryList.ownershipTypeRow).isHidden()
        }

        @Test
        fun `landlord view displays the custom property type when set`() {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(37)

            assertThat(detailsPage.propertyDetailsSummaryList.propertyTypeRow).containsText("End terrace")
        }

        @Test
        fun `local council view displays the custom property type when set`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(37)

            assertThat(detailsPage.propertyDetailsSummaryList.propertyTypeRow).containsText("End terrace")
        }

        @Test
        fun `landlord view hides the tenancy section for an unoccupied property`() {
            // Property 12: unoccupied, no licence, tenancy details not applicable.
            val detailsPage = navigator.goToPropertyDetailsLandlordView(12)

            assertThat(detailsPage.sectionHeading("Occupied by tenants")).isVisible()
            assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
            assertThat(detailsPage.sectionHeading("Tenancy details")).isHidden()
        }

        @Nested
        inner class OccupiedWithLicensingAndTenancySkipped {
            // Property 39: occupied (0 tenants, 0 households), no licence, no tenancy details.
            // created_date and last_occupied_date are both seeded to 7 days ago (occupied at registration),
            // so the provide-later deadline is (7 days ago + PROVIDE_LATER_DEADLINE_DAYS) days from today.
            private val expectedDeadline =
                LocalDate
                    .now()
                    .minusDays(7)
                    .plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK))

            @Test
            fun `landlord view shows provide later rows with the deadline date for both licensing and tenancy`() {
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
            fun `local council view shows provide later paragraphs with the deadline date for both licensing and tenancy`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(39)

                assertThat(
                    detailsPage.bodyParagraph("The landlords must provide these details before $expectedDeadline"),
                ).hasCount(2)
            }
        }

        @Nested
        inner class OccupiedAfterRegistrationWithLicensingAndTenancySkipped {
            // Property 43: occupied with licensing and tenancy skipped, but created_date (05/02/25) is earlier
            // than last_occupied_date (7 days ago), i.e. it became occupied after registration. Such properties
            // show a provide-later prompt with no deadline date.
            @Test
            fun `landlord view shows no-deadline provide later rows for both licensing and tenancy`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(43)

                assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("Yes")
                assertThat(detailsPage.sectionHeading("Tenancy details")).isVisible()
                assertThat(
                    detailsPage.propertyDetailsSummaryList.licensingRow.value,
                ).containsText("Provide this later (within 28 days of the property being occupied)")
                assertThat(
                    detailsPage.propertyDetailsSummaryList.tenancyRow.value,
                ).containsText("Provide this later (within 28 days of the property being occupied)")
            }

            @Test
            fun `local council view shows not provided paragraphs for both licensing and tenancy`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(43)

                assertThat(
                    detailsPage.bodyParagraph("These details have not been provided yet"),
                ).hasCount(2)
            }
        }

        @Nested
        inner class UnoccupiedWithLicensingSkipped {
            // Property 9: unoccupied (0 tenants), no licence.
            @Test
            fun `landlord view shows a provide later licensing row and hides the tenancy section`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(9)

                assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
                assertThat(detailsPage.propertyDetailsSummaryList.licensingRow.value)
                    .containsText("Provide this later (within 28 days of the property being occupied)")
                assertThat(detailsPage.sectionHeading("Tenancy details")).isHidden()
            }

            @Test
            fun `local council view shows a not provided licensing paragraph and hides the tenancy section`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(9)

                assertThat(detailsPage.sectionHeading("Tenancy details")).isHidden()
                assertThat(
                    detailsPage.bodyParagraph("These details have not been provided yet"),
                ).hasCount(1)
            }
        }

        @Nested
        inner class OccupiedWithAllFieldsCompleted {
            // Property 40: occupied (2 tenants, 1 household), licence present, full tenancy details.
            @Test
            fun `landlord view shows the licensing type and the tenancy details`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(40)

                assertThat(detailsPage.sectionHeading("Tenancy details")).isVisible()
                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText("1")
            }

            @Test
            fun `local council view shows the licensing type and the tenancy details`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(40)

                assertThat(detailsPage.sectionHeading("Tenancy details")).isVisible()
                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText("1")
                assertThat(
                    detailsPage.bodyParagraph("These details have not been provided yet"),
                ).hasCount(0)
            }
        }

        @Nested
        inner class UnoccupiedWithAllFieldsCompleted {
            // Property 7: unoccupied (0 tenants), licence present.
            @Test
            fun `landlord view shows the licensing type and hides the tenancy section`() {
                val detailsPage = navigator.goToPropertyDetailsLandlordView(7)

                assertThat(detailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(detailsPage.sectionHeading("Tenancy details")).isHidden()
            }

            @Test
            fun `local council view shows the licensing type and hides the tenancy section`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(7)

                assertThat(detailsPage.propertyDetailsSummaryList.licensingTypeRow.value).isVisible()
                assertThat(detailsPage.sectionHeading("Tenancy details")).isHidden()
                assertThat(
                    detailsPage.bodyParagraph("These details have not been provided yet"),
                ).hasCount(0)
            }
        }

        @Nested
        inner class ProvideLaterNotificationBanner {
            @Test
            fun `landlord view shows the tenancy-only provide-later banner`() {
                // Property 41: occupied, licence present, tenancy skipped, fully compliant.
                val detailsPage = navigator.goToPropertyDetailsLandlordView(41)

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.content.heading)
                    .containsText("You must finish adding this property’s tenancy details. ")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("licensing")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("certificate")
            }

            @Test
            fun `local council view shows the tenancy-only provide-later banner`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(41)

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.content.heading).containsText("This property is missing tenancy details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("licensing")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("certificate")
            }

            @Test
            fun `landlord view shows the licensing-only provide-later banner`() {
                // Property 42: occupied, no licence, tenancy provided, fully compliant.
                val detailsPage = navigator.goToPropertyDetailsLandlordView(42)

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.content.heading)
                    .containsText("You must finish adding this property’s licensing details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("tenancy")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("certificate")
            }

            @Test
            fun `local council view shows the licensing-only provide-later banner`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(42)

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.content.heading).containsText("This property is missing licensing details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("tenancy")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("certificate")
            }

            @Test
            fun `landlord view shows the both provide-later banner with links to licensing and tenancy`(page: Page) {
                // Property 43: occupied, no licence, tenancy skipped, fully compliant.
                val detailsPage = navigator.goToPropertyDetailsLandlordView(43)

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.content.heading)
                    .containsText("You must finish adding this property’s licensing details and tenancy details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("certificate")
                assertThat(
                    page.locator(".govuk-notification-banner__heading a.govuk-notification-banner__link"),
                ).hasCount(2)
            }

            @Test
            fun `local council view shows the both provide-later banner with a single link`(page: Page) {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(43)

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.content.heading)
                    .containsText("This property is missing licensing and tenancy details")
                assertThat(detailsPage.notificationBanner.content.heading).not().containsText("certificate")
                assertThat(
                    page.locator(".govuk-notification-banner__heading a.govuk-notification-banner__link"),
                ).hasCount(1)
            }

            @Test
            fun `landlord view shows the combined provide-later and compliance banner`() {
                // Property 39: occupied, no licence, no tenancy details, has a compliance issue.
                val detailsPage = navigator.goToPropertyDetailsLandlordView(39)

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.content.heading)
                    .containsText("You must finish providing property and tenancy details and valid compliance certificates")
            }

            @Test
            fun `local council view shows the combined provide-later and compliance banner`() {
                val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(39)

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.content.heading)
                    .containsText("This registration is missing property and tenancy details and valid compliance certificates")
            }

            @Test
            fun `no provide-later banner is shown for an unoccupied property with licensing skipped`(page: Page) {
                // Property 9: unoccupied, no licence.
                navigator.goToPropertyDetailsLandlordView(9)

                assertThat(page.getByText("You must finish adding")).isHidden()
                assertThat(page.getByText("You must finish providing")).isHidden()
            }

            @Test
            fun `no notification banner is shown when all fields are completed and the property is compliant`() {
                // Property 40: occupied, licence present, full tenancy details, fully compliant.
                val detailsPage = navigator.goToPropertyDetailsLandlordView(40)

                assertThat(detailsPage.notificationBanner).isHidden()
            }
        }
    }

    @Nested
    inner class PropertyDetailsLocalCouncilInvitations :
        NestedIntegrationTestWithImmutableData("data-joint-landlord-invitation.sql") {
        @Test
        fun `local council view shows pending invitations section with correct email`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(2)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.pendingInvitationsDetails).isVisible()
            assertThat(detailsPage.pendingInvitationsDetails).containsText("Pending invitations (1)")
            assertThat(detailsPage.pendingInvitationsDetails).containsText("pending@example.com")
        }

        @Test
        fun `local council view shows expired invitations section with correct email`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(2)
            detailsPage.tabs.goToPeopleResponsible()

            assertThat(detailsPage.expiredInvitationsDetails).isVisible()
            assertThat(detailsPage.expiredInvitationsDetails).containsText("Expired invitations (1)")
            assertThat(detailsPage.expiredInvitationsDetails).containsText("expired@example.com")
        }

        @Test
        fun `local council view does not show action links on pending invitations`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(2)
            detailsPage.tabs.goToPeopleResponsible()

            detailsPage.pendingInvitationsDetails.locator("summary").click()
            assertThat(detailsPage.pendingInvitationsDetails.locator(".prsdb-link-group-list")).isHidden()
        }

        @Test
        fun `local council view does not show action links on expired invitations`() {
            val detailsPage = navigator.goToPropertyDetailsLocalCouncilView(2)
            detailsPage.tabs.goToPeopleResponsible()

            detailsPage.expiredInvitationsDetails.locator("summary").click()
            assertThat(detailsPage.expiredInvitationsDetails.locator(".prsdb-link-group-list")).isHidden()
        }
    }
}

@WithOrgLandlordProfile
class PropertyDetailsOrgLandlordTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `property solely owned by org landlord shows your organisation in card title`() {
        val detailsPage = navigator.goToPropertyDetailsLandlordView(47)
        detailsPage.tabs.goToPeopleResponsible()

        assertEquals(1, detailsPage.landlordSummaryCards.size)
        val orgCard = detailsPage.landlordSummaryCards[0]
        assertEquals("Local Organisation Landlord (your organisation)", orgCard.title.getText())
    }

    @Test
    fun `joint property shows your organisation for org landlord and not for other landlord`() {
        val detailsPage = navigator.goToPropertyDetailsLandlordView(48)
        detailsPage.tabs.goToPeopleResponsible()

        assertEquals(2, detailsPage.landlordSummaryCards.size)

        val orgCard = detailsPage.landlordSummaryCards[0]
        assertEquals("Local Organisation Landlord (your organisation)", orgCard.title.getText())

        val otherCard = detailsPage.landlordSummaryCards[1]
        assertEquals("Alexander Smith", otherCard.title.getText())
    }
}
