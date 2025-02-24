package uk.gov.communities.prsdb.webapp.forms.journeys

import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.forms.steps.Step

data class TestStepModel(
    val urlPathSegment: String,
    val isSatisfied: Boolean = true,
    val customNextActionAddition: ((JourneyData) -> Unit)? = null,
)

class TestIteratorBuilder {
    private var initialised = false
    private val steps: MutableList<TestStepModel> = mutableListOf()
    private val journeyData: JourneyData = mutableMapOf()
    private var missingFirstStep: Boolean = false
    private var onStep: Int? = null

    fun initialised(): TestIteratorBuilder {
        initialised = true
        return this
    }

    fun onStep(step: Int): TestIteratorBuilder {
        onStep = step
        return this
    }

    fun withMissingFirstStep(): TestIteratorBuilder {
        missingFirstStep = true
        return this
    }

    fun addStepToEnd(stepModel: TestStepModel): TestIteratorBuilder {
        journeyData[stepModel.urlPathSegment] = mapOf("urlPathSegment" to stepModel.urlPathSegment, "isSatisfied" to stepModel.isSatisfied)
        return addStepToEndWithoutJourneyData(stepModel)
    }

    fun addStepToEndWithoutJourneyData(stepModel: TestStepModel): TestIteratorBuilder {
        steps.add(stepModel)
        return this
    }

    fun build(): ReachableStepDetailsIterator<TestStepId> {
        val linkedSteps =
            steps.zipWithNext {
                    stepModel,
                    nextStepModel,
                ->
                testStep(stepModel.urlPathSegment, nextStepModel.urlPathSegment, stepModel.isSatisfied, stepModel.customNextActionAddition)
            } + testStep(steps.last().urlPathSegment, isSatisfied = steps.last().isSatisfied)

        val firstStepId =
            if (missingFirstStep) {
                // A concatenation of all strings in a list cannot be contained by the list
                TestStepId(steps.joinToString { it.urlPathSegment })
            } else {
                TestStepId(steps.first().urlPathSegment)
            }

        val iterator = ReachableStepDetailsIterator(journeyData, linkedSteps, firstStepId, mock())

        val currentOnStep = onStep
        if (currentOnStep != null) {
            repeat(currentOnStep) { iterator.next() }
        } else if (initialised) {
            iterator.next()
        }

        return iterator
    }

    private fun testStep(
        urlPathSegment: String,
        nextSegment: String? = null,
        isSatisfied: Boolean = true,
        customNextActionAddition: ((JourneyData) -> Unit)? = null,
    ) = Step(
        TestStepId(urlPathSegment),
        mock(),
        handleSubmitAndRedirect = null,
        isSatisfied = { _, _ -> isSatisfied },
        nextAction = { journeyData, _ ->
            customNextActionAddition?.invoke(journeyData)
            Pair(nextSegment?.let { TestStepId(it) }, null)
        },
    )

    fun getDataForStep(urlPathSegment: String): Any? = journeyData[urlPathSegment]

    fun clearJourneyData() = journeyData.clear()
}
