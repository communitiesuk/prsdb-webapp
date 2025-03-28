package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

open class JourneyDataExtensions {
    companion object {
        fun JourneyData.getLookedUpAddress(selectedAddress: String): AddressDataModel? =
            this.getLookedUpAddresses().singleOrNull { it.singleLineAddress == selectedAddress }

        fun JourneyData.getLookedUpAddresses(): List<AddressDataModel> {
            val serializedLookedUpAddresses = this.getSerializedLookedUpAddresses() ?: return emptyList()
            return Json.decodeFromString<List<AddressDataModel>>(serializedLookedUpAddresses)
        }

        fun JourneyData.getSerializedLookedUpAddresses(): String? =
            JourneyDataHelper.getStringValueByKey(this, LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY)

        fun JourneyData.withUpdatedLookedUpAddresses(lookedUpAddresses: String): JourneyData {
            val updatedJourneyData = this + (LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to lookedUpAddresses)
            return updatedJourneyData
        }

        fun JourneyData.withUpdatedLookedUpAddresses(lookedUpAddresses: List<AddressDataModel>): JourneyData =
            this.withUpdatedLookedUpAddresses(Json.encodeToString(lookedUpAddresses))

        @JvmStatic
        protected fun JourneyData.getWantsToProceed(urlPathSegment: String): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                urlPathSegment,
                "wantsToProceed",
            )
    }
}
