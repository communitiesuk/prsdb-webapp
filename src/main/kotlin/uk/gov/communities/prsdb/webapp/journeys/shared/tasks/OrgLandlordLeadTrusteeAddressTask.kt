package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.doesNotHaveOutcome
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@JourneyFrameworkComponent
class OrgLandlordLeadTrusteeAddressTask : Task<LandlordRegistrationOrgLandlordState>() {
    override fun makeSubJourney(state: LandlordRegistrationOrgLandlordState) =
        subJourney(LeadTrusteeAddressStateAdapter(state)) {
            step(journey.lookupAddressStep) {
                routeSegment("lead-trustee-${LookupAddressStep.ROUTE_SEGMENT}")
                nextStep { mode ->
                    when (mode) {
                        LookupAddressMode.ADDRESSES_FOUND -> journey.selectAddressStep
                        LookupAddressMode.NO_ADDRESSES_FOUND -> journey.noAddressFoundStep
                    }
                }
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.lookupAddress.trusteeRegistration.fieldSetHeading",
                        "fieldSetHint" to "forms.lookupAddress.trusteeRegistration.fieldSetHint",
                    )
                }
            }
            step(journey.selectAddressStep) {
                routeSegment("lead-trustee-${SelectAddressStep.ROUTE_SEGMENT}")
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.ADDRESSES_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        SelectAddressMode.MANUAL_ADDRESS -> journey.manualAddressStep
                        else -> exitStep
                    }
                }
            }
            step(journey.noAddressFoundStep) {
                routeSegment("lead-trustee-${NoAddressFoundStep.ROUTE_SEGMENT}")
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND) }
                nextStep { journey.manualAddressStep }
            }
            step(journey.manualAddressStep) {
                routeSegment("lead-trustee-${ManualAddressStep.ROUTE_SEGMENT}")
                parents {
                    OrParents(
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.noAddressFoundStep.isComplete(),
                    )
                }
                nextStep { exitStep }
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.manualAddress.trusteeRegistration.fieldSetHeading",
                        "fieldSetHint" to null,
                    )
                }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.selectAddressStep.doesNotHaveOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.manualAddressStep.isComplete(),
                    )
                }
            }
        }
}

class LeadTrusteeAddressStateAdapter(
    private val delegateState: LandlordRegistrationOrgLandlordState,
) : AddressState,
    LandlordRegistrationOrgLandlordState by delegateState {
    override val lookupAddressStep: LookupAddressStep
        get() = delegateState.leadTrusteeLookupAddressStep
    override val selectAddressStep: SelectAddressStep
        get() = delegateState.leadTrusteeSelectAddressStep
    override val noAddressFoundStep: NoAddressFoundStep
        get() = delegateState.leadTrusteeNoAddressFoundStep
    override val manualAddressStep: ManualAddressStep
        get() = delegateState.leadTrusteeManualAddressStep
    override var cachedAddresses: List<AddressDataModel>?
        get() = delegateState.leadTrusteeCachedAddresses
        set(value) {
            delegateState.leadTrusteeCachedAddresses = value
        }
    override var isAddressAlreadyRegistered: Boolean?
        get() = delegateState.leadTrusteeIsAddressAlreadyRegistered
        set(value) {
            delegateState.leadTrusteeIsAddressAlreadyRegistered = value
        }
    override var cachedSelectedAddress: String?
        get() = delegateState.leadTrusteeCachedSelectedAddress
        set(value) {
            delegateState.leadTrusteeCachedSelectedAddress = value
        }
}
