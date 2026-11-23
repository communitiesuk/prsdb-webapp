package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.INTERNATIONAL_ADDRESSES
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.AddressSearchRegion
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class NoAddressFoundStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, AddressState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AddressState) =
        messageKeys() +
            mapOf(
                "postcode" to state.lookupAddressStep.formModel.notNullValue(LookupAddressFormModel::postcode),
                "houseNameOrNumber" to state.lookupAddressStep.formModel.notNullValue(LookupAddressFormModel::houseNameOrNumber),
                "searchAgainUrl" to Destination(state.lookupAddressStep).toUrlStringOrNull(),
            )

    override fun chooseTemplate(state: AddressState) = "forms/noAddressFoundForm"

    override fun mode(state: AddressState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: AddressState) {
        // this is also set via 'Enter manually' radio on the select address page
        state.cachedSelectedAddress = MANUAL_ADDRESS_CHOSEN
    }

    private var region: AddressSearchRegion = AddressSearchRegion.UK

    fun restrictToRegion(region: AddressSearchRegion): NoAddressFoundStepConfig {
        this.region = region
        return this
    }

    private fun messageKeys(): Map<String, String> {
        val regionKey =
            when (region) {
                AddressSearchRegion.WITH_ENGLISH_LOCAL_COUNCIL -> "englandPropertyDetails"
                AddressSearchRegion.ENGLAND_AND_WALES -> "englandOrWales"
                AddressSearchRegion.UK -> "uk"
            }
        // TODO: PDJB-2259: Remove this legacy copy when INTERNATIONAL_ADDRESSES is removed
        if (!featureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES) && region != AddressSearchRegion.WITH_ENGLISH_LOCAL_COUNCIL) {
            return mapOf(
                "headingKey" to "$MESSAGE_KEY_PREFIX.heading.englandOrWales",
                "paragraphOneBeforeLinkKey" to "$MESSAGE_KEY_PREFIX.paragraph.one.beforeLink.legacy",
                "paragraphTwoKey" to "$MESSAGE_KEY_PREFIX.paragraph.two.englandOrWales",
            )
        }
        return mapOf(
            "headingKey" to "$MESSAGE_KEY_PREFIX.heading.$regionKey",
            "paragraphOneBeforeLinkKey" to "$MESSAGE_KEY_PREFIX.paragraph.one.beforeLink.$regionKey",
            "paragraphTwoKey" to "$MESSAGE_KEY_PREFIX.paragraph.two.$regionKey",
        )
    }

    companion object {
        private const val MESSAGE_KEY_PREFIX = "addressForms.noAddressFound"
    }
}

@JourneyFrameworkComponent
final class NoAddressFoundStep(
    stepConfig: NoAddressFoundStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AddressState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "no-address-found"
    }
}
