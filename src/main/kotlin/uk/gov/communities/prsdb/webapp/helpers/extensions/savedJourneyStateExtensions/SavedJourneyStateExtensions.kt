package uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyStateExtensions

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import kotlin.collections.get

class SavedJourneyStateExtensions {
    companion object {
        private val objectMapper = ObjectMapper()

        fun SavedJourneyState.getPropertyRegistrationSingleLineAddress(): String = getPropertyRegistrationAddress().singleLineAddress

        fun SavedJourneyState.getPropertyRegistrationMultiLineAddress(): String = getPropertyRegistrationAddress().toMultiLineAddress()

        private fun SavedJourneyState.getPropertyRegistrationAddress(): AddressDataModel {
            val stateDataMap = objectMapper.readValue(this.serializedState, Map::class.java)
            val submittedJourneyData = stateDataMap["journeyData"] as Map<*, *>
            val selectedAddressData = submittedJourneyData["select-address"] as? Map<*, *>
            val selectedAddress = selectedAddressData?.get("address") as? String
            val serializedCachedAddressData = stateDataMap["cachedAddresses"] as String
            val cachedAddressData: List<AddressDataModel> = Json.decodeFromString(serializedCachedAddressData)

            return cachedAddressData.find { it.singleLineAddress == selectedAddress } ?: run {
                val manualAddressData = submittedJourneyData["manual-address"] as Map<*, *>
                val localCouncilData = submittedJourneyData["local-council"] as Map<*, *>
                AddressDataModel.fromManualAddressData(
                    addressLineOne = manualAddressData["addressLineOne"] as String,
                    addressLineTwo = manualAddressData["addressLineTwo"] as String?,
                    townOrCity = manualAddressData["townOrCity"] as String,
                    county = manualAddressData["county"] as String?,
                    postcode = manualAddressData["postcode"] as String,
                    localCouncilId = localCouncilData["localCouncilId"] as Int?,
                )
            }
        }
    }
}
