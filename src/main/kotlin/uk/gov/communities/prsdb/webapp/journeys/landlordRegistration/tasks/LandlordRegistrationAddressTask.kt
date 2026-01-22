package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.notHasOutcome
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

@JourneyFrameworkComponent
class LandlordRegistrationAddressTask : Task<AddressState>() {
    override fun makeSubJourney(state: AddressState) =
        subJourney(state) {
            step(journey.lookupAddressStep) {
                routeSegment(LookupAddressStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        LookupAddressMode.ADDRESSES_FOUND -> journey.selectAddressStep
                        LookupAddressMode.NO_ADDRESSES_FOUND -> journey.noAddressFoundStep
                    }
                }
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.lookupAddress.landlordRegistration.fieldSetHeading",
                        "fieldSetHint" to "forms.lookupAddress.landlordRegistration.fieldSetHint",
                    )
                }
            }
            step(journey.selectAddressStep) {
                routeSegment(SelectAddressStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.ADDRESSES_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        SelectAddressMode.MANUAL_ADDRESS -> journey.manualAddressStep
                        else -> exitStep
                    }
                }
            }
            step(journey.noAddressFoundStep) {
                routeSegment(NoAddressFoundStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND) }
                nextStep { journey.manualAddressStep }
            }
            step(journey.manualAddressStep) {
                routeSegment(ManualAddressStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.noAddressFoundStep.isComplete(),
                    )
                }
                nextStep { exitStep }
                withAdditionalContentProperty { "fieldSetHeading" to "forms.manualAddress.landlordRegistration.fieldSetHeading" }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.selectAddressStep.notHasOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.manualAddressStep.isComplete(),
                    )
                }
            }
        }
}
