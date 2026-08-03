package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTaskWithDependencies
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideLicensingLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep

@JourneyFrameworkComponent
class LicensingTask(
    journeyStateService: JourneyStateService,
    override val licensingTypeStep: LicensingTypeStep,
    override val selectiveLicenceStep: SelectiveLicenceStep,
    override val hmoMandatoryLicenceStep: HmoMandatoryLicenceStep,
    override val hmoAdditionalLicenceStep: HmoAdditionalLicenceStep,
    override val provideLicensingLaterStep: ProvideLicensingLaterStep,
) : DuplicableTaskWithDependencies<LicensingState, LicensingDependencies>(journeyStateService),
    LicensingState {
    override val taskState get() = this

    override val isOccupied: Boolean?
        get() = dependencies.isOccupied
    override val allowProvideLicensingLaterRoute: Boolean
        get() = dependencies.allowProvideLicensingLaterRoute

    override fun makeSubJourney(state: LicensingState) =
        subJourney(state) {
            step(journey.licensingTypeStep) {
                routeSegment(LicensingTypeStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        LicensingTypeMode.SELECTIVE_LICENCE -> journey.selectiveLicenceStep
                        LicensingTypeMode.HMO_MANDATORY_LICENCE -> journey.hmoMandatoryLicenceStep
                        LicensingTypeMode.HMO_ADDITIONAL_LICENCE -> journey.hmoAdditionalLicenceStep
                        LicensingTypeMode.NO_LICENSING -> exitStep
                        LicensingTypeMode.PROVIDE_LATER -> journey.provideLicensingLaterStep
                    }
                }
                savable()
            }
            step(journey.selectiveLicenceStep) {
                routeSegment(SelectiveLicenceStep.ROUTE_SEGMENT)
                parents { journey.licensingTypeStep.hasOutcome(LicensingTypeMode.SELECTIVE_LICENCE) }
                nextStep { exitStep }
                savable()
            }
            step(journey.hmoMandatoryLicenceStep) {
                routeSegment(HmoMandatoryLicenceStep.ROUTE_SEGMENT)
                parents { journey.licensingTypeStep.hasOutcome(LicensingTypeMode.HMO_MANDATORY_LICENCE) }
                nextStep { exitStep }
                savable()
            }
            step(journey.hmoAdditionalLicenceStep) {
                routeSegment(HmoAdditionalLicenceStep.ROUTE_SEGMENT)
                parents { journey.licensingTypeStep.hasOutcome(LicensingTypeMode.HMO_ADDITIONAL_LICENCE) }
                nextStep { exitStep }
                savable()
            }
            step(journey.provideLicensingLaterStep) {
                routeSegment(ProvideLicensingLaterStep.ROUTE_SEGMENT)
                parents { journey.licensingTypeStep.hasOutcome(LicensingTypeMode.PROVIDE_LATER) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.licensingTypeStep.hasOutcome(LicensingTypeMode.NO_LICENSING),
                        journey.selectiveLicenceStep.isComplete(),
                        journey.hmoMandatoryLicenceStep.isComplete(),
                        journey.hmoAdditionalLicenceStep.isComplete(),
                        journey.provideLicensingLaterStep.isComplete(),
                    )
                }
            }
        }
}

interface LicensingDependencies {
    val isOccupied: Boolean?
    val allowProvideLicensingLaterRoute: Boolean
}
