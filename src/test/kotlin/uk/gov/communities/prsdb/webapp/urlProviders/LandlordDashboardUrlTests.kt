package uk.gov.communities.prsdb.webapp.urlProviders

import jakarta.validation.Validator
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ControllerTest
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FullPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyPageDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import kotlin.test.Test

@WebMvcTest(
    controllers = [
        LandlordController::class,
        RegisterLandlordController::class,
        RegisterPropertyController::class,
        PropertyComplianceController::class,
    ],
    properties = ["base-url.landlord=http://localhost:8080/landlord"],
)
@Import(AbsoluteUrlProvider::class)
class LandlordDashboardUrlTests(
    context: WebApplicationContext,
) : ControllerTest(context) {
    @MockitoBean
    private lateinit var mockLandlordRegistrationJourneyFactory: LandlordRegistrationJourneyFactory

    private lateinit var landlordRegistrationJourney: LandlordRegistrationJourney

    @MockitoBean
    private lateinit var mockPropertyRegistrationJourneyFactory: PropertyRegistrationJourneyFactory

    private lateinit var propertyRegistrationJourney: PropertyRegistrationJourney

    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    lateinit var mockEmailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockitoBean
    private lateinit var mockLandlordService: LandlordService

    @MockitoBean
    private lateinit var mockPropertyRegistrationService: PropertyRegistrationService

    @MockitoBean
    private lateinit var mockIdentityService: OneLoginIdentityService

    @MockitoBean
    private lateinit var mockJourneyDataServiceFactory: JourneyDataServiceFactory

    @MockitoBean
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var mockTokenCookieService: TokenCookieService

    @MockitoBean
    private lateinit var mockFileUploadService: UploadService

    @MockitoBean
    private lateinit var mockPropertyComplianceJourneyFactory: PropertyComplianceJourneyFactory

    @MockitoBean
    private lateinit var mockPropertyComplianceUpdateJourneyFactory: PropertyComplianceUpdateJourneyFactory

    @MockitoBean
    private lateinit var mockValidator: Validator

    @MockitoBean
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Autowired
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    private lateinit var propertyComplianceJourney: PropertyComplianceJourney

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `The sign in url generated when a landlord is registered is routed to the landlord dashboard`() {
        // Arrange
        landlordRegistrationJourney =
            LandlordRegistrationJourney(
                AlwaysTrueValidator(),
                mockJourneyDataService,
                mock(),
                mockLandlordService,
                absoluteUrlProvider,
                mockEmailNotificationService,
                mock(),
            )
        whenever(mockLandlordRegistrationJourneyFactory.create()).thenReturn(landlordRegistrationJourney)

        val mockJourneyData = JourneyDataBuilder.landlordDefault(mock()).build()
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mockJourneyData)

        val landlord = createLandlord()
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

        val confirmationCaptor = argumentCaptor<LandlordRegistrationConfirmationEmail>()
        Mockito
            .doNothing()
            .whenever(mockEmailNotificationService)
            .sendEmail(any(), confirmationCaptor.capture())

        val encodedDeclarationContent = "agreesToDeclaration=true"

        // Act, Assert
        mvc
            .post("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.Declaration.urlPathSegment}") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = encodedDeclarationContent
                with(csrf())
            }.andExpect { status { is3xxRedirection() } }
            .andExpect { redirectedUrl(CONFIRMATION_PATH_SEGMENT) }

        mvc
            .get(confirmationCaptor.firstValue.prsdURL)
            .andExpect { status { is3xxRedirection() } }
            .andExpect { redirectedUrl(LANDLORD_DASHBOARD_URL) }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `The sign in url generated when a property is registered is routed to the landlord dashboard`() {
        // Arrange
        propertyRegistrationJourney =
            PropertyRegistrationJourney(
                AlwaysTrueValidator(),
                mockJourneyDataService,
                mock(),
                mockPropertyRegistrationService,
                mock(),
                mockLandlordService,
                absoluteUrlProvider,
                mockEmailNotificationService,
            )
        whenever(mockPropertyRegistrationJourneyFactory.create(any())).thenReturn(propertyRegistrationJourney)

        val mockJourneyData = JourneyDataBuilder.propertyDefault(mock()).build()
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mockJourneyData)

        val propertyOwnership = createPropertyOwnership()
        whenever(
            mockPropertyRegistrationService.registerPropertyAndReturnPropertyRegistrationNumber(
                address = anyOrNull(),
                propertyType = anyOrNull(),
                licenseType = anyOrNull(),
                licenceNumber = anyOrNull(),
                ownershipType = anyOrNull(),
                numberOfHouseholds = anyOrNull(),
                numberOfPeople = anyOrNull(),
                baseUserId = anyOrNull(),
            ),
        ).thenReturn(propertyOwnership.registrationNumber)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(anyOrNull())).thenReturn(propertyOwnership.primaryLandlord)

        val confirmationCaptor = argumentCaptor<PropertyRegistrationConfirmationEmail>()
        Mockito
            .doNothing()
            .whenever(mockEmailNotificationService)
            .sendEmail(any(), confirmationCaptor.capture())

        val encodedDeclarationContent = "agreesToDeclaration=true"

        // Act, Assert
        mvc
            .post("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.Declaration.urlPathSegment}") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = encodedDeclarationContent
                with(csrf())
            }.andExpect { status { is3xxRedirection() } }
            .andExpect { redirectedUrl(CONFIRMATION_PATH_SEGMENT) }

        mvc
            .get(confirmationCaptor.firstValue.prsdUrl)
            .andExpect { status { is3xxRedirection() } }
            .andExpect { redirectedUrl(LANDLORD_DASHBOARD_URL) }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `The sign in url generated when a property is fully compliant is routed to the landlord dashboard`() {
        // Arrange
        val compliantPropertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                eq(compliantPropertyCompliance.propertyOwnership.id),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenReturn(compliantPropertyCompliance)

        whenever(mockPropertyOwnershipService.getIsPrimaryLandlord(eq(compliantPropertyCompliance.propertyOwnership.id), any()))
            .thenReturn(true)

        val mockJourneyData = JourneyPageDataBuilder.beforePropertyComplianceCheckAnswers().build()
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mockJourneyData)

        propertyComplianceJourney =
            PropertyComplianceJourney(
                AlwaysTrueValidator(),
                mockJourneyDataService,
                mockPropertyOwnershipService,
                epcLookupService = mock(),
                mockPropertyComplianceService,
                compliantPropertyCompliance.propertyOwnership.id,
                epcCertificateUrlProvider = mock(),
                MockMessageSource(),
                mockEmailNotificationService,
                mockEmailNotificationService,
                absoluteUrlProvider,
                certificateUploadService = mock(),
                uploadService = mock(),
                checkingAnswersForStep = null,
                stepName = PropertyComplianceStepId.CheckAndSubmit.urlPathSegment,
            )
        whenever(mockPropertyComplianceJourneyFactory.create(any(), any(), anyOrNull())).thenReturn(propertyComplianceJourney)

        // Act, Assert
        mvc
            .post(
                PropertyComplianceController.getPropertyCompliancePath(compliantPropertyCompliance.propertyOwnership.id) +
                    "/${PropertyComplianceStepId.CheckAndSubmit.urlPathSegment}",
            ) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(CONFIRMATION_PATH_SEGMENT)
            }

        val emailCaptor = argumentCaptor<FullPropertyComplianceConfirmationEmail>()
        verify(mockEmailNotificationService).sendEmail(
            eq(compliantPropertyCompliance.propertyOwnership.primaryLandlord.email),
            emailCaptor.capture(),
        )

        mvc
            .get(emailCaptor.firstValue.dashboardUrl)
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(LANDLORD_DASHBOARD_URL)
            }
    }
}
