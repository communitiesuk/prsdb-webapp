package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.GAS_CERTIFICATE_14_MONTH_EXPIRY
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_MONTHS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS_BEFORE_14_MONTHS_LATER
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.OutstandingActionViewModelBuilder.Companion.DATE_FORMATTER
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class OutstandingActionsPageTests : IntegrationTest() {
    @Nested
    inner class LandlordsWithoutOutstandingActions :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-properties.sql") {
        @Test
        fun `the page loads with heading and inset text`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            assertThat(outstandingActionsPage.heading).containsText("Outstanding actions")
            assertThat(outstandingActionsPage.hintText).isHidden()
            assertThat(
                outstandingActionsPage.insetText,
            ).containsText("You have provided all the required details for your occupied properties.")
        }
    }

    // TODO: PDJB-1926: Remove this section
    @Nested
    inner class LandlordWithThirteenMonthOldGasCert :
        NestedIntegrationTestWithImmutableData(
            listOf("data-mockuser-landlord-with-compliance-actions.sql", "data-overlay-gas-cert-issued-13-months-ago.sql"),
        ) {
        // 2 Fake Way (property ownership 1) is occupied, has a 13 month old gas cert, and is missing other certs so is always listed

        @Test
        fun `gas safety row shows expired on date when 14 month expiry is disabled`() {
            featureFlagManager.disableFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("2 Fake Way")
            val expectedDate =
                LocalDate
                    .now()
                    .minusMonths(13)
                    .plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS_BEFORE_14_MONTHS_LATER.toLong())
                    .format(DATE_FORMATTER)
            assertThat(card.summaryList.gasSafetyRow).containsText("Expired on $expectedDate")
        }

        @Test
        fun `gas safety row is hidden when 14 month expiry is enabled`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("2 Fake Way")
            assertThat(card.summaryList.gasSafetyRow).isHidden()
        }

        @Test
        fun `property details gas safety card shows the certificate as expired when 14 month expiry is disabled`() {
            featureFlagManager.disableFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)
            val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(1)
            propertyDetailsPage.tabs.goToComplianceInformation()
            val certificateStatus = propertyDetailsPage.gasSafetyCard.summaryList.certificateStatusRow.value
            PlaywrightAssertions.assertThat(certificateStatus).containsText("Expired")
        }
    }

    @Nested
    inner class LandlordsWithOutstandingActions :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-compliance-actions.sql") {
        @Test
        fun `the page loads with heading and body text`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            assertThat(outstandingActionsPage.heading).containsText("Outstanding actions")
            assertThat(outstandingActionsPage.bodyText).containsText("These occupied properties are missing details or certificates")
        }

        @Test
        fun `summary cards are populated with correct content and actions`(page: Page) {
            var outstandingActionsPage = navigator.goToOutstandingActions()
            // Check compliance card - OCCUPIED, gas missing, eicr exempt, epc expired
            val completedComplianceCard = outstandingActionsPage.findSummaryCard("4 Pretend Crescent")
            assertThat(completedComplianceCard.summaryList.gasSafetyRow).containsText("No valid gas safety certificate")
            assertThat(completedComplianceCard.summaryList.electricalSafetyRow).isHidden()
            assertThat(completedComplianceCard.summaryList.energyPerformanceRow).containsText("Expired")

            completedComplianceCard.getAction("Go to property").link.clickAndWait()
            val propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "3"))
            assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)

            // Check compliance card - UNOCCUPIED, gas missing, eicr exempt, epc expired
            outstandingActionsPage = navigator.goToOutstandingActions()
            assertThat(outstandingActionsPage.findSummaryCard("5 Invented Lane")).isHidden()

            // Check compliance card - OCCUPIED, gas valid, eicr missing, epc in date but low rating
            outstandingActionsPage = navigator.goToOutstandingActions()
            val thirdComplianceCard = outstandingActionsPage.findSummaryCard("2 Fake Way")
            assertThat(thirdComplianceCard.summaryList.gasSafetyRow).isHidden()
            assertThat(thirdComplianceCard.summaryList.electricalSafetyRow).containsText("No valid electrical safety certificate")
            assertThat(thirdComplianceCard.summaryList.energyPerformanceRow).containsText("No valid EPC")

            // Check compliance card - UNOCCUPIED, gas valid, eicr missing, epc low rating
            outstandingActionsPage = navigator.goToOutstandingActions()
            assertThat(outstandingActionsPage.findSummaryCard("3 Imaginary Street")).isHidden()
        }

        @Test
        fun `gas safety row shows provide this later message for provide later status`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("6 Fabricated Avenue")
            val expectedDate = LocalDate.now().plusDays(28).format(DATE_FORMATTER)
            assertThat(card.summaryList.gasSafetyRow).containsText("Provide this later (before $expectedDate)")
        }

        @Test
        fun `gas safety row shows expired on date for expired certificate`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("7 Mythical Drive")
            // The certificate was issued 14 months and 1 day ago
            val expectedDate =
                LocalDate
                    .now()
                    .minusMonths(GAS_SAFETY_CERT_VALIDITY_MONTHS.toLong())
                    .minusDays(1)
                    .plusMonths(GAS_SAFETY_CERT_VALIDITY_MONTHS.toLong())
                    .format(DATE_FORMATTER)
            assertThat(card.summaryList.gasSafetyRow).containsText("Expired on $expectedDate")
        }

        @Test
        fun `gas safety row shows no valid certificate for not added status`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("4 Pretend Crescent")
            assertThat(card.summaryList.gasSafetyRow).containsText("No valid gas safety certificate")
        }

        @Test
        fun `electrical safety row shows provide this later message for provide later status`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("6 Fabricated Avenue")
            val expectedDate = LocalDate.now().plusDays(28).format(DATE_FORMATTER)
            assertThat(card.summaryList.electricalSafetyRow).containsText("Provide this later (before $expectedDate)")
        }

        @Test
        fun `electrical safety row shows expired on date for expired certificate`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("7 Mythical Drive")
            val expectedDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER)
            assertThat(card.summaryList.electricalSafetyRow).containsText("Expired on $expectedDate")
        }

        @Test
        fun `electrical safety row shows no valid certificate for not added status`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("2 Fake Way")
            assertThat(card.summaryList.electricalSafetyRow).containsText("No valid electrical safety certificate")
        }

        @Nested
        inner class EpcComplianceActions :
            NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-epc-compliance-actions.sql") {
            @Test
            fun `occupied property with provide later shows provide this later message`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC Provide Later Occupied")
                val expectedDate = LocalDate.now().plusDays(28).format(DATE_FORMATTER)
                assertThat(card.summaryList.energyPerformanceRow).containsText("Provide this later (before $expectedDate)")
            }

            @Test
            fun `occupied after registration property with provide later shows undated provide this later message`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC Provide Later Occupied After Registration")
                assertThat(card.summaryList.energyPerformanceRow)
                    .containsText("Provide this later (within 28 days of the property being occupied)")
            }

            @Test
            fun `occupied property with valid high rating does not show epc row`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC Valid High Rating Occupied")
                assertThat(card.summaryList.energyPerformanceRow).isHidden()
            }

            @Test
            fun `occupied property with valid low rating and exemption does not show epc row`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC Valid Low Exempt Occupied")
                assertThat(card.summaryList.energyPerformanceRow).isHidden()
            }

            @Test
            fun `occupied property with valid low rating and no exemption shows no valid certificate`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC Valid Low No Exempt Occupied")
                assertThat(card.summaryList.energyPerformanceRow).containsText("No valid EPC")
            }

            @Test
            fun `occupied property with expired epc and tenancy before expiry and high rating shows expired on date`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC Expired Tenancy Before High Occupied")
                val expectedDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER)
                assertThat(card.summaryList.energyPerformanceRow).containsText("Expired on $expectedDate")
                assertThat(card.epcInsetText).isVisible()
            }

            @Test
            fun `occupied property with expired epc and tenancy before expiry and low rating with exemption shows expired on date`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC Expired Tenancy Before Low Exempt Occupied")
                val expectedDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER)
                assertThat(card.summaryList.energyPerformanceRow).containsText("Expired on $expectedDate")
                assertThat(card.epcInsetText).isVisible()
            }

            @Test
            fun `occupied property with expired epc and tenancy before expiry and low rating without exemption shows no valid cert`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC Expired Tenancy Before Low No Exempt Occupied")
                assertThat(card.summaryList.energyPerformanceRow).containsText("No valid EPC")
                assertThat(card.epcInsetText).isHidden()
            }

            @Test
            fun `occupied property with expired epc not in date when tenancy began shows expired`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC Expired Not In Date Occupied")
                val expectedDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER)
                assertThat(card.summaryList.energyPerformanceRow).containsText("Expired on $expectedDate")
                assertThat(card.epcInsetText).isHidden()
            }

            @Test
            fun `occupied property with no epc required shows no valid certificate`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC No EPC Required Occupied")
                assertThat(card.summaryList.energyPerformanceRow).containsText("No valid EPC")
            }

            @Test
            fun `occupied property with no epc not required does not show epc row`() {
                val outstandingActionsPage = navigator.goToOutstandingActions()
                val card = outstandingActionsPage.findSummaryCard("EPC No EPC Not Required Occupied")
                assertThat(card.summaryList.energyPerformanceRow).isHidden()
            }

            @Test
            fun `unoccupied properties are not shown`() {
                listOf(
                    "EPC Provide Later Unoccupied",
                    "EPC Valid High Rating Unoccupied",
                    "EPC Valid Low Exempt Unoccupied",
                    "EPC Valid Low No Exempt Unoccupied",
                    "EPC Expired Tenancy Before High Unoccupied",
                    "EPC Expired Tenancy Before Low Exempt Unoccupied",
                    "EPC Expired Tenancy Before Low No Exempt Unoccupied",
                    "EPC Expired Not In Date Unoccupied",
                    "EPC No EPC Required Unoccupied",
                    "EPC No EPC Not Required Unoccupied",
                ).forEach { address ->
                    assertThat(navigator.goToOutstandingActions().findSummaryCard(address)).isHidden()
                }
            }
        }
    }

    @Nested
    inner class OutstandingActionsPagination :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-many-compliance-actions.sql") {
        @Test
        fun `pagination is displayed when there are more items than the page size`(page: Page) {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            assertThat(outstandingActionsPage.pagination).isVisible()
        }
    }

    @Nested
    inner class LandlordsWithFewOutstandingActions :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-compliance-actions.sql") {
        @Test
        fun `pagination is not displayed when items fit on one page`(page: Page) {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            assertThat(outstandingActionsPage.pagination).not().isVisible()
        }
    }

    @Nested
    inner class LandlordsWithPropertyDetailsOutstandingActions :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-property-details-outstanding-actions.sql") {
        @Test
        fun `licensing and tenancy rows show the dated provide this later message when occupied since registration`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("10 Licensing Lane")
            val expectedDate = LocalDate.now().plusDays(28).format(DATE_FORMATTER)
            assertThat(card.summaryList.licensingRow).containsText("Provide this later (before $expectedDate)")
            assertThat(card.summaryList.tenancyRow).containsText("Provide this later (before $expectedDate)")
            assertThat(card.summaryList.gasSafetyRow).isHidden()
            assertThat(card.summaryList.electricalSafetyRow).isHidden()
            assertThat(card.summaryList.energyPerformanceRow).isHidden()
        }

        @Test
        fun `tenancy row shows the undated provide this later message when occupied after registration`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("11 Tenancy Terrace")
            assertThat(card.summaryList.tenancyRow)
                .containsText("Provide this later (within 28 days of the property being occupied)")
            assertThat(card.summaryList.licensingRow).isHidden()
        }

        @Test
        fun `unoccupied property with licensing provided later is not shown`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            assertThat(outstandingActionsPage.findSummaryCard("12 Vacant View")).isHidden()
        }

        @Test
        fun `property with tenancy and certificate actions shows both`() {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            val card = outstandingActionsPage.findSummaryCard("13 Mixed Mews")
            assertThat(card.summaryList.tenancyRow).containsText("Provide this later")
            assertThat(card.summaryList.gasSafetyRow).containsText("No valid gas safety certificate")
        }

        @Test
        fun `go to property links to the property details tab when licensing or tenancy details are outstanding`(page: Page) {
            val outstandingActionsPage = navigator.goToOutstandingActions()
            outstandingActionsPage
                .findSummaryCard("10 Licensing Lane")
                .getAction("Go to property")
                .link
                .clickAndWait()
            val propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "1"))
            assertEquals(PROPERTY_DETAILS_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)
        }
    }
}
