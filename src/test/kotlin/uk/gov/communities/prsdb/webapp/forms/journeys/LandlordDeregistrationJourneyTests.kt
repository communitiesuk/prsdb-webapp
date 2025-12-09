package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordNoPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordWithPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDetailsEmailSection
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDetailsEmailSectionList
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class LandlordDeregistrationJourneyTests {
    private val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    private lateinit var mockJourneyDataService: JourneyDataService

    private lateinit var mockJourneyDataServiceFactory: JourneyDataServiceFactory

    private lateinit var mockLandlordDeregistrationService: LandlordDeregistrationService

    private lateinit var mockLandlordService: LandlordService

    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    private lateinit var mockSecurityContextService: SecurityContextService

    private lateinit var mockConfirmationWithNoPropertiesEmailSender:
        EmailNotificationService<LandlordNoPropertiesDeregistrationConfirmationEmail>

    private lateinit var mockConfirmationLandlordWithPropertiesEmailSender:
        EmailNotificationService<LandlordWithPropertiesDeregistrationConfirmationEmail>

    private lateinit var landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        mockJourneyDataServiceFactory = mock()
        mockLandlordDeregistrationService = mock()
        mockLandlordService = mock()
        mockPropertyOwnershipService = mock()
        mockSecurityContextService = mock()
        mockConfirmationWithNoPropertiesEmailSender = mock()
        mockConfirmationLandlordWithPropertiesEmailSender = mock()

        landlordDeregistrationJourneyFactory =
            LandlordDeregistrationJourneyFactory(
                alwaysTrueValidator,
                mockJourneyDataServiceFactory,
                mockLandlordDeregistrationService,
                mockLandlordService,
                mockPropertyOwnershipService,
                mockSecurityContextService,
                mockConfirmationWithNoPropertiesEmailSender,
                mockConfirmationLandlordWithPropertiesEmailSender,
            )
    }

    @Test
    fun `When the landlord is deregistered, user roles are refreshed`() {
        // Arrange
        val baseUserId = "user-id"
        setupDeregistrationAsALandlord(baseUserId, userProperties = emptyList())

        // Act
        landlordDeregistrationJourneyFactory
            .create()
            .completeStep(DeregisterLandlordStepId.AreYouSure.urlPathSegment, mapOf("wantsToProceed" to "true"), null, mock())

        // Assert
        verify(mockLandlordDeregistrationService).deregisterLandlord(baseUserId)
        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `When the landlord is deregistered, landlordHadActiveProperties is stored in the session and journey data is cleared`() {
        // Arrange
        setupDeregistrationAsALandlord(baseUserId = "user-id", userProperties = emptyList())

        // Act
        landlordDeregistrationJourneyFactory
            .create()
            .completeStep(DeregisterLandlordStepId.AreYouSure.urlPathSegment, mapOf("wantsToProceed" to "true"), null, mock())

        // Assert
        verify(mockLandlordDeregistrationService).addLandlordHadActivePropertiesToSession(false)
        verify(mockJourneyDataService).removeJourneyDataAndContextIdFromSession()
    }

    @Test
    fun `When a landlord with no properties is deregistered a confirmation email is sent`() {
        // Arrange
        setupDeregistrationAsALandlord(baseUserId = "user-id", userProperties = emptyList())

        // Act
        landlordDeregistrationJourneyFactory
            .create()
            .completeStep(DeregisterLandlordStepId.AreYouSure.urlPathSegment, mapOf("wantsToProceed" to "true"), null, mock())

        // Assert
        verify(mockConfirmationWithNoPropertiesEmailSender)
            .sendEmail(eq("example@email.com"), any())
    }

    @Test
    fun `When a landlord with registered properties is deregistered a confirmation email is sent`() {
        val propertyOwnedByLandlord = MockLandlordData.createPropertyOwnership()
        setupDeregistrationAsALandlord(baseUserId = "user-id", userProperties = listOf(propertyOwnedByLandlord))

        val expectedPropertyEmailSectionList =
            PropertyDetailsEmailSectionList(
                listOf(
                    PropertyDetailsEmailSection(
                        1,
                        RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnedByLandlord.registrationNumber).toString(),
                        propertyOwnedByLandlord.address.singleLineAddress,
                    ),
                ),
            )

        // Act
        landlordDeregistrationJourneyFactory
            .create()
            .completeStep(DeregisterLandlordStepId.Reason.urlPathSegment, mapOf("reason" to ""), null, mock())

        // Assert
        verify(mockConfirmationLandlordWithPropertiesEmailSender)
            .sendEmail(
                "example@email.com",
                LandlordWithPropertiesDeregistrationConfirmationEmail(expectedPropertyEmailSectionList),
            )
    }

    private fun setupDeregistrationAsALandlord(
        baseUserId: String,
        userProperties: List<PropertyOwnership>,
    ) {
        val journeyData =
            mutableMapOf(
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment to
                    mutableMapOf(
                        LandlordDeregistrationCheckUserPropertiesFormModel::userHasRegisteredProperties.name to userProperties.isNotEmpty(),
                    ),
            ) as JourneyData

        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)
        whenever(
            mockJourneyDataServiceFactory.create(DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE),
        ).thenReturn(mockJourneyDataService)
        whenever(
            mockLandlordService.retrieveLandlordByBaseUserId(baseUserId),
        ).thenReturn(MockLandlordData.createLandlord(email = "example@email.com"))
        whenever(mockPropertyOwnershipService.retrieveAllActivePropertiesForLandlord(baseUserId)).thenReturn(userProperties)

        JourneyTestHelper.setMockUser(baseUserId)
    }
}
