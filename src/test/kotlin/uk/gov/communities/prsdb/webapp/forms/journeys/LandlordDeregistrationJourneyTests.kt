package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

class LandlordDeregistrationJourneyTests {
    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @MockitoBean
    private lateinit var mockJourneyDataService: JourneyDataService

    @MockitoBean
    private lateinit var mockJourneyDataServiceFactory: JourneyDataServiceFactory

    @MockitoBean
    private lateinit var mockLandlordDeregistrationService: LandlordDeregistrationService

    @MockitoBean
    private lateinit var mockSecurityContextService: SecurityContextService

    @MockitoSpyBean
    private lateinit var landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        mockJourneyDataServiceFactory = mock()
        mockLandlordDeregistrationService = mock()
        mockSecurityContextService = mock()

        landlordDeregistrationJourneyFactory =
            LandlordDeregistrationJourneyFactory(
                alwaysTrueValidator,
                mockJourneyDataServiceFactory,
                mockLandlordDeregistrationService,
                mockSecurityContextService,
            )
    }

    @Test
    fun `When the landlord is deregistered, user roles are refreshed`() {
        // Arrange
        val baseUserId = "user-id"
        setupDeregistrationAsALandlordWithNoProperties(baseUserId)

        // Act
        landlordDeregistrationJourneyFactory
            .create()
            .completeStep(DeregisterLandlordStepId.AreYouSure.urlPathSegment, mapOf("wantsToProceed" to "true"), null, mock())

        // Assert
        verify(mockLandlordDeregistrationService).deregisterLandlordAndTheirProperties(baseUserId)
        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `When the landlord is deregistered, landlordHadActiveProperties is stored in the session and journey data is cleared`() {
        // Arrange
        setupDeregistrationAsALandlordWithNoProperties(baseUserId = "user-id")

        // Act
        landlordDeregistrationJourneyFactory
            .create()
            .completeStep(DeregisterLandlordStepId.AreYouSure.urlPathSegment, mapOf("wantsToProceed" to "true"), null, mock())

        // Assert
        verify(mockLandlordDeregistrationService).addLandlordHadActivePropertiesToSession(false)
        verify(mockJourneyDataService).clearJourneyDataFromSession()
    }

    private fun setupDeregistrationAsALandlordWithNoProperties(baseUserId: String) {
        val journeyData =
            mutableMapOf(
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment to
                    mutableMapOf(
                        USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY to false,
                    ),
            ) as JourneyData

        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)
        whenever(mockJourneyDataServiceFactory.create(DEREGISTER_LANDLORD_JOURNEY_URL)).thenReturn(mockJourneyDataService)

        JourneyTestHelper.setMockUser(baseUserId)
    }
}
