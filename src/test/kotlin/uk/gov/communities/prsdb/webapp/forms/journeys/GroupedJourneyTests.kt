package uk.gov.communities.prsdb.webapp.forms.journeys

import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.GroupedStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal
import kotlin.test.assertEquals

class GroupedJourneyTests {
    private lateinit var validator: SpringValidatorAdapter
    private lateinit var mockJourneyDataService: JourneyDataService

    enum class TestGroupedStepId(
        override val urlPathSegment: String,
        override val groupIdentifier: GroupIdentifier,
    ) : GroupedStepId<GroupIdentifier> {
        GroupOneStepOne("group1-step1", GroupIdentifier.GroupOne),
        GroupOneStepTwo("group1-step2", GroupIdentifier.GroupOne),
        GroupTwoStepOne("group2-step1", GroupIdentifier.GroupTwo),
        CYAStep("check-your-answers", GroupIdentifier.Cya),
    }

    enum class GroupIdentifier {
        GroupOne,
        GroupTwo,
        Cya,
    }

    class TestFormModel : FormModel {
        @NotNull
        var testProperty: String? = null
    }

    class TestGroupedJourney(
        journeyType: JourneyType,
        initialStepId: TestGroupedStepId,
        validator: SpringValidatorAdapter,
        journeyDataService: JourneyDataService,
        steps: Set<Step<TestGroupedStepId>>,
    ) : Journey<TestGroupedStepId>(journeyType, initialStepId, validator, journeyDataService) {
        override val checkYourAnswersStepId = TestGroupedStepId.CYAStep
        override val sections = createSingleSectionWithSingleTaskFromSteps(initialStepId, steps)

        override val stepRouter = GroupedStepRouter(this)
    }

    private lateinit var validatorFactory: ValidatorFactory

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
    inner class ChangingAnswersTests {
        @Test
        fun `completeStep redirects to next step with same changingAnswerFor when within group`() {
            // Arrange
            val groupedJourney =
                TestGroupedJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    TestGroupedStepId.GroupOneStepOne,
                    validator,
                    mockJourneyDataService,
                    steps =
                        setOf(
                            Step(
                                TestGroupedStepId.GroupOneStepOne,
                                page = Page(TestFormModel::class, "index", mapOf()),
                                nextAction = { _, _ -> Pair(TestGroupedStepId.GroupOneStepTwo, null) },
                            ),
                            Step(
                                TestGroupedStepId.GroupOneStepTwo,
                                page = Page(TestFormModel::class, "index", mapOf()),
                            ),
                        ),
                )
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mapOf("testProperty" to "testValue")

            // Act
            val result =
                groupedJourney.completeStep(
                    TestGroupedStepId.GroupOneStepOne.urlPathSegment,
                    pageData,
                    null,
                    principal,
                    TestGroupedStepId.GroupOneStepOne.urlPathSegment,
                )

            // Assert
            assertEquals(
                "redirect:${TestGroupedStepId.GroupOneStepTwo.urlPathSegment}?" +
                    "changingAnswerFor=${TestGroupedStepId.GroupOneStepOne.urlPathSegment}",
                result.viewName,
            )
        }

        @Test
        fun `completeStep redirects to check your answers page if leaving group`() {
            // Arrange
            val groupedJourney =
                TestGroupedJourney(
                    JourneyType.LANDLORD_REGISTRATION,
                    TestGroupedStepId.GroupOneStepOne,
                    validator,
                    mockJourneyDataService,
                    steps =
                        setOf(
                            Step(
                                TestGroupedStepId.GroupOneStepOne,
                                page = Page(TestFormModel::class, "index", mapOf()),
                                nextAction = { _, _ -> Pair(TestGroupedStepId.GroupTwoStepOne, null) },
                            ),
                            Step(
                                TestGroupedStepId.GroupTwoStepOne,
                                page = Page(TestFormModel::class, "index", mapOf()),
                            ),
                        ),
                )
            val principal = Principal { "testPrincipalId" }
            val pageData: PageData = mapOf("testProperty" to "testValue")

            // Act
            val result =
                groupedJourney.completeStep(
                    TestGroupedStepId.GroupOneStepOne.urlPathSegment,
                    pageData,
                    null,
                    principal,
                    TestGroupedStepId.GroupOneStepOne.urlPathSegment,
                )

            // Assert
            assertEquals(
                "redirect:${TestGroupedStepId.CYAStep.urlPathSegment}",
                result.viewName,
            )
        }
    }
}
