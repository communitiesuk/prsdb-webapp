package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.CHECKING_ANSWERS_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.GroupedUpdateStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class GroupedUpdateJourneyTests {
    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

    enum class TestGroupedUpdateStepId(
        override val urlPathSegment: String,
        override val groupIdentifier: GroupIdentifier,
        override val isCheckYourAnswersStepId: Boolean = false,
    ) : GroupedUpdateStepId<GroupIdentifier> {
        GroupOneStepOne("group1-step1", GroupIdentifier.GroupOne),
        GroupOneStepTwo("group1-step2", GroupIdentifier.GroupOne),
        GroupOneCyaStep("group1-cya", GroupIdentifier.GroupOne, true),
        GroupTwoCyaStep("group2-cya", GroupIdentifier.GroupTwo, true),
    }

    enum class GroupIdentifier {
        GroupOne,
        GroupTwo,
    }

    class TestGroupedUpdateJourney(
        journeyDataService: JourneyDataService,
        currentStep: TestGroupedUpdateStepId,
    ) : GroupedUpdateJourney<TestGroupedUpdateStepId>(
            JourneyType.PROPERTY_DETAILS_UPDATE,
            initialStepId = TestGroupedUpdateStepId.GroupOneStepOne,
            AlwaysTrueValidator(),
            journeyDataService,
            currentStep.urlPathSegment,
            isCheckingAnswers = true,
        ) {
        override val sections =
            createSingleSectionWithSingleTaskFromSteps(
                currentStep,
                setOf(
                    Step(
                        TestGroupedUpdateStepId.GroupOneStepOne,
                        page = Page(NoInputFormModel::class, "templateName", emptyMap()),
                        nextAction = { _, _ -> Pair(TestGroupedUpdateStepId.GroupOneStepTwo, null) },
                    ),
                    Step(
                        TestGroupedUpdateStepId.GroupOneStepTwo,
                        page = Page(NoInputFormModel::class, "templateName", emptyMap()),
                        nextAction = { _, _ -> Pair(TestGroupedUpdateStepId.GroupOneCyaStep, null) },
                    ),
                    Step(
                        TestGroupedUpdateStepId.GroupOneCyaStep,
                        page = Page(NoInputFormModel::class, "templateName", emptyMap()),
                        nextAction = { _, _ -> Pair(TestGroupedUpdateStepId.GroupTwoCyaStep, null) },
                    ),
                    Step(
                        TestGroupedUpdateStepId.GroupTwoCyaStep,
                        page = Page(NoInputFormModel::class, "templateName", emptyMap()),
                    ),
                ),
            )

        override val stepRouter = GroupedUpdateStepRouter(this)

        override val unreachableStepRedirect: String = ""

        override fun createOriginalJourneyData(): JourneyData = emptyMap()
    }

    @Nested
    inner class CheckingAnswersTests {
        @Test
        fun `completeStep redirects to next step with same checkingAnswersFor when not reaching end of group`() {
            // Arrange
            val groupedUpdateJourney = TestGroupedUpdateJourney(mockJourneyDataService, TestGroupedUpdateStepId.GroupOneStepOne)

            // Act
            val result =
                groupedUpdateJourney
                    .completeStep(
                        formData = emptyMap(),
                        principal = { "testPrincipalId" },
                        checkingAnswersForStep = TestGroupedUpdateStepId.GroupOneStepOne.urlPathSegment,
                    ).viewName!!

            // Assert
            assertEquals(TestGroupedUpdateStepId.GroupOneStepTwo.urlPathSegment, result.getPath())
            assertEquals(TestGroupedUpdateStepId.GroupOneStepOne.urlPathSegment, result.getQueryParam(CHECKING_ANSWERS_FOR_PARAMETER_NAME))
        }

        @Test
        fun `completeStep redirects to the corresponding CYA step when reaching end of group`() {
            // Arrange
            val groupedUpdateJourney = TestGroupedUpdateJourney(mockJourneyDataService, TestGroupedUpdateStepId.GroupOneStepTwo)

            // Act
            val result =
                groupedUpdateJourney
                    .completeStep(
                        formData = emptyMap(),
                        principal = { "testPrincipalId" },
                        checkingAnswersForStep = TestGroupedUpdateStepId.GroupOneStepOne.urlPathSegment,
                    ).viewName!!

            // Assert
            assertEquals(TestGroupedUpdateStepId.GroupOneCyaStep.urlPathSegment, result.getPath())
            assertNull(result.getQueryParam(CHECKING_ANSWERS_FOR_PARAMETER_NAME))
        }

        @Test
        fun `getModelAndViewForStep yields a back link to the previous step with same checkingAnswersFor if not at start of group`() {
            // Arrange
            whenever(mockJourneyDataService.getJourneyDataFromSession())
                .thenReturn(mapOf(TestGroupedUpdateStepId.GroupOneStepOne.urlPathSegment to emptyMap<String, Any>()))
            val groupedUpdateJourney = TestGroupedUpdateJourney(mockJourneyDataService, TestGroupedUpdateStepId.GroupOneStepTwo)

            // Act
            val result =
                groupedUpdateJourney
                    .getModelAndViewForStep(
                        submittedPageData = null,
                        checkingAnswersForStep = TestGroupedUpdateStepId.GroupOneStepOne.urlPathSegment,
                    ).model[BACK_URL_ATTR_NAME]
                    .toString()

            // Assert
            assertEquals(TestGroupedUpdateStepId.GroupOneStepOne.urlPathSegment, result.getPath())
            assertEquals(TestGroupedUpdateStepId.GroupOneStepOne.urlPathSegment, result.getQueryParam(CHECKING_ANSWERS_FOR_PARAMETER_NAME))
        }

        @Test
        fun `getModelAndViewForStep yields a back link to the corresponding CYA step if at start of group`() {
            // Arrange
            val groupedUpdateJourney = TestGroupedUpdateJourney(mockJourneyDataService, TestGroupedUpdateStepId.GroupOneStepOne)

            // Act
            val result =
                groupedUpdateJourney
                    .getModelAndViewForStep(
                        submittedPageData = null,
                        checkingAnswersForStep = TestGroupedUpdateStepId.GroupOneStepOne.urlPathSegment,
                    ).model[BACK_URL_ATTR_NAME]
                    .toString()

            // Assert
            assertEquals(TestGroupedUpdateStepId.GroupOneCyaStep.urlPathSegment, result.getPath())
            assertNull(result.getQueryParam(CHECKING_ANSWERS_FOR_PARAMETER_NAME))
        }
    }

    companion object {
        private fun String.getUriComponents() = UriComponentsBuilder.fromUriString(this.removePrefix("redirect:")).build()

        private fun String.getPath() = this.getUriComponents().path

        private fun String.getQueryParam(name: String) = this.getUriComponents().queryParams.getFirst(name)
    }
}
