package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.validation.Validator
import kotlin.reflect.KClass

/**
 * A Step is a definition of one step along the flow of a multi-page form. The Step class deals only with concerns relating to that flow.
 */
sealed class Step<TStepId : StepId>(
    val nextStepActions: List<StepAction<TStepId, out StepActionTarget<TStepId>>>,
) {
    abstract fun isSatisfied(journeyData: JourneyData): Boolean

    class StandardStep<TStepId : StepId, TPageForm : FormModel<TPageForm>>(
        val stepId: TStepId,
        val page: Page<TPageForm>,
        val persistAfterSubmit: Boolean = false,
        val allowRepeats: Boolean = false,
        nextStepActions: List<StepAction<TStepId, out StepActionTarget<TStepId>>>,
        private val isSatisfiedOverride: ((JourneyData) -> Boolean)? = null,
    ) : Step<TStepId>(nextStepActions) {
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
        nextStepActions: List<StepAction<TStepId, out StepActionTarget<TStepId>>>,
    ) : Step<TStepId>(nextStepActions) {
        override fun isSatisfied(journeyData: JourneyData): Boolean = true
    }
}

sealed class StepActionTarget<TStepId : StepId> {
    data class Step<TStepId : StepId>(
        val stepId: TStepId,
    ) : StepActionTarget<TStepId>()

    data class Path<TStepId : StepId>(
        val path: String,
    ) : StepActionTarget<TStepId>()
}

sealed class StepAction<TStepId : StepId, TTarget : StepActionTarget<TStepId>>(
    val target: TTarget,
) {
    class Unconditional<TStepId : StepId>(
        target: StepActionTarget<TStepId>,
    ) : StepAction<TStepId, StepActionTarget<TStepId>>(target)

    class SavedFormsCondition<TStepId : StepId>(
        target: StepActionTarget.Step<TStepId>,
        val condition: (List<Map<String, String>>) -> Boolean,
    ) : StepAction<TStepId, StepActionTarget.Step<TStepId>>(target)

    class UserActionCondition<TStepId : StepId>(
        target: StepActionTarget<TStepId>,
        val condition: (String) -> Boolean,
    ) : StepAction<TStepId, StepActionTarget<TStepId>>(target)
}

class NextStepBuilder<TStepId : StepId> {
    private val nextStepActions: MutableList<StepAction<TStepId, out StepActionTarget<TStepId>>> = mutableListOf()

    fun ifSavedForms(
        stepId: TStepId,
        condition: (List<Map<String, String>>) -> Boolean,
    ) {
        val target = StepActionTarget.Step(stepId)
        nextStepActions.add(StepAction.SavedFormsCondition(target, condition))
    }

    fun ifUserAction(
        stepId: TStepId,
        condition: (String) -> Boolean,
    ) {
        val target = StepActionTarget.Step(stepId)
        nextStepActions.add(StepAction.UserActionCondition(target, condition))
    }

    fun default(stepId: TStepId) {
        val target = StepActionTarget.Step(stepId)
        nextStepActions.add(StepAction.Unconditional(target))
    }

    fun default(path: String) {
        val target = StepActionTarget.Path<TStepId>(path)
        nextStepActions.add(StepAction.Unconditional(target))
    }

    fun build() = nextStepActions
}

abstract class StepBuilder<TStepId : StepId> {
    protected lateinit var nextStepActions: List<StepAction<TStepId, out StepActionTarget<TStepId>>>

    fun nextStep(stepId: TStepId) {
        val target = StepActionTarget.Step(stepId)
        nextStepActions = listOf(StepAction.Unconditional(target))
    }

    fun nextStep(path: String) {
        val target = StepActionTarget.Path<TStepId>(path)
        nextStepActions = listOf(StepAction.Unconditional(target))
    }

    fun nextStep(init: NextStepBuilder<TStepId>.() -> Unit) {
        nextStepActions = NextStepBuilder<TStepId>().apply(init).build()
    }
}

class InterstitialStepBuilder<TStepId : StepId> : StepBuilder<TStepId>() {
    fun build() = Step.InterstitialStep(nextStepActions)
}

class StandardStepBuilder<TStepId : StepId>(
    private val validator: Validator,
    private val stepId: TStepId,
) : StepBuilder<TStepId>() {
    private var page: Page<*>? = null
    var allowRepeats: Boolean = false

    fun <TPageForm : FormModel<TPageForm>> page(
        pageFormClass: KClass<TPageForm>,
        init: PageBuilder<TPageForm>.() -> Unit,
    ) {
        page = PageBuilder(pageFormClass, validator).apply(init).build()
    }

    fun build() = Step.StandardStep(page = page!!, nextStepActions = nextStepActions, allowRepeats = allowRepeats, stepId = stepId)
}
