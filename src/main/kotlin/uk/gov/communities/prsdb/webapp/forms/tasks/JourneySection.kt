package uk.gov.communities.prsdb.webapp.forms.tasks

import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId

class JourneySection<T : StepId>(
    val tasks: List<JourneyTask<T>>,
    val headingKey: String? = null,
) {
    fun isStepInSection(stepId: T): Boolean = stepId in tasks.flatMap { task -> task.steps.map { step -> step.id } }

    companion object {
        fun <T : StepId> withOneTask(
            task: JourneyTask<T>,
            headingKey: String? = null,
        ) = JourneySection(listOf(task), headingKey)

        fun <T : StepId> withOneStep(step: Step<T>) = JourneySection(listOf(JourneyTask.withOneStep(step)))
    }
}
