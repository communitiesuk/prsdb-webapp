package uk.gov.communities.prsdb.webapp.forms.journeys

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
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
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        CYAStep("check-your-answers"),
    }

    class TestJourney(
        journeyType: JourneyType,
        initialStepId: TestStepId,
        validator: Validator,
        journeyDataService: JourneyDataService,
        steps: Set<Step<TestStepId>> =
            setOf(
                Step(
                    TestStepId.StepOne,
                    page =
                        Page(
                            TestFormModel::class,
                            "index",
                            mapOf("testKey" to "testValue"),
                        ),
                ),
            ),
    ) : Journey<TestStepId>(journeyType, initialStepId, validator, journeyDataService) {
        override val checkYourAnswersStepId = TestStepId.CYAStep

        override val sections: List<JourneySection<TestStepId>> =
            createSingleSectionWithSingleTaskFromSteps(initialStepId, steps)
    }

    class TestJourneyWithSections(
        journeyType: JourneyType,
        initialStepId: TestStepId,
        validator: Validator,
        journeyDataService: JourneyDataService,
        override val sections: List<JourneySection<TestStepId>>,
    ) : Journey<TestStepId>(journeyType, initialStepId, validator, journeyDataService)

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
                                        mapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )

            // Act and Assert
            val exception =
                assertThrows<ResponseStatusException> {
                    testJourney.getModelAndViewForStep(
                        TestStepId.StepTwo.urlPathSegment,
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
                                        mapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepThree, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mapOf("testKey" to "testValue"),
                                    ),
                            ),
                            Step(
                                TestStepId.StepThree,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )
            val pageData: PageData = mapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mapOf(TestStepId.StepOne.urlPathSegment to pageData)

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.getModelAndViewForStep(TestStepId.StepTwo.urlPathSegment, null, null)

            // Assert
            assertEquals("redirect:${TestStepId.StepOne.urlPathSegment}", result.viewName)
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
                                        mapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )
            val journeyData: JourneyData = mapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.getModelAndViewForStep(TestStepId.StepTwo.urlPathSegment, null, null)

            // Assert
            assertEquals("redirect:${TestStepId.StepOne.urlPathSegment}", result.viewName)
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
                                        mapOf(),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "templateName",
                                        mapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )
            val pageData: PageData = mapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mapOf(TestStepId.StepOne.urlPathSegment to pageData)

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.getModelAndViewForStep(TestStepId.StepTwo.urlPathSegment, null, pageData)

            // Assert
            assertIs<BindingResult>(result.model[BindingResult.MODEL_KEY_PREFIX + "formModel"])
            val bindingResult: BindingResult = result.model[BindingResult.MODEL_KEY_PREFIX + "formModel"] as BindingResult
            assertContains(result.model, "testKey")
            assertEquals("testValue", result.model["testKey"])
            assertContains(result.model, "backUrl")
            assertEquals(TestStepId.StepOne.urlPathSegment, result.model["backUrl"])
            val propertyValue = bindingResult.getRawFieldValue("testProperty")
            assertEquals("testPropertyValue", propertyValue)
            assertEquals("templateName", result.viewName)
        }
    }

    @Nested
    inner class GetSectionHeaderInfoTests {
        @Test
        fun `getSectionHeaderInfo returns SectionHeaderViewModel if headingKey is not null`() {
            val testStep =
                Step(
                    TestStepId.StepOne,
                    page =
                        Page(
                            TestFormModel::class,
                            "index",
                            mapOf("testKey" to "testValue"),
                            shouldDisplaySectionHeader = true,
                        ),
                )

            val testJourney =
                TestJourneyWithSections(
                    JourneyType.PROPERTY_REGISTRATION,
                    sections =
                        listOf(
                            JourneySection.withOneTask(
                                JourneyTask(TestStepId.StepOne, setOf(testStep)),
                                "Test section heading name key",
                            ),
                        ),
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                )

            val result = testJourney.getModelAndViewForStep(TestStepId.StepOne.urlPathSegment, null)

            val expectedSectionHeader = SectionHeaderViewModel("Test section heading name key", 1, 1)
            assertTrue(ReflectionEquals(expectedSectionHeader).matches(result.model["sectionHeaderInfo"]))
        }

        @Test
        fun `getSectionHeaderInfo returns null if headingKey not null`() {
            val testStep =
                Step(
                    TestStepId.StepOne,
                    page =
                        Page(
                            TestFormModel::class,
                            "index",
                            mapOf("testKey" to "testValue"),
                        ),
                )

            val testJourney =
                TestJourneyWithSections(
                    JourneyType.PROPERTY_REGISTRATION,
                    sections =
                        listOf(
                            JourneySection.withOneStep(testStep),
                        ),
                    initialStepId = TestStepId.StepOne,
                    journeyDataService = mockJourneyDataService,
                    validator = validator,
                )

            val result = testJourney.getModelAndViewForStep(TestStepId.StepOne.urlPathSegment, null)

            assertNull(result.model["sectionHeaderInfo"])
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
                    "any-key",
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
                    mapOf("testKey" to "testValue"),
                )
            val spiedOnPage = spy(page)

            val testJourney =
                TestJourney(
                    journeyType = JourneyType.LANDLORD_REGISTRATION,
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
                                        mapOf(),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepThree, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "templateName",
                                        mapOf("testKey" to "testValue"),
                                    ),
                            ),
                            Step(
                                TestStepId.StepThree,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "index",
                                        mapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepFour, null) },
                            ),
                            Step(
                                TestStepId.StepFour,
                                page = spiedOnPage,
                            ),
                        ),
                )
            val pageDataStepOne: PageData = mapOf("testProperty" to "testProperty1")
            val pageDataStepTwo: PageData = mapOf("testProperty" to "testProperty2")
            val pageDataStepThree: PageData = mapOf("testProperty" to "testProperty3")
            val pageDataStepFour: PageData = mapOf("testProperty" to "testProperty4")
            val journeyData: JourneyData =
                mapOf(
                    TestStepId.StepOne.urlPathSegment to pageDataStepOne,
                    TestStepId.StepTwo.urlPathSegment to pageDataStepTwo,
                    TestStepId.StepThree.urlPathSegment to pageDataStepThree,
                )
            val filteredJourneyData: JourneyData =
                mapOf(
                    TestStepId.StepOne.urlPathSegment to pageDataStepOne,
                    TestStepId.StepThree.urlPathSegment to pageDataStepThree,
                )

            whenever(spiedOnJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            testJourney.getModelAndViewForStep(
                TestStepId.StepFour.urlPathSegment,
                null,
                pageDataStepFour,
            )

            // Assert
            verify(spiedOnPage).getModelAndView(
                anyOrNull(),
                eq(TestStepId.StepThree.urlPathSegment),
                eq(filteredJourneyData),
                eq(null),
            )
        }
    }

    @Nested
    inner class GetModelAndViewForStepTests {
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
                                        mapOf("testKey" to "testValue"),
                                    ),
                            ),
                        ),
                )
            val pageData: PageData = mapOf()
            val principal = Principal { "testPrincipalId" }

            // Act and Assert
            val exception =
                assertThrows<ResponseStatusException> {
                    testJourney.completeStep(
                        TestStepId.StepTwo.urlPathSegment,
                        pageData,
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
                                        mapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mapOf()
            val journeyData: JourneyData =
                mapOf(TestStepId.StepOne.urlPathSegment to pageData)

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.completeStep(
                    TestStepId.StepOne.urlPathSegment,
                    pageData,
                    null,
                    principal,
                )

            // Assert
            assertIs<BindingResult>(result.model[BindingResult.MODEL_KEY_PREFIX + "formModel"])
            val bindingResult: BindingResult = result.model[BindingResult.MODEL_KEY_PREFIX + "formModel"] as BindingResult
            assertContains(result.model, "testKey")
            assertEquals("testValue", result.model["testKey"])
            val propertyValue = bindingResult.getRawFieldValue("testProperty")
            assertNull(propertyValue)
            assertEquals("stepOneTemplate", result.viewName)
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
                                        mapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.completeStep(
                    TestStepId.StepOne.urlPathSegment,
                    pageData,
                    null,
                    principal,
                )
            val journeyDataCaptor = argumentCaptor<JourneyData>()
            verify(mockJourneyDataService).addToJourneyDataIntoSession(journeyDataCaptor.capture())

            // Assert
            assertEquals("redirect:${TestStepId.StepTwo.urlPathSegment}", result.viewName)
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
                                        mapOf("testKey" to "testValue"),
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
                                        mapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession())
                .thenReturn(journeyData)
                .thenReturn(journeyData + (TestStepId.StepOne.urlPathSegment to pageData))

            // Act
            val result =
                testJourney.completeStep(
                    TestStepId.StepOne.urlPathSegment,
                    pageData,
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
            assertEquals("redirect:${TestStepId.StepTwo.urlPathSegment}", result.viewName)
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
                                        mapOf("testKey" to "testValue"),
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
                                        mapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.completeStep(
                    TestStepId.StepOne.urlPathSegment,
                    pageData,
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
            assertEquals("redirect:${TestStepId.StepTwo.urlPathSegment}", result.viewName)
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
                                        mapOf("testKey" to "testValue"),
                                    ),
                                handleSubmitAndRedirect = { _, _, _ -> "/customRedirect" },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.completeStep(
                    TestStepId.StepOne.urlPathSegment,
                    pageData,
                    null,
                    principal,
                )

            // Assert
            assertEquals("redirect:/customRedirect", result.viewName)
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
                                        mapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(null, null) },
                            ),
                        ),
                )
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mapOf()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act and Assert
            assertThrows<IllegalStateException> {
                testJourney.completeStep(
                    TestStepId.StepOne.urlPathSegment,
                    pageData,
                    null,
                    principal,
                )
            }
        }
    }

    @Nested
    inner class ChangingAnswersTests {
        @Test
        fun `getModelAndViewForStep returns back button to check your answers page when changing answers`() {
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
                                        mapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )

            val pageData: PageData = mapOf("testProperty" to "testPropertyValue")
            val journeyData: JourneyData = mapOf(TestStepId.StepOne.urlPathSegment to pageData)
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            val result =
                testJourney.getModelAndViewForStep(
                    TestStepId.StepTwo.urlPathSegment,
                    null,
                    null,
                    TestStepId.StepTwo.urlPathSegment,
                )

            // Assert
            assertContains(result.model, "backUrl")
            assertEquals(TestStepId.CYAStep.urlPathSegment, result.model["backUrl"])
        }

        @Test
        fun `completeStep redirects to check your answers page when changing answers`() {
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
                                        mapOf("testKey" to "testValue"),
                                    ),
                                nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                            ),
                            Step(
                                TestStepId.StepTwo,
                                page =
                                    Page(
                                        TestFormModel::class,
                                        "stepTwoTemplate",
                                        mapOf("anotherTestKey" to "anotherTestValue"),
                                    ),
                            ),
                        ),
                )
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mapOf("testProperty" to "testPropertyValue")

            // Act
            val result =
                testJourney.completeStep(
                    TestStepId.StepTwo.urlPathSegment,
                    pageData,
                    null,
                    principal,
                    TestStepId.StepTwo.urlPathSegment,
                )

            // Assert
            assertEquals("redirect:${TestStepId.CYAStep.urlPathSegment}", result.viewName)
        }
    }
}
