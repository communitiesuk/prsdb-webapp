package uk.gov.communities.prsdb.webapp.forms

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.validation.Validator
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import org.springframework.validation.support.BindingAwareModelMap
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.UpdateDetailsJourney
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService

class UpdateDetailsJourneyTests {
    private lateinit var validatorFactory: ValidatorFactory
    private lateinit var validator: Validator

    class TestFormModel : FormModel {
        @NotNull
        var testProperty: String? = null
    }

    @Mock
    lateinit var mockJourneyDataService: JourneyDataService

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = SpringValidatorAdapter(validatorFactory.validator)
    }

    @AfterEach
    fun tearDown() {
        validatorFactory.close()
    }

    @Nested
    inner class PopulateModelAndGetViewNameTests {
        @Test
        fun `throws not found exception when step is missing`() {
            // Arrange
            val testJourney =
                UpdateDetailsJourney(
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    landlordService = mock(),
                )
            val model = BindingAwareModelMap()

            // Act and Assert
            TODO()
        }

        @Test
        fun `returns redirect when step is not reachable`() {
            // Arrange
            val testJourney =

                UpdateDetailsJourney(
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    landlordService = mock(),
                )
            val model = BindingAwareModelMap()
            val pageData: PageData = mutableMapOf("testProperty" to "testPropertyValue")
            TODO()
        }

        @Test
        fun `returns redirect when earlier step has validation errors`() {
            // Arrange
            val testJourney =
                UpdateDetailsJourney(
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    landlordService = mock(),
                )
            val model = BindingAwareModelMap()
            val journeyData: JourneyData = mutableMapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)
            TODO()
        }

        @Test
        fun `returns populated model and template name when step is reachable`() {
            // Arrange
            val testJourney =
                UpdateDetailsJourney(
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    landlordService = mock(),
                )
            val model = BindingAwareModelMap()
            val pageData: PageData = mutableMapOf("testProperty" to "testPropertyValue")
            TODO()
        }
    }

    @ExtendWith(MockitoExtension::class)
    @Nested
    inner class FilteredJourneyData {
        @Mock
        private lateinit var mockHttpSession: HttpSession

        @Mock
        private lateinit var mockFormContextRepository: FormContextRepository

        @Mock
        private lateinit var mockOneLoginUserRepository: OneLoginUserRepository

        @Mock
        private lateinit var mockLandlordService: LandlordService

        private lateinit var journeyDataService: JourneyDataService

        @BeforeEach
        fun setup() {
            journeyDataService =
                JourneyDataService(
                    mockHttpSession,
                    mockFormContextRepository,
                    mockOneLoginUserRepository,
                    ObjectMapper(),
                )

            mockLandlordService = mock()
        }

        @Test
        fun `populateModelAndGetViewName returns populateModelAndGetTemplateName with filteredJourneyData as a parameter`() {
            // Arrange
            val testJourney =
                UpdateDetailsJourney(
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    landlordService = mockLandlordService,
                )

            val model = BindingAwareModelMap()

            val previousJourneyData =
                UpdateDetailsStepId.entries.associateBy({ it.urlPathSegment }, {
                    mutableMapOf(
                        "testProperty" to "testValue",
                    )
                })
            TODO()
        }
    }

    @Nested
    inner class UpdateJourneyDataAndGetViewNameOrRedirectTests {
        @Test
        fun `throws not found exception when step is missing`() {
            // Arrange
            TODO()
        }

        @Test
        fun `returns populated current step view when page has validation errors`() {
            TODO()
        }

        @Test
        fun `returns redirect to next step when valid data and updates the journey data when the page data is valid`() {
            // Arrange
            TODO()
        }

        @Test
        fun `saves the journey data when saveAfterSubmit is set on the step`() {
            TODO()
        }

        @Test
        fun `delegates to the handleSubmitAndRedirect function when present on the current step`() {
            TODO()
        }

        @Test
        fun `throws illegal state exception when nextAction function returns null`() {
            TODO()
        }
    }
}
