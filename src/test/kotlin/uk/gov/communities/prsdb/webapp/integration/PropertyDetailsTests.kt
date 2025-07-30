package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalAuthorityView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEicrPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEpcPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateGasSafetyPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.AreYouSureFormPagePropertyDeregistration
import kotlin.test.assertEquals

class PropertyDetailsTests : SinglePageTestWithSeedData("data-local.sql") {
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
        fun `in the key details section the landlord name link goes the landlord view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.getLandlordNameLinkFromKeyDetails("Alexander Smith").clickAndWait()

            assertPageIs(page, LandlordDetailsPage::class)
        }

        @Test
        fun `in the landlord details section the landlord name link goes the landlord view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.getLandlordLinkFromLandlordDetails("Alexander Smith").clickAndWait()

            val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

            landlordDetailsPage.backLink.clickAndWait()
            assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "1"))
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
            detailsPage.deleteButton.clickAndWait()
            assertPageIs(
                page,
                AreYouSureFormPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }

        @Nested
        inner class NotificationBanner {
            @Test
            fun `is visible and includes correct messages when all certs are missing`(page: Page) {
                val propertyOwnershipId = 8
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.title).containsText("Important")
                assertThat(
                    detailsPage.notificationBanner.content,
                ).containsText(
                    "This property is missing a gas safety certificate. Upload a certificate as soon as possible.\n" +
                        "This property is missing an Electrical Installation Condition Report (EICR)." +
                        " Upload an EICR as soon as possible.\n" +
                        "This property is missing an energy performance certificate (EPC). Add a new certificate as soon as possible.",
                )
            }

            @Test
            fun `is visible and includes correct messages when all certs are expired`(page: Page) {
                val propertyOwnershipId = 9
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.title).containsText("Important")
                assertThat(
                    detailsPage.notificationBanner.content,
                ).containsText(
                    "The gas safety certificate for this property has expired. Upload a new certificate as soon as possible.\n" +
                        "The Electrical Installation Condition Report (EICR) for this property has expired. " +
                        "Upload a new EICR as soon as possible.\n" +
                        "The energy performance certificate (EPC) for this property has expired. " +
                        "Add a new certificate as soon as possible.",
                )
            }

            @Test
            fun `is visible and includes correct message when epc has a low rating and mees exemption is missing`(page: Page) {
                val propertyOwnershipId = 10
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.title).containsText("Important")
                assertThat(
                    detailsPage.notificationBanner.content,
                ).containsText("This property’s energy performance certificate (EPC) is below E.")
                assertThat(
                    detailsPage.notificationBanner.content,
                ).containsText("You must")
                assertThat(
                    detailsPage.notificationBanner.content,
                ).containsText("add a new certificate or add a MEES exemption")
            }

            @Test
            fun `is not visible when all certs are compliant`(page: Page) {
                val propertyOwnershipId = 11
                val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }

            @Nested
            inner class UpdateLinks {
                @Test
                fun `upload a gas safety cert when missing redirects to the update gas safety cert page`(page: Page) {
                    val propertyOwnershipId = 8
                    val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
                    detailsPage.notificationBanner.updateMissingGasSafetyLink.clickAndWait()

                    assertPageIs(page, UpdateGasSafetyPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "8"))
                }

                @Test
                fun `upload a new gas safety cert when expired redirects to the update gas safety cert page`(page: Page) {
                    val propertyOwnershipId = 9
                    val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
                    detailsPage.notificationBanner.updateExpiredGasSafetyLink.clickAndWait()

                    assertPageIs(page, UpdateGasSafetyPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "9"))
                }

                @Test
                fun `upload an eicr when missing redirects to the update eicr page`(page: Page) {
                    val propertyOwnershipId = 8
                    val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
                    detailsPage.notificationBanner.updateMissingEicrLink.clickAndWait()

                    assertPageIs(page, UpdateEicrPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "8"))
                }

                @Test
                fun `upload a new eicr when expired redirects to the update eicr page`(page: Page) {
                    val propertyOwnershipId = 9
                    val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
                    detailsPage.notificationBanner.updateExpiredEicrLink.clickAndWait()

                    assertPageIs(page, UpdateEicrPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "9"))
                }

                // TODO PRSD-1313 remove @Disabled when the update epc page is implemented
                @Disabled
                @Test
                fun `add an epc when missing redirects to the update epc page`(page: Page) {
                    val propertyOwnershipId = 8
                    val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
                    detailsPage.notificationBanner.addEpcLink.clickAndWait()

                    assertPageIs(page, UpdateEpcPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "8"))
                }

                // TODO PRSD-1313 remove @Disabled when the update epc page is implemented
                @Disabled
                @Test
                fun `add an epc when expired redirects to the update epc page`(page: Page) {
                    val propertyOwnershipId = 9
                    val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
                    detailsPage.notificationBanner.addEpcLink.clickAndWait()

                    assertPageIs(page, UpdateEpcPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "9"))
                }

                // TODO PRSD-1313 remove @Disabled when the update epc page is implemented
                @Disabled
                @Test
                fun `add an epc or mees exemption when epc has low rating redirects to the update epc page`(page: Page) {
                    val propertyOwnershipId = 10
                    val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
                    detailsPage.notificationBanner.addEpcOrMeesExemptionLink.clickAndWait()

                    assertPageIs(page, UpdateEpcPagePropertyComplianceUpdate::class, mapOf("propertyOwnershipId" to "10"))
                }
            }
        }
    }

    @Nested
    inner class PropertyDetailsLocalAuthorityView {
        @Test
        fun `the property details page loads with the property details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        fun `loading the landlord details page and clicking landlord details tab shows the landlords details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "landlord-details")
        }

        @Test
        fun `loading the landlord details page and clicking compliance information tab shows the compliance information tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.tabs.goToComplianceInformation()

            assertEquals(detailsPage.tabs.activeTabPanelId, COMPLIANCE_INFO_FRAGMENT)
        }

        @Test
        fun `when the landlord details tab is active clicking the property details tab shows property details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.tabs.goToPropertyDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        fun `in the key details section the landlord name link goes the local authority view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.getLandlordNameLinkFromKeyDetails("Alexander Smith").clickAndWait()

            assertPageIs(page, LocalAuthorityViewLandlordDetailsPage::class, mapOf("id" to "1"))
        }

        @Test
        fun `in the landlord details section the landlord name link goes the local authority view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.getLandlordLinkFromLandlordDetails("Alexander Smith").clickAndWait()

            val landlordDetailsPage = assertPageIs(page, LocalAuthorityViewLandlordDetailsPage::class, mapOf("id" to "1"))

            landlordDetailsPage.backLink.clickAndWait()
            assertPageIs(
                page,
                PropertyDetailsPageLocalAuthorityView::class,
                mapOf("propertyOwnershipId" to "1"),
            )
        }

        @Test
        fun `the back link returns to the dashboard`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.backLink.clickAndWait()
            assertPageIs(page, LocalAuthorityDashboardPage::class)
        }

        @Test
        fun `loading the landlord details page shows the last time the landlords record was updated`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)

            assertThat(detailsPage.insetText).containsText("updated these details on")
        }

        @Nested
        inner class NotificationBanner {
            @Test
            fun `is visible and includes correct messages when all certs are missing`(page: Page) {
                val propertyOwnershipId = 8
                val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.title).containsText("Important")
                assertThat(
                    detailsPage.notificationBanner.content,
                ).containsText(
                    "This property is missing a gas safety certificate.\n" +
                        "This property is missing an Electrical Installation Condition Report (EICR).\n" +
                        "This property is missing an energy performance certificate (EPC).",
                )
            }

            @Test
            fun `is visible and includes correct messages when all certs are expired`(page: Page) {
                val propertyOwnershipId = 9
                val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.title).containsText("Important")
                assertThat(
                    detailsPage.notificationBanner.content,
                ).containsText(
                    "The gas safety certificate for this property has expired.\n" +
                        "The Electrical Installation Condition Report (EICR) for this property has expired.\n" +
                        "The energy performance certificate (EPC) for this property has expired.",
                )
            }

            @Test
            fun `is visible and includes correct message when epc has a low rating and mees exemption is missing`(page: Page) {
                val propertyOwnershipId = 10
                val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isVisible()
                assertThat(detailsPage.notificationBanner.title).containsText("Important")
                assertThat(
                    detailsPage.notificationBanner.content,
                ).containsText(
                    "This property’s energy performance certificate (EPC) is below E.",
                )
            }

            @Test
            fun `is not visible when all certs are compliant`(page: Page) {
                val propertyOwnershipId = 11
                val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(propertyOwnershipId.toLong())

                assertThat(detailsPage.notificationBanner).isHidden()
            }
        }
    }
}
