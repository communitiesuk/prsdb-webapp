package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.JointLandlordsPropertyRegistrationStrategy
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OwnershipAndLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep

@JourneyFrameworkComponent
class OwnershipAndLandlordsTask(
    private val jointLandlordsStrategy: JointLandlordsPropertyRegistrationStrategy,
) : Task<OwnershipAndLandlordsState>() {
    override fun makeSubJourney(state: OwnershipAndLandlordsState) =
        subJourney(state) {
            step(journey.ownershipTypeStep) {
                routeSegment(OwnershipTypeStep.ROUTE_SEGMENT)
                nextStep {
                    jointLandlordsStrategy.ifEnabledOrElse {
                        ifEnabled { journey.jointLandlordsTask.firstStep }
                        ifDisabled { exitStep }
                    }
                }
                savable()
            }
            jointLandlordsStrategy.ifEnabled {
                task(journey.jointLandlordsTask) {
                    parents { journey.ownershipTypeStep.isComplete() }
                    nextStep { exitStep }
                    savable()
                }
            }
            exitStep {
                parents {
                    jointLandlordsStrategy.ifEnabledOrElse {
                        ifEnabled { journey.jointLandlordsTask.isComplete() }
                        ifDisabled { journey.ownershipTypeStep.isComplete() }
                    }
                }
            }
        }
}
