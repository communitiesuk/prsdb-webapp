package uk.gov.communities.prsdb.webapp.urlProviders

import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.ControllerTest
import uk.gov.communities.prsdb.webapp.controllers.LandlordDashboardController
import uk.gov.communities.prsdb.webapp.controllers.LandlordDashboardController.Companion.LANDLORD_BASE_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import java.net.URLEncoder
import kotlin.test.Test
import kotlin.test.assertContains

@WebMvcTest(controllers = [LandlordDashboardController::class, RegisterLandlordController::class])
class LandlordDashboardUrlTests(
    context: WebApplicationContext,
) : ControllerTest(context) {
    @SpyBean
    private lateinit var landlordRegistrationJourney: LandlordRegistrationJourney

    @MockBean
    lateinit var anyEmailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockBean
    lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @MockBean
    lateinit var mockLandlordService: LandlordService

    @MockBean
    lateinit var mockIdentityService: OneLoginIdentityService

    @MockBean
    lateinit var mockJourneyDataService: JourneyDataService

    @MockBean
    lateinit var mockAddressLookupService: AddressLookupService

    @MockBean
    lateinit var mockAddressDataService: AddressDataService

    @MockBean
    lateinit var mockLocalAuthorityService: LocalAuthorityService

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `The sign in url generated when a landlord is registered is routed to the landlord dashboard`() {
        // Arrange
        val testEmail = "test@example.com"
        val landlord = createLandlord(email = testEmail)

        val mockJourneyData = JourneyDataBuilder.landlordDefault(mockAddressDataService, mockLocalAuthorityService).build()
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mockJourneyData)

        // It's not using this, it's trying to call the real function - looks like it's using a real bean?
        whenever(
            mockLandlordService.createLandlord(
                baseUserId = anyOrNull(),
                name = anyOrNull(),
                email = anyOrNull(),
                phoneNumber = anyOrNull(),
                addressDataModel = anyOrNull(),
                countryOfResidence = anyOrNull(),
                isVerified = anyOrNull(),
                nonEnglandOrWalesAddress = anyOrNull(),
                dateOfBirth = anyOrNull(),
            ),
        ).thenReturn(landlord)

        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri())
            .thenCallRealMethod()

        val confirmationCaptor = argumentCaptor<LandlordRegistrationConfirmationEmail>()
        Mockito
            .doNothing()
            .whenever(
                anyEmailNotificationService,
            ).sendEmail(any(), confirmationCaptor.capture())

        // TODO: PRSD-670 Can this just be "agreesToDeclaration=true" ? It's cribbed from the invitationUrlTests version but might not need to be so complicated...
        val encodedDeclarationContent = "agreesToDeclaration=${URLEncoder.encode("true", "UTF-8")}"

        // Act
        mvc
            .post("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.Declaration.urlPathSegment}") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = encodedDeclarationContent
                with(csrf())
            }.andExpect { status { is3xxRedirection() } }

        println(confirmationCaptor.firstValue.prsdURL)
        mvc
            .get(confirmationCaptor.firstValue.prsdURL)
            .andExpect { status { is3xxRedirection() } }

        // Assert
        assertContains(confirmationCaptor.firstValue.prsdURL, LANDLORD_BASE_URL)
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `The sign in url generated when a property is registered is routed to the landlord dashboard`() {
    }
}
