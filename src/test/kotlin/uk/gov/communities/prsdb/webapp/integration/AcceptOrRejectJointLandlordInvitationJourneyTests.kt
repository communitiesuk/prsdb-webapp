package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.AcceptOrRejectPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.CheckAnswersPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.ConfirmIdentityFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.ConfirmYouAreALandlordForThisPropertyPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.CountryOfResidenceFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.EmailFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.InvitationRejectedConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.InvitationUnavailablePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.LandlordTypeFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.LookupAddressFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgAddressFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgCompanyNumberFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgEmailFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgIsRegisteredCharityFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgIsRegisteredCompanyFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgMainContactFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgNameFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgPhoneNumberFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgSelectAddressFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.OrgTypeFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.PhoneNumberFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.PrivacyNoticePageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.PropertyJoinedConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.SelectAddressFormPageAcceptJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.LocalDate
import java.util.UUID

class AcceptOrRejectJointLandlordInvitationJourneyTests : IntegrationTestWithMutableData("data-joint-landlord-invitation.sql") {
    private val validToken = "aaaabbbb-cccc-dddd-eeee-ffff00001111"
    private val alreadyOnPropertyToken = "aaaabbbb-cccc-dddd-eeee-ffff00004444"

    @Autowired
    lateinit var jointLandlordInvitationRepository: JointLandlordInvitationRepository

    @Autowired
    lateinit var landlordRepository: LandlordRepository

    @Autowired
    lateinit var propertyOwnershipService: PropertyOwnershipService

    @Test
    fun `Landlord user with a valid token can accept the invitation and reach a confirmation page`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
        assertThat(page.locator("main")).containsText("Original Landlord")
        assertThat(page.locator("main")).containsText("2 Fake Way")
        acceptOrRejectPage.acceptInvitation()

        val confirmYouAreALandlordForThisPropertyPage = assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)
        assertThat(confirmYouAreALandlordForThisPropertyPage.propertyAddress).containsText("2 Fake Way")
        assertThat(confirmYouAreALandlordForThisPropertyPage.successBanner).not().isVisible()
        confirmYouAreALandlordForThisPropertyPage.form.submit()

        val confirmationPage = assertPageIs(page, PropertyJoinedConfirmationPage::class)
        assertThat(confirmationPage.confirmationBanner.body).containsText("2 Fake Way")
        assertThat(confirmationPage.surveyLink.locator).isVisible()
    }

    @Test
    fun `User with a valid token can reject the invitation and reach a confirmation page`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
        acceptOrRejectPage.rejectInvitation()
        assertPageIs(page, InvitationRejectedConfirmationPage::class)
    }

    @Test
    fun `Submitting confirm page with an invalid token redirects to invitation unavailable page`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
        acceptOrRejectPage.acceptInvitation()

        val confirmPage = assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)

        // Invalidate the invitation by deleting it from the database
        val invitation = jointLandlordInvitationRepository.findByToken(UUID.fromString(validToken))!!
        jointLandlordInvitationRepository.delete(invitation)

        confirmPage.form.submit()

        assertPageIs(page, InvitationUnavailablePage::class)
    }

    @Test
    fun `Landlord who is already on the invited property is redirected to the property record after signing in`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(alreadyOnPropertyToken)
        acceptOrRejectPage.acceptInvitation()
        // After pressing accept the service attempts to sign the user in

        assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "2"))
    }

    @Test
    fun `Landlord added to the invited property while in the journey is sent back to the start`(page: Page) {
        var acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
        acceptOrRejectPage.acceptInvitation()

        val confirmPage = assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)

        // Add the user to the invited property after the confirm page has rendered
        val landlord = landlordRepository.findById(1).get()
        propertyOwnershipService.addLandlordToPropertyOwnership(1, landlord)

        confirmPage.form.submit()

        acceptOrRejectPage = assertPageIs(page, AcceptOrRejectPage::class)
        acceptOrRejectPage.acceptInvitation()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "1"))
    }

    @Nested
    inner class UserIsNotLandlord : NestedIntegrationTestWithMutableData("data-joint-landlord-invitation-mockuser-not-landlord.sql") {
        @Test
        fun `User with a valid token can accept the invitation, register as a landlord and reach a confirmation page`(page: Page) {
            val verifiedIdentity = VerifiedIdentityDataModel("name", LocalDate.now())
            whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentity)

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

            val landlordTypePage = assertPageIs(page, LandlordTypeFormPageAcceptJointLandlordInvitation::class)
            landlordTypePage.submitIndividual()

            val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageAcceptJointLandlordInvitation::class)
            countryOfResidencePage.submitUk()

            val lookupAddressPage = assertPageIs(page, LookupAddressFormPageAcceptJointLandlordInvitation::class)
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

            val selectAddressPage = assertPageIs(page, SelectAddressFormPageAcceptJointLandlordInvitation::class)
            selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

            val checkAnswersPage = assertPageIs(page, CheckAnswersPageAcceptJointLandlordInvitation::class)
            checkAnswersPage.confirmAndSubmit()

            val confirmYouAreALandlordForThisPropertyPage = assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)
            assertThat(confirmYouAreALandlordForThisPropertyPage.propertyAddress).containsText("2 Fake Way")
            assertThat(confirmYouAreALandlordForThisPropertyPage.successBanner).isVisible()
            assertThat(confirmYouAreALandlordForThisPropertyPage.successBanner).containsText("L-")
            confirmYouAreALandlordForThisPropertyPage.form.submit()

            val confirmationPage = assertPageIs(page, PropertyJoinedConfirmationPage::class)
            assertThat(confirmationPage.confirmationBanner.body).containsText("2 Fake Way")
            assertThat(confirmationPage.surveyLink.locator).isVisible()
        }

        @Test
        fun `User with a valid token can reject the invitation and reach a confirmation page without registering as a landlord`(
            page: Page,
        ) {
            val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
            acceptOrRejectPage.rejectInvitation()
            assertPageIs(page, InvitationRejectedConfirmationPage::class)
        }

        @Test
        fun `User with a valid token can accept the invitation, register as an organisation landlord and reach a confirmation page`(
            page: Page,
        ) {
            val verifiedIdentity = VerifiedIdentityDataModel("name", LocalDate.now())
            whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentity)

            val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
            acceptOrRejectPage.acceptInvitation()

            val privacyNoticePage = assertPageIs(page, PrivacyNoticePageAcceptJointLandlordInvitation::class)
            privacyNoticePage.agreeAndSubmit()

            val confirmIdentityPage = assertPageIs(page, ConfirmIdentityFormPageAcceptJointLandlordInvitation::class)
            confirmIdentityPage.confirm()

            val emailPage = assertPageIs(page, EmailFormPageAcceptJointLandlordInvitation::class)
            emailPage.submitEmail("registrant@example.com")

            val phoneNumPage = assertPageIs(page, PhoneNumberFormPageAcceptJointLandlordInvitation::class)
            phoneNumPage.submitPhoneNumber("07123456789")

            val landlordTypePage = assertPageIs(page, LandlordTypeFormPageAcceptJointLandlordInvitation::class)
            landlordTypePage.submitOrganisation()

            val orgNamePage = assertPageIs(page, OrgNameFormPageAcceptJointLandlordInvitation::class)
            orgNamePage.submitName("Test Organisation Name")

            val orgAddressPage = assertPageIs(page, OrgAddressFormPageAcceptJointLandlordInvitation::class)
            orgAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

            val orgSelectAddressPage = assertPageIs(page, OrgSelectAddressFormPageAcceptJointLandlordInvitation::class)
            orgSelectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

            val orgEmailPage = assertPageIs(page, OrgEmailFormPageAcceptJointLandlordInvitation::class)
            orgEmailPage.submitEmail("test.address@provider.com")

            val orgPhoneNumberPage = assertPageIs(page, OrgPhoneNumberFormPageAcceptJointLandlordInvitation::class)
            orgPhoneNumberPage.submitPhoneNumber("07777777777")

            val orgTypePage = assertPageIs(page, OrgTypeFormPageAcceptJointLandlordInvitation::class)
            orgTypePage.submitCompany()

            val orgIsRegisteredCharityPage = assertPageIs(page, OrgIsRegisteredCharityFormPageAcceptJointLandlordInvitation::class)
            orgIsRegisteredCharityPage.submitNo()

            val orgIsRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageAcceptJointLandlordInvitation::class)
            orgIsRegisteredCompanyPage.submitYes()

            val orgCompanyNumberPage = assertPageIs(page, OrgCompanyNumberFormPageAcceptJointLandlordInvitation::class)
            orgCompanyNumberPage.submitCompanyNumber("12345678")

            val orgMainContactPage = assertPageIs(page, OrgMainContactFormPageAcceptJointLandlordInvitation::class)
            orgMainContactPage.submit("Test Contact", "contact@example.com", "07123456789")

            val checkAnswersPage = assertPageIs(page, CheckAnswersPageAcceptJointLandlordInvitation::class)
            checkAnswersPage.confirmAndSubmit()

            val confirmYouAreALandlordForThisPropertyPage = assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)
            assertThat(confirmYouAreALandlordForThisPropertyPage.propertyAddress).containsText("2 Fake Way")
            assertThat(confirmYouAreALandlordForThisPropertyPage.successBanner).isVisible()
            assertThat(confirmYouAreALandlordForThisPropertyPage.successBanner).containsText("L-")
            confirmYouAreALandlordForThisPropertyPage.form.submit()

            val confirmationPage = assertPageIs(page, PropertyJoinedConfirmationPage::class)
            assertThat(confirmationPage.confirmationBanner.body).containsText("2 Fake Way")
            assertThat(confirmationPage.surveyLink.locator).isVisible()
        }
    }
}
