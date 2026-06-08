package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.CheckAnswersPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.ConfirmIdentityFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.ConfirmYouAreALandlordForThisPropertyPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.CountryOfResidenceFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.EmailFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.InvitationRejectedConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.LookupAddressFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.PhoneNumberFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.PrivacyNoticePageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.PropertyJoinedConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.SelectAddressFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import java.net.URI
import java.time.LocalDate

class AcceptOrRejectJointLandlordInvitationJourneyTests : IntegrationTestWithMutableData("data-joint-landlord-invitation.sql") {
    private val validToken = "aaaabbbb-cccc-dddd-eeee-ffff00001111"

    @BeforeEach
    fun enableJointLandlordsFlag() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
    }

    @Test
    fun `Landlord user with a valid token can accept the invitation and reach a confirmation page`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
        assertThat(page.locator("main")).containsText("Original Landlord")
        assertThat(page.locator("main")).containsText("2 Fake Way")
        acceptOrRejectPage.acceptInvitation()

        val confirmYouAreALandlordForThisPropertyPage = assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)
        assertThat(confirmYouAreALandlordForThisPropertyPage.heading).isVisible()
        assertThat(confirmYouAreALandlordForThisPropertyPage.propertyAddress).containsText("2 Fake Way")
        assertThat(confirmYouAreALandlordForThisPropertyPage.successBanner).not().isVisible()
        confirmYouAreALandlordForThisPropertyPage.form.submit()

        assertPageIs(page, PropertyJoinedConfirmationPage::class)
    }

    @Test
    fun `User with a valid token can reject the invitation and reach a confirmation page`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
        acceptOrRejectPage.rejectInvitation()
        assertPageIs(page, InvitationRejectedConfirmationPage::class)
    }

    @Nested
    inner class UserIsNotLandlord : NestedIntegrationTestWithMutableData("data-joint-landlord-invitation-mockuser-not-landlord.sql") {
        @Test
        fun `User with a valid token can accept the invitation, register as a landlord and reach a confirmation page`(page: Page) {
            val verifiedIdentity = VerifiedIdentityDataModel("name", LocalDate.now())
            whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentity)
            whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("www.prsd.gov.uk/landlord"))

            val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
            assertThat(page.locator("main")).containsText("Original Landlord")
            assertThat(page.locator("main")).containsText("2 Fake Way")
            acceptOrRejectPage.acceptInvitation()

            val privacyNoticePage = assertPageIs(page, PrivacyNoticePageAcceptJointLandlordInvitation::class)
            privacyNoticePage.agreeAndSubmit()

            val confirmIdentityPage = assertPageIs(page, ConfirmIdentityFormPageAcceptJointLandlordInvitation::class)
            confirmIdentityPage.confirm()

            val emailPage = assertPageIs(page, EmailFormPageAcceptJointLandlordInvitation::class)
            emailPage.submitEmail("test@example.com")

            val phoneNumPage = assertPageIs(page, PhoneNumberFormPageAcceptJointLandlordInvitation::class)
            phoneNumPage.submitPhoneNumber("07123456789")

            val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageAcceptJointLandlordInvitation::class)
            countryOfResidencePage.submitUk()

            val lookupAddressPage = assertPageIs(page, LookupAddressFormPageAcceptJointLandlordInvitation::class)
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

            val selectAddressPage = assertPageIs(page, SelectAddressFormPageAcceptJointLandlordInvitation::class)
            selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

            val checkAnswersPage = assertPageIs(page, CheckAnswersPageAcceptJointLandlordInvitation::class)
            checkAnswersPage.confirmAndSubmit()

            val confirmYouAreALandlordForThisPropertyPage = assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)
            assertThat(confirmYouAreALandlordForThisPropertyPage.heading).isVisible()
            assertThat(confirmYouAreALandlordForThisPropertyPage.propertyAddress).containsText("2 Fake Way")
            assertThat(confirmYouAreALandlordForThisPropertyPage.successBanner).isVisible()
            assertThat(confirmYouAreALandlordForThisPropertyPage.successBanner).containsText("L-")
            confirmYouAreALandlordForThisPropertyPage.form.submit()

            assertPageIs(page, PropertyJoinedConfirmationPage::class)
        }

        @Test
        fun `User with a valid token can reject the invitation and reach a confirmation page without registering as a landlord`(
            page: Page,
        ) {
            val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
            acceptOrRejectPage.rejectInvitation()
            assertPageIs(page, InvitationRejectedConfirmationPage::class)
        }
    }
}
