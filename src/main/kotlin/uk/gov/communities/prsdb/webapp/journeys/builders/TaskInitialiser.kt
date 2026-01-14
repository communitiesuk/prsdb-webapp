package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.Task.Companion.configureSavable
import uk.gov.communities.prsdb.webapp.journeys.TaskComplete
import uk.gov.communities.prsdb.webapp.journeys.TaskExitStep

class TaskInitialiser<TStateInit : JourneyState>(
    private val task: Task<TStateInit>,
    private val state: TStateInit,
    private val elementConfiguration: ElementConfiguration<TaskComplete> =
        ElementConfiguration("Task ${task::class.simpleName}}"),
) : ConfigurableElement<TaskComplete> by elementConfiguration,
    BuildableElement {
    override fun build(): List<JourneyStep<*, *, *>> {
        val nonNullDestinationProvider =
            elementConfiguration.nextDestinationProvider
                ?: throw JourneyInitialisationException("$initialiserName does not have a nextDestination defined")

        exitStepOverride?.let { task.setCustomExitStep(it) }

        val taskSubJourney =
            task.getTaskSubJourneyBuilder(state) {
                nextDestination(nonNullDestinationProvider)
            }

        taskSubJourney.configure {
            elementConfiguration.unreachableStepDestination?.let { unreachableStepDestinationIfNotSet(it) }
            elementConfiguration.additionalContentProviders.forEach { contentValueProvider ->
                withAdditionalContentProperty(contentValueProvider)
            }
        }
        taskSubJourney.configureFirst {
            elementConfiguration.backDestinationOverride?.let { backDestination(it) }
            parents(
                elementConfiguration.parentageProvider
                    ?: throw JourneyInitialisationException("$initialiserName does not have parentage defined"),
            )
        }
        if (elementConfiguration.shouldSaveProgress) {
            taskSubJourney.configureSavable {
                saveProgress()
            }
        }

        return taskSubJourney.build()
    }

    override fun configure(configuration: ConfigurableElement<*>.() -> Unit) = configuration()

    override fun configureFirst(configuration: ConfigurableElement<*>.() -> Unit) = configuration()

    var exitStepOverride: TaskExitStep? = null
        private set

    fun customExitStep(step: TaskExitStep) {
        if (exitStepOverride != null) {
            throw JourneyInitialisationException("Exit step has already been initialised")
        }
        exitStepOverride = step
    }
}
