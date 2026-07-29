package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.TenancyDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class TenancyDetailsTask(
    journeyStateService: JourneyStateService,
    override val householdsAndTenantsTask: HouseholdsAndTenantsTask,
    override val rentIncludesBillsTask: RentIncludesBillsTask,
    override val furnishedStatus: FurnishedStatusStep,
    override val rentFrequencyAndAmountTask: RentFrequencyAndAmountTask,
) : DuplicableTask<TenancyDetailsState>(journeyStateService),
    TenancyDetailsState {
    override val taskState get() = this

    override fun makeSubJourney(state: TenancyDetailsState) =
        subJourney(state) {
            duplicableTask(journey.householdsAndTenantsTask) {
                withDependencies { HouseHoldsAndTenantsDependencies(true) }
                nextStep { journey.rentIncludesBillsTask.firstStep }
                savable()
            }
            duplicableTask(journey.rentIncludesBillsTask) {
                parents { journey.householdsAndTenantsTask.isComplete() }
                nextStep { journey.furnishedStatus }
                savable()
            }
            step(journey.furnishedStatus) {
                routeSegment(FurnishedStatusStep.ROUTE_SEGMENT)
                parents { journey.rentIncludesBillsTask.isComplete() }
                nextStep { journey.rentFrequencyAndAmountTask.firstStep }
                savable()
            }
            duplicableTask(journey.rentFrequencyAndAmountTask) {
                parents { journey.furnishedStatus.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                savable()
                parents { journey.rentFrequencyAndAmountTask.isComplete() }
            }
        }
}
