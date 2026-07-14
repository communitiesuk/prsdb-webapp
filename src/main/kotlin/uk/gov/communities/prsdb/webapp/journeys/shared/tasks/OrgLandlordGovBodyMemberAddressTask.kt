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
class OrgLandlordGovBodyMemberAddressTask : Task<LandlordRegistrationOrgLandlordState>() {
    override fun makeSubJourney(state: LandlordRegistrationOrgLandlordState) =
        subJourney(GovBodyMemberAddressStateAdapter(state)) {
            step(journey.lookupAddressStep) {
                routeSegment("organisation-governing-body-member-${LookupAddressStep.ROUTE_SEGMENT}")
                nextStep { mode ->
                    when (mode) {
                        LookupAddressMode.ADDRESSES_FOUND -> journey.selectAddressStep
                        LookupAddressMode.NO_ADDRESSES_FOUND -> journey.noAddressFoundStep
                    }
                }
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.lookupAddress.govBodyMemberRegistration.fieldSetHeading",
                        "fieldSetHint" to "forms.lookupAddress.govBodyMemberRegistration.fieldSetHint",
                    )
                }
            }
            step(journey.selectAddressStep) {
                routeSegment("organisation-governing-body-member-${SelectAddressStep.ROUTE_SEGMENT}")
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.ADDRESSES_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        SelectAddressMode.MANUAL_ADDRESS -> journey.manualAddressStep
                        else -> exitStep
                    }
                }
                withAdditionalContentProperties {
                    mapOf("fieldSetHeading" to "forms.selectAddress.govBodyMemberRegistration.fieldSetHeading")
                }
            }
            step(journey.noAddressFoundStep) {
                routeSegment("organisation-governing-body-member-${NoAddressFoundStep.ROUTE_SEGMENT}")
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND) }
                nextStep { journey.manualAddressStep }
            }
            step(journey.manualAddressStep) {
                routeSegment("organisation-governing-body-member-${ManualAddressStep.ROUTE_SEGMENT}")
                parents {
                    OrParents(
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.noAddressFoundStep.isComplete(),
                    )
                }
                nextStep { exitStep }
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.manualAddress.govBodyMemberRegistration.fieldSetHeading",
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

class GovBodyMemberAddressStateAdapter(
    private val delegateState: LandlordRegistrationOrgLandlordState,
) : AddressState,
    LandlordRegistrationOrgLandlordState by delegateState {
    override val lookupAddressStep: LookupAddressStep
        get() = delegateState.govBodyMemberLookupAddressStep
    override val selectAddressStep: SelectAddressStep
        get() = delegateState.govBodyMemberSelectAddressStep
    override val noAddressFoundStep: NoAddressFoundStep
        get() = delegateState.govBodyMemberNoAddressFoundStep
    override val manualAddressStep: ManualAddressStep
        get() = delegateState.govBodyMemberManualAddressStep
    override var cachedAddresses: List<AddressDataModel>?
        get() = delegateState.govBodyMemberCachedAddresses
        set(value) {
            delegateState.govBodyMemberCachedAddresses = value
        }
    override var isAddressAlreadyRegistered: Boolean?
        get() = delegateState.govBodyMemberIsAddressAlreadyRegistered
        set(value) {
            delegateState.govBodyMemberIsAddressAlreadyRegistered = value
        }
    override var cachedSelectedAddress: String?
        get() = delegateState.govBodyMemberCachedSelectedAddress
        set(value) {
            delegateState.govBodyMemberCachedSelectedAddress = value
        }
}
