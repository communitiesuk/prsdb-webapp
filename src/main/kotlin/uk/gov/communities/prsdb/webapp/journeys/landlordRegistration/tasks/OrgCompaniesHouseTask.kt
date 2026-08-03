package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgCompaniesHouseState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class OrgCompaniesHouseTask(
    journeyStateService: JourneyStateService,
    override val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep,
    override val orgCompanyNumberStep: OrgCompanyNumberStep,
) : DuplicableTask<OrgCompaniesHouseState>(journeyStateService),
    OrgCompaniesHouseState {
    override val taskState get() = this

    override fun makeSubJourney(state: OrgCompaniesHouseState) =
        subJourney(state) {
            step(journey.orgIsRegisteredCompanyStep) {
                routeSegment(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.orgCompanyNumberStep
                        YesOrNo.NO -> exitStep
                    }
                }
            }
            step(journey.orgCompanyNumberStep) {
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                parents { journey.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.YES) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.NO),
                        journey.orgCompanyNumberStep.isComplete(),
                    )
                }
            }
        }
}
