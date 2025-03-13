import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyTest
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership

class PropertyDeregistrationJourneyTests : JourneyTest() {
    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @MockBean
    private lateinit var mockJourneyDataService: JourneyDataService

    @MockBean
    private lateinit var mockJourneyDataServiceFactory: JourneyDataServiceFactory

    @MockBean
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @MockBean
    private lateinit var mockPropertyService: PropertyService

    @SpyBean
    private lateinit var propertyDeregistrationJourneyFactory: PropertyDeregistrationJourneyFactory

    @BeforeEach
    fun setup() {
        mockJourneyDataServiceFactory = mock()
        mockJourneyDataService = mock()
        mockPropertyOwnershipService = mock()
        mockPropertyService = mock()

        whenever(mockJourneyDataServiceFactory.create(anyString())).thenReturn(mockJourneyDataService)

        propertyDeregistrationJourneyFactory =
            PropertyDeregistrationJourneyFactory(
                alwaysTrueValidator,
                mockJourneyDataServiceFactory,
                mockPropertyOwnershipService,
                mockPropertyService,
            )
    }

    @Test
    fun `When the reason step is submitted by an authorised user, the property is deregistered`() {
        val propertyOwnership = createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id

        val currentUserId = propertyOwnership.primaryLandlord.baseUser.id
        setMockUser(currentUserId)

        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyDeregistrationJourneyFactory
            .create(propertyOwnershipId)
            .completeStep(DeregisterPropertyStepId.Reason.urlPathSegment, mapOf("reason" to ""), null, mock())

        // Assert
        verify(mockPropertyOwnershipService, times(1)).deletePropertyOwnership(propertyOwnership)
        verify(mockPropertyService, times(1)).deleteProperty(propertyOwnership.property)
    }
}
