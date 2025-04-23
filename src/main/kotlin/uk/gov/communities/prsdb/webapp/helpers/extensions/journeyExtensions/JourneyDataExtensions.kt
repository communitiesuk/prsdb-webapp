package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateFormModel

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
        protected fun JourneyData.getFieldSetLocalDateValue(urlPathSegment: String): LocalDate? {
            val day = JourneyDataHelper.getFieldStringValue(this, urlPathSegment, DateFormModel::day.name) ?: return null
            val month = JourneyDataHelper.getFieldStringValue(this, urlPathSegment, DateFormModel::month.name) ?: return null
            val year = JourneyDataHelper.getFieldStringValue(this, urlPathSegment, DateFormModel::year.name) ?: return null
            return DateTimeHelper.parseDateOrNull(day, month, year)
        }
    }
}
