package uk.gov.communities.prsdb.webapp.testHelpers.builders

import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyDataKey
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.ReachableStepDetailsIterator
import uk.gov.communities.prsdb.webapp.forms.TestStepId
import uk.gov.communities.prsdb.webapp.forms.pages.AbstractPage
import uk.gov.communities.prsdb.webapp.forms.steps.Step

data class TestStepModel(
    val urlPathSegment: String,
    val isSatisfied: Boolean = true,
    val customNextActionAddition: ((JourneyData) -> Unit)? = null,
)

class TestIteratorBuilder {
    private var initialised = false
    private val steps: MutableList<TestStepModel> = mutableListOf()
    private var journeyData: JourneyData = emptyMap()
    private var initialStepModel: TestStepModel? = null
    private var onStep: Int? = null

    fun initialised(): TestIteratorBuilder {
        initialised = true
        return this
    }

    fun onStep(step: Int): TestIteratorBuilder {
        onStep = step
        return this
    }

    fun withNonStepJourneyData(journeyDataKey: JourneyDataKey): TestIteratorBuilder {
        journeyData = journeyData + (journeyDataKey.key to journeyDataKey.key)
        return this
    }

    fun withFirstStep(stepModel: TestStepModel): TestIteratorBuilder {
        if (initialStepModel != null) {
            throw PrsdbWebException("This builder already has a first step set")
        }
        initialStepModel = stepModel
        journeyData =
            journeyData +
            (stepModel.urlPathSegment to mapOf("urlPathSegment" to stepModel.urlPathSegment, "isSatisfied" to stepModel.isSatisfied))
        return this
    }

    fun withNextStep(stepModel: TestStepModel): TestIteratorBuilder {
        journeyData =
            journeyData +
            (stepModel.urlPathSegment to mapOf("urlPathSegment" to stepModel.urlPathSegment, "isSatisfied" to stepModel.isSatisfied))
        return withNextStepWithoutPageData(stepModel)
    }

    fun withNextStepWithoutPageData(stepModel: TestStepModel): TestIteratorBuilder {
        steps.add(stepModel)
        return this
    }

    fun build(): ReachableStepDetailsIterator<TestStepId> {
        val linkedSteps =
            (
                steps.zipWithNext { stepModel, nextStepModel ->
                    testStep(
                        stepModel.urlPathSegment,
                        nextStepModel.urlPathSegment,
                        stepModel.isSatisfied,
                        stepModel.customNextActionAddition,
                    )
                } + steps.lastOrNull()?.let { testStep(it.urlPathSegment, isSatisfied = it.isSatisfied) }
            ).filterNotNull()

        val currentInitialStepModel = initialStepModel
        val iterator =
            if (currentInitialStepModel != null) {
                val initialStep =
                    testStep(
                        currentInitialStepModel.urlPathSegment,
                        nextSegment = steps.firstOrNull()?.urlPathSegment,
                        isSatisfied = currentInitialStepModel.isSatisfied,
                        customNextActionAddition = currentInitialStepModel.customNextActionAddition,
                    )
                ReachableStepDetailsIterator(journeyData, linkedSteps + initialStep, initialStep.id, mock())
            } else {
                // A concatenation of all strings in a list cannot be contained by the list
                val urlSegmentNotUsedByAnyStep = steps.joinToString { it.urlPathSegment }
                ReachableStepDetailsIterator(journeyData, linkedSteps, TestStepId(urlSegmentNotUsedByAnyStep), mock())
            }

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
    ): Step<TestStepId> {
        val mockPage: AbstractPage = mock()
        val mockBindingResult: BindingResult = mock()
        whenever(mockPage.bindDataToFormModel(anyOrNull(), anyOrNull())).thenReturn(mockBindingResult)
        return Step(
            TestStepId(urlPathSegment),
            mockPage,
            handleSubmitAndRedirect = null,
            isSatisfied = { _ -> isSatisfied },
            nextAction = { journeyData, _ ->
                customNextActionAddition?.invoke(journeyData)
                Pair(nextSegment?.let { TestStepId(it) }, null)
            },
        )
    }

    fun getDataForStep(urlPathSegment: String): Any? = journeyData[urlPathSegment]

    fun getDataForKey(key: JourneyDataKey): Any? = journeyData[key.key]
}
