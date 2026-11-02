package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.doesNotHaveOutcome
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OwnershipTypeState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class OwnershipTypeTask(
    journeyStateService: JourneyStateService,
    override val ownsPropertyStep: OwnsPropertyStep,
    override val ownershipKindStep: OwnershipKindStep,
    override val freeholdTypeStep: FreeholdTypeStep,
) : TaskWithoutDependencies<OwnershipTypeState>(journeyStateService),
    OwnershipTypeState {
    override val taskState get() = this

    override fun makeSubJourney(state: OwnershipTypeState) =
        subJourney(state) {
            step(journey.ownsPropertyStep) {
                routeSegment(OwnsPropertyStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.ownershipKindStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            step(journey.ownershipKindStep) {
                routeSegment(OwnershipKindStep.ROUTE_SEGMENT)
                parents { journey.ownsPropertyStep.hasOutcome(YesOrNo.YES) }
                nextStep { mode ->
                    when (mode) {
                        OwnershipKind.LEASEHOLD_ONLY -> exitStep
                        OwnershipKind.FREEHOLD_ONLY, OwnershipKind.FREEHOLD_AND_LEASEHOLD -> journey.freeholdTypeStep
                    }
                }
                savable()
            }
            step(journey.freeholdTypeStep) {
                routeSegment(FreeholdTypeStep.ROUTE_SEGMENT)
                parents { journey.ownershipKindStep.doesNotHaveOutcome(OwnershipKind.LEASEHOLD_ONLY) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.ownsPropertyStep.hasOutcome(YesOrNo.NO),
                        journey.ownershipKindStep.hasOutcome(OwnershipKind.LEASEHOLD_ONLY),
                        journey.freeholdTypeStep.isComplete(),
                    )
                }
            }
        }
}
