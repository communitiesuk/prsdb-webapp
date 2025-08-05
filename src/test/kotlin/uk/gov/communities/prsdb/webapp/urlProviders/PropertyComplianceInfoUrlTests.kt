package uk.gov.communities.prsdb.webapp.urlProviders

import jakarta.validation.Validator
import org.junit.jupiter.api.Test
import org.mockito.Mock
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
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PartialPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.FileUploader
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyPageDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource

@WebMvcTest(
    controllers = [PropertyComplianceController::class, PropertyDetailsController::class],
    properties = ["base-url.landlord=http://localhost:8080/landlord"],
)
@Import(AbsoluteUrlProvider::class)
class PropertyComplianceInfoUrlTests(
    context: WebApplicationContext,
) : ControllerTest(context) {
    @MockitoBean
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Autowired
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @MockitoBean
    private lateinit var mockTokenCookieService: TokenCookieService

    @MockitoBean
    private lateinit var mockFileUploader: FileUploader

    @MockitoBean
    private lateinit var mockPropertyComplianceJourneyFactory: PropertyComplianceJourneyFactory

    @MockitoBean
    private lateinit var mockPropertyComplianceUpdateJourneyFactory: PropertyComplianceUpdateJourneyFactory

    @MockitoBean
    private lateinit var mockValidator: Validator

    @MockitoBean
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var mockPropertyDetailsUpdateJourneyFactory: PropertyDetailsUpdateJourneyFactory

    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    private lateinit var mockEmailNotificationService: EmailNotificationService<EmailTemplateModel>

    private lateinit var propertyComplianceJourney: PropertyComplianceJourney

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `The sign in url generated when a property is partially compliant is routed to the compliance info page`() {
        // Arrange
        val nonCompliantPropertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                eq(nonCompliantPropertyCompliance.propertyOwnership.id),
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
        ).thenReturn(nonCompliantPropertyCompliance)

        whenever(
            mockPropertyOwnershipService.getIsPrimaryLandlord(
                eq(nonCompliantPropertyCompliance.propertyOwnership.id),
                any(),
            ),
        ).thenReturn(true)
        whenever(
            mockPropertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(
                eq(nonCompliantPropertyCompliance.propertyOwnership.id),
                any(),
            ),
        ).thenReturn(nonCompliantPropertyCompliance.propertyOwnership)

        val mockJourneyData = JourneyPageDataBuilder.beforePropertyComplianceCheckAnswers().build()
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mockJourneyData)

        propertyComplianceJourney =
            PropertyComplianceJourney(
                AlwaysTrueValidator(),
                mockJourneyDataService,
                mockPropertyOwnershipService,
                epcLookupService = mock(),
                mockPropertyComplianceService,
                nonCompliantPropertyCompliance.propertyOwnership.id,
                epcCertificateUrlProvider = mock(),
                MockMessageSource(),
                mockEmailNotificationService,
                mockEmailNotificationService,
                absoluteUrlProvider,
                checkingAnswersForStep = null,
                stepName = PropertyComplianceStepId.CheckAndSubmit.name,
            )
        whenever(mockPropertyComplianceJourneyFactory.create(any(), any(), anyOrNull())).thenReturn(propertyComplianceJourney)

        // Act, Assert
        mvc
            .post(
                PropertyComplianceController.getPropertyCompliancePath(nonCompliantPropertyCompliance.propertyOwnership.id) +
                    "/${PropertyComplianceStepId.CheckAndSubmit.urlPathSegment}",
            ) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(CONFIRMATION_PATH_SEGMENT)
            }

        val emailCaptor = argumentCaptor<PartialPropertyComplianceConfirmationEmail>()
        verify(mockEmailNotificationService).sendEmail(
            eq(nonCompliantPropertyCompliance.propertyOwnership.primaryLandlord.email),
            emailCaptor.capture(),
        )

        mvc
            .get(emailCaptor.firstValue.updateComplianceUrl)
            .andExpect { status { isOk() } }
    }
}
