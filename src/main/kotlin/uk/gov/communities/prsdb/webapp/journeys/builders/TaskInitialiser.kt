package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationComplete
import uk.gov.communities.prsdb.webapp.journeys.Task

class TaskInitialiser<TStateInit : JourneyState>(
    private val task: Task<TStateInit>,
    private val state: TStateInit,
    private val elementConfiguration: ElementConfiguration<NavigationComplete> =
        ElementConfiguration("Task ${task::class.simpleName}}"),
) : ConfigurableElement<NavigationComplete> by elementConfiguration,
    BuildableElement {
    override fun build(): List<JourneyStep<*, *, *>> {
        val nonNullDestinationProvider =
            elementConfiguration.nextDestinationProvider
                ?: throw JourneyInitialisationException("$initialiserName does not have a nextDestination defined")
        val taskParentage =
            elementConfiguration.parentageProvider?.invoke()
                ?: throw JourneyInitialisationException("$initialiserName does not have parentage defined")

        val taskSubJourney =
            task.getTaskSubJourneyBuilder(state, taskParentage) {
                nextDestination(nonNullDestinationProvider)
            }
        taskSubJourney.configure {
            elementConfiguration.unreachableStepDestination?.let { unreachableStepDestinationIfNotSet(it) }
            elementConfiguration.additionalContentProviders.forEach { contentValueProvider ->
                withAdditionalContentProperty(contentValueProvider)
            }
        }

        return taskSubJourney.build()
    }

    override fun configure(configuration: ConfigurableElement<*>.() -> Unit) = configuration()
}
