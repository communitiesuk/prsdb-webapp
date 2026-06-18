package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithMutableData.NestedIntegrationTestWithMutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages.AreYouSureFormPageLandlordDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages.ConfirmationPageLandlordDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages.ReasonFormPageLandlordDeregistration

class LandlordDeregistrationJourneyTests : IntegrationTest() {
    @Nested
    inner class LandlordWithProperties :
        NestedIntegrationTestWithMutableData("data-mockuser-landlord-with-properties-and-incomplete-property.sql") {
        @Test
        fun `User with properties can navigate the whole journey`(page: Page) {
            val landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.deleteAccountButton.clickAndWait()
            val areYouSurePage = assertPageIs(page, AreYouSureFormPageLandlordDeregistration::class)
            assertThat(areYouSurePage.form.fieldsetHeading)
                .containsText("Are you sure you want to delete your account and all your properties?")
            areYouSurePage.submitWantsToProceed()

            val reasonPage = assertPageIs(page, ReasonFormPageLandlordDeregistration::class)
            assertThat(reasonPage.form.fieldsetHeading)
                .containsText("Why are you deleting your account? (optional)")
            reasonPage.form.submit()

            val confirmationPage = assertPageIs(page, ConfirmationPageLandlordDeregistration::class)
            assertThat(confirmationPage.confirmationBanner).containsText("Account deleted")
            assertTrue(
                confirmationPage.page
                    .content()
                    .contains(
                        "You have deleted your landlord information and all your properties. We have sent you an email confirming this.",
                    ),
            )

            // Check they can no longer access the landlord dashboard
            val landlordDashboard = navigator.goToLandlordDashboard()
            assertTrue(landlordDashboard.page.content().contains("You do not have permission to access this page"))
        }
    }

    @Nested
    inner class LandlordWithoutProperties : NestedIntegrationTestWithMutableData("data-unverified-landlord.sql") {
        @Test
        fun `User with no properties can navigate the whole journey`(page: Page) {
            val landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.deleteAccountButton.clickAndWait()
            val areYouSurePage = assertPageIs(page, AreYouSureFormPageLandlordDeregistration::class)
            assertThat(areYouSurePage.form.fieldsetHeading).containsText("Are you sure you want to delete your account?")
            areYouSurePage.submitWantsToProceed()

            val confirmationPage = assertPageIs(page, ConfirmationPageLandlordDeregistration::class)
            assertThat(confirmationPage.confirmationBanner).containsText("Account deleted")
            assertTrue(confirmationPage.page.content().contains("We have sent you a confirmation email."))

            // Check they can no longer access the landlord dashboard
            val landlordDashboard = navigator.goToLandlordDashboard()
            assertTrue(landlordDashboard.page.content().contains("You do not have permission to access this page"))
        }
    }

    @Nested
    inner class LandlordWithJointLandlordInvitation :
        NestedIntegrationTestWithMutableData("data-mockuser-landlord-with-joint-landlord-invitation.sql") {
        @Test
        fun `deregistering a joint landlord preserves the jointly-owned property and its pending invitation`(
            page: Page,
            @Autowired jdbcTemplate: JdbcTemplate,
        ) {
            // Count database entities
            val originalPropertyCount =
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM property_ownership",
                    Int::class.java,
                )
            val originalInvitationCount =
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM joint_landlord_invitation",
                    Int::class.java,
                )
            val originalLandlordshipCount =
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM ownership_link",
                    Int::class.java,
                )

            // Deregister landlord
            val landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.deleteAccountButton.clickAndWait()
            val areYouSurePage = assertPageIs(page, AreYouSureFormPageLandlordDeregistration::class)
            areYouSurePage.submitWantsToProceed()

            val reasonPage = assertPageIs(page, ReasonFormPageLandlordDeregistration::class)
            reasonPage.form.submit()

            val confirmationPage = assertPageIs(page, ConfirmationPageLandlordDeregistration::class)
            assertThat(confirmationPage.confirmationBanner).containsText("Account deleted")

            // Re-count database entities
            val propertyCount =
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM property_ownership",
                    Int::class.java,
                )
            val invitationCount =
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM joint_landlord_invitation",
                    Int::class.java,
                )
            val landlordshipCount =
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM ownership_link",
                    Int::class.java,
                )

            assertEquals(originalPropertyCount, propertyCount)
            assertEquals(originalInvitationCount, invitationCount)
            assertEquals(originalLandlordshipCount!!.minus(1), landlordshipCount)
        }
    }

    @Nested
    inner class LandlordWithSoleAndJointProperties :
        NestedIntegrationTestWithMutableData("data-mockuser-landlord-with-sole-and-joint-properties.sql") {
        @Test
        fun `deregistering deletes solely-owned properties but keeps jointly-owned ones`(
            page: Page,
            @Autowired jdbcTemplate: JdbcTemplate,
        ) {
            val landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.deleteAccountButton.clickAndWait()
            val areYouSurePage = assertPageIs(page, AreYouSureFormPageLandlordDeregistration::class)
            areYouSurePage.submitWantsToProceed()
            val reasonPage = assertPageIs(page, ReasonFormPageLandlordDeregistration::class)
            reasonPage.form.submit()
            assertPageIs(page, ConfirmationPageLandlordDeregistration::class)

            val soleCount =
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM property_ownership WHERE id = 1",
                    Int::class.java,
                )
            val jointCount =
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM property_ownership WHERE id = 2",
                    Int::class.java,
                )
            val coOwnerMembership =
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM ownership_link WHERE landlordship_id = 2 AND landlord_id = 2",
                    Int::class.java,
                )

            assertEquals(0, soleCount)
            assertEquals(1, jointCount)
            assertEquals(1, coOwnerMembership)
        }
    }
}
