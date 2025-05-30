import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class PropertyDeregistrationJourneyTests {
    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @MockitoBean
    private lateinit var mockJourneyDataService: JourneyDataService

    @MockitoBean
    private lateinit var mockJourneyDataServiceFactory: JourneyDataServiceFactory

    @MockitoBean
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var mockPropertyDeregistrationService: PropertyDeregistrationService

    @MockitoBean
    lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyDeregistrationConfirmationEmail>

    @MockitoSpyBean
    private lateinit var propertyDeregistrationJourneyFactory: PropertyDeregistrationJourneyFactory

    @BeforeEach
    fun setup() {
        mockJourneyDataServiceFactory = mock()
        mockJourneyDataService = mock()
        mockPropertyOwnershipService = mock()
        mockPropertyDeregistrationService = mock()
        mockConfirmationEmailSender = mock()

        whenever(mockJourneyDataServiceFactory.create(anyString())).thenReturn(mockJourneyDataService)

        propertyDeregistrationJourneyFactory =
            PropertyDeregistrationJourneyFactory(
                alwaysTrueValidator,
                mockJourneyDataServiceFactory,
                mockPropertyOwnershipService,
                mockPropertyDeregistrationService,
                mockConfirmationEmailSender,
            )
    }

    @Test
    fun `When the reason step is submitted, the property is deregistered and the propertyOwnershipId is stored in the session`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id

        val currentUserId = propertyOwnership.primaryLandlord.baseUser.id
        JourneyTestHelper.setMockUser(currentUserId)

        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyDeregistrationJourneyFactory
            .create(propertyOwnershipId)
            .completeStep(DeregisterPropertyStepId.Reason.urlPathSegment, mapOf("reason" to ""), null, mock())

        // Assert
        verify(mockPropertyDeregistrationService).deregisterProperty(propertyOwnershipId)
        verify(mockPropertyDeregistrationService)
            .addDeregisteredPropertyAndOwnershipIdsToSession(propertyOwnershipId, propertyOwnership.property.id)
    }

    @Test
    fun `When the property is deregistered, a confirmation email is sent`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id

        val currentUserId = propertyOwnership.primaryLandlord.baseUser.id
        JourneyTestHelper.setMockUser(currentUserId)

        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyDeregistrationJourneyFactory
            .create(propertyOwnershipId)
            .completeStep(DeregisterPropertyStepId.Reason.urlPathSegment, mapOf("reason" to ""), null, mock())

        verify(mockConfirmationEmailSender).sendEmail(
            "example@email.com",
            PropertyDeregistrationConfirmationEmail(
                prn =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(
                            RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456),
                        ).toString(),
                singleLineAddress = "1 Example Road, EG1 2AB",
            ),
        )
    }
}
