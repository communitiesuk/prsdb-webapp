package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

class LandlordDeregistrationJourneyTests {
    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @MockBean
    private lateinit var mockJourneyDataService: JourneyDataService

    @MockBean
    private lateinit var mockJourneyDataServiceFactory: JourneyDataServiceFactory

    @SpyBean
    private lateinit var landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory

    @BeforeEach
    fun setup() {
        mockJourneyDataServiceFactory = mock()
        mockJourneyDataService = mock()

        whenever(mockJourneyDataServiceFactory.create(anyString())).thenReturn(mockJourneyDataService)

        landlordDeregistrationJourneyFactory =
            LandlordDeregistrationJourneyFactory(
                alwaysTrueValidator,
                mockJourneyDataServiceFactory,
            )
    }

    @Test
    fun `getHeadingsAndHintsForAreYouSureStep adds the no properties fieldset heading if the user has no registered properties`() {
        // Arrange
        val journeyData =
            mutableMapOf(
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment to
                    mutableMapOf(
                        USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY to false,
                    ),
            ) as JourneyData
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        // Act
        val content = landlordDeregistrationJourneyFactory.create().getHeadingsAndHintsForAreYouSureStep()

        // Assert
        assertEquals("forms.areYouSure.landlordDeregistration.noProperties.fieldSetHeading", content["fieldSetHeading"])
        assertNull(content["fieldSetHint"])
    }

    @Test
    fun `getHeadingsAndHintsForAreYouSureStep adds the with properties fieldset heading and hint if the user has registered properties`() {
        // Arrange
        val journeyData =
            mutableMapOf(
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment to
                    mutableMapOf(
                        USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY to true,
                    ),
            ) as JourneyData
        val modelAndView = ModelAndView()
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        // Act
        val content = landlordDeregistrationJourneyFactory.create().getHeadingsAndHintsForAreYouSureStep()

        // Assert
        assertEquals("forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHeading", content["fieldSetHeading"])
        assertEquals("forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHint", content["fieldSetHint"])
    }

    @Test
    fun `getHeadingsAndHintsForAreYouSureStep throws an exception if userHasRegisteredProperties is not found in JourneyData`() {
        val journeyData = mutableMapOf<String, Any>() as JourneyData
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)
        val thrownError =
            assertThrows<ResponseStatusException> {
                landlordDeregistrationJourneyFactory.create().getHeadingsAndHintsForAreYouSureStep()
            }
        assertEquals(thrownError.statusCode, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
