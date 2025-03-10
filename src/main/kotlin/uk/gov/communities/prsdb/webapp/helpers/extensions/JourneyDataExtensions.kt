package uk.gov.communities.prsdb.webapp.helpers.extensions

import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

class JourneyDataExtensions {
    companion object {
        fun JourneyData.getLookedUpAddress(selectedAddress: String): AddressDataModel? {
            val serializedLookedUpAddresses =
                JourneyDataHelper.getStringValueByKey(this, LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY)
                    ?: return null
            val lookedUpAddresses = Json.decodeFromString<List<AddressDataModel>>(serializedLookedUpAddresses)
            return lookedUpAddresses.singleOrNull { it.singleLineAddress == selectedAddress }
        }
    }
}
