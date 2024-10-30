package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.validation.Validator
import kotlin.reflect.KClass

/**
 * A Step is a definition of one step along the flow of a multi-page form. The Step class deals only with concerns relating to that flow.
 */
sealed class Step<TStepId : StepId>(
    val nextStepAction: StepAction<TStepId>,
) {
    abstract fun isSatisfied(journeyData: JourneyData): Boolean

    class StandardStep<TStepId : StepId, TPageForm : FormModel<TPageForm>>(
        val stepId: TStepId,
        val page: Page<TPageForm>,
        val persistAfterSubmit: Boolean = false,
        val allowRepeats: Boolean = false,
        nextStepAction: StepAction<TStepId>,
        private val isSatisfiedOverride: ((JourneyData) -> Boolean)? = null,
    ) : Step<TStepId>(nextStepAction) {
        val bindJourneyDataToModel: (JourneyData, Int?) -> PageModel<TPageForm> =
            { journeyData, entityIndex ->
                val formDataList = getFormDataList(journeyData)
                val index = getEntityIndex(entityIndex, journeyData)
                val formData = formDataList.getOrNull(index)
                page.bindFormDataToModel(formData)
            }

        val updateJourneyData: (
            JourneyData,
            Map<String, String>,
            Int?,
        ) -> Unit =
            { journeyData, formDataMap, entityIndex ->
                val formDataList = getFormDataList(journeyData)
                val index = getEntityIndex(entityIndex, journeyData)
                formDataList.add(index, formDataMap)
            }

        private fun getFormDataList(journeyData: JourneyData): MutableList<Map<String, String>> =
            journeyData.getOrPut(stepId.urlPathSegment, {
                mutableListOf()
            })

        private fun getEntityIndex(
            passedIndex: Int?,
            journeyData: JourneyData,
        ): Int {
            val formDataList = getFormDataList(journeyData)
            return if (allowRepeats) {
                passedIndex ?: formDataList.size
            } else {
                0
            }
        }

        override fun isSatisfied(journeyData: JourneyData): Boolean =
            isSatisfiedOverride?.invoke(journeyData) ?: !bindJourneyDataToModel(journeyData, 0).hasErrors()
    }

    class InterstitialStep<TStepId : StepId>(
        nextStepAction: StepAction<TStepId>,
    ) : Step<TStepId>(nextStepAction) {
        override fun isSatisfied(journeyData: JourneyData): Boolean = true
    }
}

sealed class StepAction<TStepId : StepId> {
    data class GoToStep<TStepId : StepId>(
        val stepId: TStepId,
    ) : StepAction<TStepId>()

    data class Redirect<TStepId : StepId>(
        val path: String,
    ) : StepAction<TStepId>()

    data class GoToOrLoop<TStepId : StepId>(
        val nextId: TStepId,
        val loopId: TStepId,
    ) : StepAction<TStepId>()

    data class RedirectOrLoop<TStepId : StepId>(
        val path: String,
        val loopId: TStepId,
    ) : StepAction<TStepId>()
}

class StepBuilder<TStepId : StepId>(
    private val validator: Validator,
    private val stepId: TStepId,
) {
    private var page: Page<*>? = null
    private var nextStepAction: StepAction<TStepId>? = null
    var allowRepeats: Boolean = false

    fun <TPageForm : FormModel<TPageForm>> page(
        pageFormClass: KClass<TPageForm>,
        init: PageBuilder<TPageForm>.() -> Unit,
    ) {
        page = PageBuilder(pageFormClass, validator).apply(init).build()
    }

    fun goToStep(stepId: TStepId) {
        nextStepAction = StepAction.GoToStep(stepId)
    }

    fun redirect(path: String) {
        nextStepAction = StepAction.Redirect(path)
    }

    fun goToOrLoop(
        nextId: TStepId,
        loopId: TStepId,
    ) {
        nextStepAction = StepAction.GoToOrLoop(nextId, loopId)
    }

    fun redirectOrLoop(
        nextPath: String,
        loopId: TStepId,
    ) {
        nextStepAction = StepAction.RedirectOrLoop(nextPath, loopId)
    }

    fun build() = Step.StandardStep(page = page!!, nextStepAction = nextStepAction!!, allowRepeats = allowRepeats, stepId = stepId)
}
