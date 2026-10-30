package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyManagerState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckPropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemovePropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class PropertyManagerTask(
    journeyStateService: JourneyStateService,
    override val propertyManagerStep: PropertyManagerStep,
    override val propertyManagerDetailsStep: PropertyManagerDetailsStep,
    override val checkPropertyManagerStep: CheckPropertyManagerStep,
    override val removePropertyManagerStep: RemovePropertyManagerStep,
) : TaskWithoutDependencies<PropertyManagerState>(journeyStateService),
    PropertyManagerState {
    override val taskState get() = this

    override fun makeSubJourney(state: PropertyManagerState) =
        subJourney(state) {
            step(journey.propertyManagerStep) {
                routeSegment(PropertyManagerStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.propertyManagerDetailsStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            step(journey.propertyManagerDetailsStep) {
                routeSegment(PropertyManagerDetailsStep.ROUTE_SEGMENT)
                parents { journey.propertyManagerStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.checkPropertyManagerStep }
                savable()
            }
            step(journey.checkPropertyManagerStep) {
                routeSegment(CheckPropertyManagerStep.ROUTE_SEGMENT)
                parents { journey.propertyManagerDetailsStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            step(journey.removePropertyManagerStep) {
                routeSegment(RemovePropertyManagerStep.ROUTE_SEGMENT)
                parents { journey.propertyManagerDetailsStep.isComplete() }
                backStep { journey.checkPropertyManagerStep }
                nextStep { mode ->
                    if (mode == YesOrNo.YES) journey.propertyManagerStep else journey.checkPropertyManagerStep
                }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.checkPropertyManagerStep.isComplete(),
                        journey.propertyManagerStep.hasOutcome(YesOrNo.NO),
                    )
                }
            }
        }
}
