package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.ConfirmYouAreALandlordForThisPropertyPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.InvitationRejectedConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.PropertyJoinedConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ConfirmIdentityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PrivacyNoticePageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
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
        val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
        assertThat(page.locator("main")).containsText("Original Landlord")
        assertThat(page.locator("main")).containsText("2 Fake Way")
        acceptOrRejectPage.acceptInvitation()

        // TODO PDJB-260 - if the user is already logged in as a registered landlord this should work.
        //  If not logged in, they should log in then get to the confirmation page
        //  If not a landlord, they need to register before they reach the confirmation page

        val confirmYouAreALandlordForThisPropertyPage = assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)
        confirmYouAreALandlordForThisPropertyPage.form.submit()

        assertPageIs(page, PropertyJoinedConfirmationPage::class)
    }

    @Test
    fun `User with a valid token can reject the invitation and reach a confirmation page`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
        // TODO PDJB-260 - add tests for the invite being rejected
        //  Add tests checking that unauthenticated users are asked to log in / register before reaching the confirmation page
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

            val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
            assertThat(page.locator("main")).containsText("Original Landlord")
            assertThat(page.locator("main")).containsText("2 Fake Way")
            acceptOrRejectPage.acceptInvitation()

            val privacyNoticePage = assertPageIs(page, PrivacyNoticePageLandlordRegistration::class)
            privacyNoticePage.agreeAndSubmit()

            val confirmIdentityPage = assertPageIs(page, ConfirmIdentityFormPageLandlordRegistration::class)
            confirmIdentityPage.confirm()

            val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
            emailPage.submitEmail("test@example.com")

            val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
            phoneNumPage.submitPhoneNumber("07123456789")

            val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
            countryOfResidencePage.submitUk()

            val lookupAddressPage = assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

            val selectAddressPage = assertPageIs(page, SelectAddressFormPageLandlordRegistration::class)
            selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

            val checkAnswersPage = assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
            checkAnswersPage.confirmAndSubmit()

            val confirmYouAreALandlordForThisPropertyPage = assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)
            confirmYouAreALandlordForThisPropertyPage.form.submit()

            assertPageIs(page, PropertyJoinedConfirmationPage::class)
        }

        @Test
        fun `User with a valid token can reject the invitation and reach a confirmation page without registering as a landlord`(
            page: Page,
        ) {
            val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
            acceptOrRejectPage.rejectInvitation()
            assertPageIs(page, InvitationRejectedConfirmationPage::class)
        }
    }
}
