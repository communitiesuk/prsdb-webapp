package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgCharityState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberNorthernIrelandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberScotlandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class OrgCharityTask(
    journeyStateService: JourneyStateService,
    override val orgCharityStep: OrgIsRegisteredCharityStep,
    override val orgCharityRegisteredWithStep: OrgCharityRegisteredWithStep,
    override val orgCharityNumberEnglandAndWalesStep: OrgCharityNumberEnglandAndWalesStep,
    override val orgCharityNumberNorthernIrelandStep: OrgCharityNumberNorthernIrelandStep,
    override val orgCharityNumberScotlandStep: OrgCharityNumberScotlandStep,
) : DuplicableTask<OrgCharityState>(journeyStateService),
    OrgCharityState {
    override val taskState get() = this

    override fun makeSubJourney(state: OrgCharityState) =
        subJourney(state) {
            step(journey.orgCharityStep) {
                routeSegment(OrgIsRegisteredCharityStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.orgCharityRegisteredWithStep
                        YesOrNo.NO -> exitStep
                    }
                }
            }
            step(journey.orgCharityRegisteredWithStep) {
                routeSegment(OrgCharityRegisteredWithStep.ROUTE_SEGMENT)
                parents { journey.orgCharityStep.hasOutcome(YesOrNo.YES) }
                nextStep { mode ->
                    when (mode) {
                        CharityRegulator.ENGLAND_AND_WALES -> journey.orgCharityNumberEnglandAndWalesStep
                        CharityRegulator.NORTHERN_IRELAND -> journey.orgCharityNumberNorthernIrelandStep
                        CharityRegulator.SCOTLAND -> journey.orgCharityNumberScotlandStep
                        CharityRegulator.NONE -> exitStep
                    }
                }
            }
            step(journey.orgCharityNumberEnglandAndWalesStep) {
                routeSegment(OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT)
                parents { journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.ENGLAND_AND_WALES) }
                nextStep { exitStep }
            }
            step(journey.orgCharityNumberNorthernIrelandStep) {
                routeSegment(OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT)
                parents { journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.NORTHERN_IRELAND) }
                nextStep { exitStep }
            }
            step(journey.orgCharityNumberScotlandStep) {
                routeSegment(OrgCharityNumberScotlandStep.ROUTE_SEGMENT)
                parents { journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.SCOTLAND) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.orgCharityStep.hasOutcome(YesOrNo.NO),
                        journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.NONE),
                        journey.orgCharityNumberEnglandAndWalesStep.isComplete(),
                        journey.orgCharityNumberNorthernIrelandStep.isComplete(),
                        journey.orgCharityNumberScotlandStep.isComplete(),
                    )
                }
            }
        }
}
