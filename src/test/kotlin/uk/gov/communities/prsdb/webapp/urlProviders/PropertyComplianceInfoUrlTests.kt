package uk.gov.communities.prsdb.webapp.urlProviders

import jakarta.validation.Validator
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
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
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.controllers.ControllerTest
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailBulletPointList
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PartialPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModelFactory
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.assertEquals

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
    private lateinit var mockPropertyComplianceJourneyFactory: PropertyComplianceJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var mockValidator: Validator

    @MockitoBean
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var mockEmailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockitoBean
    private lateinit var uploadService: UploadService

    @MockitoBean
    private lateinit var propertyComplianceViewModelFactory: PropertyComplianceViewModelFactory

    @MockitoBean
    private lateinit var landlordService: LandlordService

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `The sign in url generated when a property is partially compliant is routed to the compliance info page`() {
        // Arrange
        val nonCompliantPropertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()
        val propertyOwnershipId = nonCompliantPropertyCompliance.propertyOwnership.id

        whenever(
            mockPropertyOwnershipService.getIsPrimaryLandlord(
                eq(propertyOwnershipId),
                any(),
            ),
        ).thenReturn(true)
        whenever(
            mockPropertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(
                eq(propertyOwnershipId),
                any(),
            ),
        ).thenReturn(nonCompliantPropertyCompliance.propertyOwnership)
        whenever(
            mockPropertyComplianceJourneyFactory.createJourneySteps(
                eq(propertyOwnershipId),
                eq(false),
            ),
        ).thenReturn(mapOf(PropertyComplianceStepId.CheckAndSubmit.urlPathSegment to mockStepLifecycleOrchestrator))
        doAnswer {
            mockEmailNotificationService.sendEmail(
                nonCompliantPropertyCompliance.propertyOwnership.primaryLandlord.email,
                PartialPropertyComplianceConfirmationEmail(
                    nonCompliantPropertyCompliance.propertyOwnership.address.singleLineAddress,
                    RegistrationNumberDataModel.fromRegistrationNumber(
                        nonCompliantPropertyCompliance.propertyOwnership.registrationNumber,
                    ),
                    EmailBulletPointList("Some compliance information needs updating"),
                    absoluteUrlProvider.buildComplianceInformationUri(propertyOwnershipId).toString(),
                ),
            )
            ModelAndView("redirect:${PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId)}")
        }.whenever(mockStepLifecycleOrchestrator)
            .postStepModelAndView(any())

        // Act, Assert
        mvc
            .post(
                PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                    "/${PropertyComplianceStepId.CheckAndSubmit.urlPathSegment}",
            ) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))
            }

        val emailCaptor = argumentCaptor<PartialPropertyComplianceConfirmationEmail>()
        verify(mockEmailNotificationService).sendEmail(
            eq(nonCompliantPropertyCompliance.propertyOwnership.primaryLandlord.email),
            emailCaptor.capture(),
        )

        assertEquals(
            absoluteUrlProvider.buildComplianceInformationUri(propertyOwnershipId).toString(),
            emailCaptor.firstValue.updateComplianceUrl,
        )

        mvc
            .get(emailCaptor.firstValue.updateComplianceUrl)
            .andExpect { status { isOk() } }
    }
}
