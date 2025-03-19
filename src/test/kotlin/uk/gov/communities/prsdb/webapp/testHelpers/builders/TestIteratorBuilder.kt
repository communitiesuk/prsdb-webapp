package uk.gov.communities.prsdb.webapp.testHelpers.builders

import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.ReachableStepDetailsIterator
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId

data class TestStepId(
    override val urlPathSegment: String,
) : StepId

data class TestStepModel(
    val urlPathSegment: String,
    val isSatisfied: Boolean = false,
    val customReachableStepUrlSegments: List<String>? = null,
)

class TestIteratorBuilder {
    private var initialStepModel: TestStepModel? = null
    private val nonInitialStepModels: MutableList<TestStepModel> = mutableListOf()
    private var journeyData: JourneyData = emptyMap()
    private var onStep: Int = 0

    fun withInitialStep(stepModel: TestStepModel): TestIteratorBuilder {
        addStepDataToJourneyData(stepModel)
        initialStepModel = stepModel
        return this
    }

    fun withNextStep(stepModel: TestStepModel): TestIteratorBuilder {
        addStepDataToJourneyData(stepModel)
        nonInitialStepModels.add(stepModel)
        return this
    }

    fun onStep(step: Int): TestIteratorBuilder {
        onStep = step
        return this
    }

    fun build(): ReachableStepDetailsIterator<TestStepId> {
        val stepModels =
            mutableListOf<TestStepModel>().apply {
                initialStepModel?.let { add(initialStepModel!!) }
                addAll(nonInitialStepModels)
            }

        val steps =
            (
                stepModels.zipWithNext {
                        stepModel,
                        nextStepModel,
                    ->
                    testStep(
                        stepModel.urlPathSegment,
                        nextStepModel.urlPathSegment,
                        stepModel.isSatisfied,
                        stepModel.customReachableStepUrlSegments,
                    )
                } + stepModels.lastOrNull()?.let { testStep(it.urlPathSegment, isSatisfied = it.isSatisfied) }
            ).filterNotNull()

        // A prepended concatenation of all strings in a list cannot be contained by the list
        val testStepIdNotUsedByAnyStep = TestStepId(steps.joinToString(prefix = "_") { it.id.urlPathSegment })
        val initialStepId =
            steps.singleOrNull { it.id.urlPathSegment == initialStepModel?.urlPathSegment }?.id
                ?: testStepIdNotUsedByAnyStep

        val iterator = ReachableStepDetailsIterator(journeyData, steps, initialStepId, mock())
        repeat(onStep) { iterator.next() }
        return iterator
    }

    fun getDataForStep(urlPathSegment: String): Any? = journeyData[urlPathSegment]

    private fun addStepDataToJourneyData(stepModel: TestStepModel) {
        journeyData = journeyData +
            (stepModel.urlPathSegment to mapOf("urlPathSegment" to stepModel.urlPathSegment, "isSatisfied" to stepModel.isSatisfied))
    }

    private fun testStep(
        urlPathSegment: String,
        nextSegment: String? = null,
        isSatisfied: Boolean = true,
        customReachableStepSegments: List<String>? = null,
    ): Step<TestStepId> {
        val nextAction = Pair(nextSegment?.let { TestStepId(it) }, null)
        val defaultReachableAction = listOf(nextAction).filterIsInstance<Pair<TestStepId, Int?>>()
        val customReachableActions = customReachableStepSegments?.map { Pair(TestStepId(it), null) }

        return Step(
            TestStepId(urlPathSegment),
            mock(),
            handleSubmitAndRedirect = null,
            isSatisfied = { _, _ -> isSatisfied },
            nextAction = { _, _ -> nextAction },
            reachableActions = { _, _ -> customReachableActions ?: defaultReachableAction },
        )
    }
}
