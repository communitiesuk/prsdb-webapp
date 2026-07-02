package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.ConfirmYouAreALandlordForThisPropertyPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ConfirmIdentityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PrivacyNoticePageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfBedroomsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import java.net.URI
import java.time.LocalDate

private const val TAGGED_BUTTON_SELECTOR = "button[data-plausible-event='Transaction']"

class LandlordRegistrationTransactionEventTests : IntegrationTestWithImmutableData("data-mockuser-not-landlord.sql") {
    private val absoluteLandlordUrl = "www.prsd.gov.uk/landlord"

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>

    @MockitoBean
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @BeforeEach
    fun setup() {
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(absoluteLandlordUrl))
        featureFlagManager.disable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Test
    fun `only the landlord registration check answers commit button is tagged for the Plausible Transaction event`(page: Page) {
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(VerifiedIdentityDataModel("name", LocalDate.now()))

        val landlordRegistrationStartPage = navigator.goToLandlordRegistrationServiceInformationStartPage()
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
        landlordRegistrationStartPage.startButton.clickAndWait()

        val privacyNoticePage = assertPageIs(page, PrivacyNoticePageLandlordRegistration::class)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
        privacyNoticePage.agreeAndSubmit()

        val confirmIdentityPage = assertPageIs(page, ConfirmIdentityFormPageLandlordRegistration::class)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
        confirmIdentityPage.confirm()

        val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
        emailPage.submitEmail("test@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
        phoneNumPage.submitPhoneNumber("07123456789")

        val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
        countryOfResidencePage.submitUk()

        val lookupAddressPage = assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage = assertPageIs(page, SelectAddressFormPageLandlordRegistration::class)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).isVisible()
    }
}

class PropertyBedroomsUpdateTransactionEventTests : IntegrationTestWithImmutableData("data-local.sql") {
    private val occupiedPropertyOwnershipId = 1L
    private val occupiedPropertyUrlArguments = mapOf("propertyOwnershipId" to occupiedPropertyOwnershipId.toString())

    @MockitoBean
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @BeforeEach
    fun setUp() {
        whenever(absoluteUrlProvider.buildLandlordDashboardUri())
            .thenReturn(URI("example.com"))
        whenever(absoluteUrlProvider.buildComplianceInformationUri(any()))
            .thenReturn(URI("example.com"))
    }

    @Test
    fun `the property bedrooms update commit button is tagged for the Plausible Transaction event`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
        propertyDetailsPage.propertyDetailsSummaryList.numberOfBedroomsRow.clickFirstActionLinkAndWait()

        assertPageIs(page, NumberOfBedroomsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).isVisible()
    }

    @Test
    fun `the property details page is not tagged for the Plausible Transaction event`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)

        assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)
        assertThat(propertyDetailsPage.page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
    }
}

class AcceptJointLandlordInvitationTransactionEventTests :
    IntegrationTestWithImmutableData("data-joint-landlord-invitation.sql") {
    private val validToken = "aaaabbbb-cccc-dddd-eeee-ffff00001111"

    @BeforeEach
    fun setup() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
        featureFlagManager.disable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Test
    fun `only the confirm-you-are-a-landlord commit button is tagged for the Plausible Transaction event`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).hasCount(0)
        acceptOrRejectPage.acceptInvitation()

        assertPageIs(page, ConfirmYouAreALandlordForThisPropertyPage::class)
        assertThat(page.locator(TAGGED_BUTTON_SELECTOR)).isVisible()
    }
}
