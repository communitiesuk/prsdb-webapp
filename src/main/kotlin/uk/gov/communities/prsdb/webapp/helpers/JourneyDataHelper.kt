package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

open class JourneyDataHelper {
    companion object {
        fun getManualAddress(
            journeyData: JourneyData,
            manualAddressPathSegment: String,
        ): AddressDataModel? {
            val addressLineOne =
                JourneyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "addressLineOne",
                ) ?: return null

            val townOrCity =
                JourneyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "townOrCity",
                ) ?: return null

            val postcode =
                JourneyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "postcode",
                ) ?: return null

            val addressLineTwo =
                JourneyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "addressLineTwo",
                )

            val county =
                JourneyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "county",
                )

            return AddressDataModel.fromManualAddressData(
                addressLineOne,
                townOrCity,
                postcode,
                addressLineTwo,
                county,
            )
        }
    }
}
