package uk.gov.communities.prsdb.webapp.forms

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import org.springframework.validation.support.BindingAwareModelMap
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.forms.journeys.Journey
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneySection
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class JourneyTests {
    private lateinit var validatorFactory: ValidatorFactory
    private lateinit var validator: Validator

    enum class TestStepId(
        override val urlPathSegment: String,
    ) : StepId {
        StepOne("step1"),
        StepTwo("step2"),
        StepThree("step3"),
        StepFour("step4"),
    }

    class TestJourney(
        journeyType: JourneyType,
        steps: Set<Step<TestStepId>>,
        override val initialStepId: TestStepId,
        validator: Validator,
        journeyDataService: JourneyDataService,
    ) : Journey<TestStepId>(journeyType, validator, journeyDataService) {
        override val sections: List<JourneySection<TestStepId>> = unitarySetOfSteps(initialStepId, steps)

        var initialisationCount = 0

        override fun oneTimeInitialisation(journeyData: JourneyData) {
            initialisationCount++
        }
    }

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
    inner class GetStepIdTests {
        @Test
        fun `returns correct step id when present`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )

            // Act
            val foundStepId = testJourney.getStepId(TestStepId.StepOne.urlPathSegment)

            // Assert
            assertEquals(TestStepId.StepOne, foundStepId)
        }

        @Test
        fun `throws not found exception when step is missing`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )

            // Act and Assert
            val exception =
                assertThrows<ResponseStatusException> { testJourney.getStepId(TestStepId.StepTwo.urlPathSegment) }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        }
    }

    @Nested
    inner class PopulateModelAndGetViewNameTests {
        @Test
        fun `throws not found exception when step is missing`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()

            // Act and Assert
            val exception =
                assertThrows<ResponseStatusException> {
                    testJourney.populateModelAndGetViewName(
                        TestStepId.StepTwo,
                        model,
                        null,
                    )
                }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        }

        @Test
        fun `returns redirect when step is not reachable`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepThree, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                            ),
                            Step(
                                TestStepId.StepThree,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val pageData: PageData = mutableMapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mutableMapOf(TestStepId.StepOne.urlPathSegment to pageData)

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result = testJourney.populateModelAndGetViewName(TestStepId.StepTwo, model, null, null)

            // Assert
            assertEquals("redirect:/${REGISTER_LANDLORD_JOURNEY_URL}/${TestStepId.StepOne.urlPathSegment}", result)
        }

        @Test
        fun `returns redirect when earlier step has validation errors`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val journeyData: JourneyData = mutableMapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result = testJourney.populateModelAndGetViewName(TestStepId.StepTwo, model, null, null)

            // Assert
            assertEquals("redirect:/${REGISTER_LANDLORD_JOURNEY_URL}/${TestStepId.StepOne.urlPathSegment}", result)
        }

        @Test
        fun `returns populated model and template name when step is reachable`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf(),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "templateName",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val pageData: PageData = mutableMapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mutableMapOf(TestStepId.StepOne.urlPathSegment to pageData)

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result = testJourney.populateModelAndGetViewName(TestStepId.StepTwo, model, null, pageData)

            // Assert
            assertIs<BindingResult>(model[BindingResult.MODEL_KEY_PREFIX + "formModel"])
            val bindingResult: BindingResult = model[BindingResult.MODEL_KEY_PREFIX + "formModel"] as BindingResult
            assertContains(model, "testKey")
            assertEquals("testValue", model["testKey"])
            assertContains(model, "backUrl")
            assertEquals(TestStepId.StepOne.urlPathSegment, model["backUrl"])
            val propertyValue = bindingResult.getRawFieldValue("testProperty")
            assertEquals("testPropertyValue", propertyValue)
            assertEquals("templateName", result)
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
        }

        @Test
        fun `populateModelAndGetViewName returns populateModelAndGetTemplateName with filteredJourneyData as a parameter`() {
            // Arrange
            val spiedOnJourneyDataService = spy(journeyDataService)
            val page =
                Page(
                    TestFormModel::class,
                    "templateName",
                    mutableMapOf("testKey" to "testValue"),
                )
            val spiedOnPage = spy(page)

            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = spiedOnJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf(),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepThree, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "templateName",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                            ),
                            Step(
                                TestStepId.StepThree,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepFour, null) },
                            ),
                            Step(
                                TestStepId.StepFour,
                                page = spiedOnPage,
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val pageDataStepOne: PageData = mutableMapOf("testProperty" to "testProperty")
            val pageDataStepTwo: PageData = mutableMapOf("testPropertyTwo" to "testProperty")
            val pageDataStepThree: PageData = mutableMapOf("testProperty" to "testProperty")
            val pageDataStepFour: PageData = mutableMapOf("testProperty" to "testProperty")
            val journeyData: JourneyData =
                mutableMapOf(
                    TestStepId.StepOne.urlPathSegment to pageDataStepOne,
                    TestStepId.StepTwo.urlPathSegment to pageDataStepTwo,
                    TestStepId.StepThree.urlPathSegment to pageDataStepThree,
                )
            val filteredJourneyData: JourneyData =
                mutableMapOf(
                    TestStepId.StepOne.urlPathSegment to pageDataStepOne,
                    TestStepId.StepThree.urlPathSegment to pageDataStepThree,
                )
            whenever(spiedOnJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            testJourney.populateModelAndGetViewName(TestStepId.StepFour, model, null, pageDataStepFour)

            // Assert
            verify(spiedOnPage).populateModelAndGetTemplateName(
                validator,
                model,
                pageDataStepFour,
                TestStepId.StepThree.urlPathSegment,
                filteredJourneyData,
            )
        }
    }

    @Nested
    inner class UpdateJourneyDataAndGetViewNameOrRedirectTests {
        @Test
        fun `throws not found exception when step is missing`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val pageData: PageData = mutableMapOf()
            val principal = Principal { "testPrincipalId" }

            // Act and Assert
            val exception =
                assertThrows<ResponseStatusException> {
                    testJourney.updateJourneyDataAndGetViewNameOrRedirect(
                        TestStepId.StepTwo,
                        pageData,
                        model,
                        null,
                        principal,
                    )
                }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        }

        @Test
        fun `returns populated current step view when page has validation errors`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepOneTemplate",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mutableMapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mutableMapOf()
            val journeyData: JourneyData =
                mutableMapOf(TestStepId.StepOne.urlPathSegment to pageData)

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.updateJourneyDataAndGetViewNameOrRedirect(
                    TestStepId.StepOne,
                    pageData,
                    model,
                    null,
                    principal,
                )

            // Assert
            assertIs<BindingResult>(model[BindingResult.MODEL_KEY_PREFIX + "formModel"])
            val bindingResult: BindingResult = model[BindingResult.MODEL_KEY_PREFIX + "formModel"] as BindingResult
            assertContains(model, "testKey")
            assertEquals("testValue", model["testKey"])
            val propertyValue = bindingResult.getRawFieldValue("testProperty")
            assertNull(propertyValue)
            assertEquals("stepOneTemplate", result)
        }

        @Test
        fun `returns redirect to next step when valid data and updates the journey data when the page data is valid`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepOneTemplate",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mutableMapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mutableMapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mutableMapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.updateJourneyDataAndGetViewNameOrRedirect(
                    TestStepId.StepOne,
                    pageData,
                    model,
                    null,
                    principal,
                )
            val journeyDataCaptor = argumentCaptor<JourneyData>()
            verify(mockJourneyDataService).setJourneyData(journeyDataCaptor.capture())

            // Assert
            assertEquals("redirect:/${REGISTER_LANDLORD_JOURNEY_URL}/${TestStepId.StepTwo.urlPathSegment}", result)
            assertIs<PageData>(journeyDataCaptor.firstValue[TestStepId.StepOne.urlPathSegment]!!)
            val resultPageData = objectToStringKeyedMap(journeyDataCaptor.firstValue[TestStepId.StepOne.urlPathSegment])
            assertEquals("testPropertyValue", resultPageData?.get("testProperty"))
        }

        @Test
        fun `saves the journey data when saveAfterSubmit is set to true on the step`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepOneTemplate",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                                saveAfterSubmit = true,
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mutableMapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mutableMapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mutableMapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.updateJourneyDataAndGetViewNameOrRedirect(
                    TestStepId.StepOne,
                    pageData,
                    model,
                    null,
                    principal,
                )
            val journeyDataCaptor = argumentCaptor<JourneyData>()
            verify(mockJourneyDataService).saveJourneyData(
                anyLong(),
                journeyDataCaptor.capture(),
                eq(JourneyType.LANDLORD_REGISTRATION),
                eq(principal),
            )

            // Assert
            assertEquals("redirect:/${REGISTER_LANDLORD_JOURNEY_URL}/${TestStepId.StepTwo.urlPathSegment}", result)
            assertIs<PageData>(journeyDataCaptor.firstValue[TestStepId.StepOne.urlPathSegment]!!)
            val resultPageData = objectToStringKeyedMap(journeyDataCaptor.firstValue[TestStepId.StepOne.urlPathSegment])
            assertEquals("testPropertyValue", resultPageData?.get("testProperty"))
        }

        @Test
        fun `does not save the journey data when saveAfterSubmit is set to false on the step`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepOneTemplate",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                                saveAfterSubmit = false,
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mutableMapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mutableMapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mutableMapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.updateJourneyDataAndGetViewNameOrRedirect(
                    TestStepId.StepOne,
                    pageData,
                    model,
                    null,
                    principal,
                )
            val journeyDataCaptor = argumentCaptor<JourneyData>()

            // Assert
            verify(mockJourneyDataService, never()).saveJourneyData(
                anyLong(),
                journeyDataCaptor.capture(),
                eq(JourneyType.LANDLORD_REGISTRATION),
                eq(principal),
            )
            assertEquals("redirect:/${REGISTER_LANDLORD_JOURNEY_URL}/${TestStepId.StepTwo.urlPathSegment}", result)
        }

        @Test
        fun `delegates to the handleSubmitAndRedirect function when present on the current step`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepOneTemplate",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                handleSubmitAndRedirect = { _, _ -> "/customRedirect" },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mutableMapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mutableMapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mutableMapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.updateJourneyDataAndGetViewNameOrRedirect(
                    TestStepId.StepOne,
                    pageData,
                    model,
                    null,
                    principal,
                )

            // Assert
            assertEquals("redirect:/customRedirect", result)
        }

        @Test
        fun `throws illegal state exception when nextAction function returns null`() {
            // Arrange
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepOneTemplate",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(null, null) },
                            ),
                        ),
                )
            val model = BindingAwareModelMap()
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mutableMapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mutableMapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act and Assert
            assertThrows<IllegalStateException> {
                testJourney.updateJourneyDataAndGetViewNameOrRedirect(
                    TestStepId.StepOne,
                    pageData,
                    model,
                    null,
                    principal,
                )
            }
        }
    }

    @Nested
    inner class JourneyDataManipulationTests {
        @Test
        fun `when there is no journey data in the session or the database, one-time intialisation is called`() {
            val principalName = "principalName"
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepOneTemplate",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(null, null) },
                            ),
                        ),
                )

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf())
            whenever(mockJourneyDataService.getContextId(principalName, JourneyType.LANDLORD_REGISTRATION)).thenReturn(null)
            val initialInitialisationCount = testJourney.initialisationCount

            // Act
            testJourney.initialiseJourneyDataIfNotInitialised(principalName)

            // Assert
            assertEquals(initialInitialisationCount + 1, testJourney.initialisationCount)
        }

        @Test
        fun `when the journey data is not in the session it will be loaded into the session from the database`() {
            val principalName = "principalName"
            val contextId = 67L
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepOneTemplate",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(null, null) },
                            ),
                        ),
                )
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf())
            whenever(mockJourneyDataService.getContextId(principalName, JourneyType.LANDLORD_REGISTRATION)).thenReturn(contextId)
            val initialInitialisationCount = testJourney.initialisationCount

            // Act
            testJourney.initialiseJourneyDataIfNotInitialised(principalName)

            // Assert
            val captor = argumentCaptor<Long>()
            verify(mockJourneyDataService).loadJourneyDataIntoSession(captor.capture())
            Assertions.assertEquals(contextId, captor.allValues.single())

            assertEquals(initialInitialisationCount, testJourney.initialisationCount)
        }

        @Test
        fun `when the journey data is already in the session, journey data is not loaded and the journey is not reinitialised`() {
            val principalName = "principalName"
            val testJourney =
                TestJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                    steps =
                        setOf(
                            Step(
                                TestStepId.StepOne,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepOneTemplate",
                                        mutableMapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(null, null) },
                            ),
                        ),
                )
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf("anything" to "Anything else"))
            val initialInitialisationCount = testJourney.initialisationCount

            // Act
            testJourney.initialiseJourneyDataIfNotInitialised(principalName)

            // Assert
            assertEquals(initialInitialisationCount, testJourney.initialisationCount)
            verify(mockJourneyDataService, Mockito.never()).loadJourneyDataIntoSession(any())
        }
    }
}
