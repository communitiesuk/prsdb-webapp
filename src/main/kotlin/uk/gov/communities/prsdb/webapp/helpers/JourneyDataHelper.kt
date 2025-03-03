package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.time.LocalDate

open class JourneyDataHelper {
    companion object {
        fun getLookupAddressHouseNameOrNumberAndPostcode(
            journeyData: JourneyData,
            lookupAddressPathSegment: String,
        ): Pair<String, String>? {
            val houseNameOrNumber =
                getFieldStringValue(journeyData, lookupAddressPathSegment, "houseNameOrNumber")
                    ?: return null

            val postcode =
                getFieldStringValue(journeyData, lookupAddressPathSegment, "postcode")
                    ?: return null

            return Pair(houseNameOrNumber, postcode)
        }

        fun getManualAddress(
            journeyData: JourneyData,
            manualAddressPathSegment: String,
            selectLocalAuthorityPathSegment: String? = null,
        ): AddressDataModel? {
            val addressLineOne =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "addressLineOne",
                ) ?: return null

            val townOrCity =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "townOrCity",
                ) ?: return null

            val postcode =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "postcode",
                ) ?: return null

            val addressLineTwo =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "addressLineTwo",
                )

            val county =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "county",
                )

            val localAuthorityId =
                selectLocalAuthorityPathSegment?.let {
                    getFieldIntegerValue(
                        journeyData,
                        it,
                        "localAuthorityId",
                    ) ?: return null
                }

            return AddressDataModel.fromManualAddressData(
                addressLineOne,
                townOrCity,
                postcode,
                addressLineTwo,
                county,
                localAuthorityId,
            )
        }

        fun getStringValueByKey(
            journeyData: JourneyData,
            key: String,
        ): String? = journeyData[key]?.toString()

        fun getPageData(
            journeyData: JourneyData,
            pageName: String,
            subPageNumber: Int? = null,
        ): PageData? {
            var pageData = objectToStringKeyedMap(journeyData[pageName])
            if (subPageNumber != null && pageData != null) {
                pageData = objectToStringKeyedMap(pageData[subPageNumber.toString()])
            }
            return pageData
        }

        fun getFieldStringValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): String? {
            val pageData = getPageData(journeyData, urlPathSegment, subPageNumber)
            return pageData?.get(fieldName)?.toString()
        }

        fun getFieldIntegerValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): Int? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return fieldAsString.toInt()
        }

        fun getFieldLocalDateValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): LocalDate? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return fieldAsString.let { LocalDate.parse(fieldAsString) }
        }

        fun getFieldBooleanValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): Boolean? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return fieldAsString == "true"
        }

        inline fun <reified E : Enum<E>> getFieldEnumValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): E? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return enumValueOf<E>(fieldAsString)
        }
    }
}
