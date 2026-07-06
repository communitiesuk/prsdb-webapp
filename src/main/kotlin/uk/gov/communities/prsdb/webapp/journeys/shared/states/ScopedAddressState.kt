package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

// Lets a routed address task be added to a journey more than once. It IS an AddressState - built against fresh
// step objects supplied per instance - and delegates all other journey-state behaviour to the real journey
// state, but prefixes every address key (the cached variables and all step form data) with the instance route.
// Each instance therefore stores its data separately without the real state gaining any per-instance fields.
class ScopedAddressState(
    private val routePrefix: String,
    journeyStateService: JourneyStateService,
    private val delegate: JourneyState,
    override val lookupAddressStep: LookupAddressStep,
    override val selectAddressStep: SelectAddressStep,
    override val noAddressFoundStep: NoAddressFoundStep,
    override val manualAddressStep: ManualAddressStep,
) : AddressState,
    JourneyState by delegate {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)

    override var cachedAddresses: List<AddressDataModel>? by delegateProvider.nullableDelegate("$routePrefix/cachedAddresses")
    override var cachedSelectedAddress: String? by delegateProvider.nullableDelegate("$routePrefix/cachedSelectedAddress")
    override var isAddressAlreadyRegistered: Boolean? by delegateProvider.nullableDelegate("$routePrefix/isAddressAlreadyRegistered")

    override fun getStepData(key: String): FormData? = delegate.getStepData("$routePrefix/$key")

    override fun addStepData(
        key: String,
        value: FormData,
    ) = delegate.addStepData("$routePrefix/$key", value)

    override fun clearStepData(key: String) = delegate.clearStepData("$routePrefix/$key")
}
