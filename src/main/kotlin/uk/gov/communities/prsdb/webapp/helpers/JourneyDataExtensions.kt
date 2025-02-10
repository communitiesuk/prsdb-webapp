package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.time.LocalDate

fun JourneyData.getLookupAddressHouseNameOrNumberAndPostcode(lookupAddressPathSegment: String): Pair<String, String>? {
    val houseNameOrNumber =
        getFieldStringValue(lookupAddressPathSegment, "houseNameOrNumber")
            ?: return null

    val postcode =
        getFieldStringValue(lookupAddressPathSegment, "postcode")
            ?: return null

    return Pair(houseNameOrNumber, postcode)
}

fun JourneyData.getManualAddress(
    manualAddressPathSegment: String,
    selectLocalAuthorityPathSegment: String? = null,
): AddressDataModel? {
    val addressLineOne =
        getFieldStringValue(
            manualAddressPathSegment,
            "addressLineOne",
        ) ?: return null

    val townOrCity =
        getFieldStringValue(
            manualAddressPathSegment,
            "townOrCity",
        ) ?: return null

    val postcode =
        getFieldStringValue(
            manualAddressPathSegment,
            "postcode",
        ) ?: return null

    val addressLineTwo =
        getFieldStringValue(
            manualAddressPathSegment,
            "addressLineTwo",
        )

    val county =
        getFieldStringValue(
            manualAddressPathSegment,
            "county",
        )

    val localAuthorityId =
        selectLocalAuthorityPathSegment?.let {
            getFieldIntegerValue(
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

fun JourneyData.getPageData(
    pageName: String,
    subPageNumber: Int? = null,
): PageData? {
    var pageData = objectToStringKeyedMap(this[pageName])
    if (subPageNumber != null && pageData != null) {
        pageData = objectToStringKeyedMap(pageData[subPageNumber.toString()])
    }
    return pageData
}

fun JourneyData.getFieldStringValue(
    urlPathSegment: String,
    fieldName: String,
    subPageNumber: Int? = null,
): String? {
    val pageData = getPageData(urlPathSegment, subPageNumber)
    return pageData?.get(fieldName)?.toString()
}

fun JourneyData.getFieldIntegerValue(
    urlPathSegment: String,
    fieldName: String,
    subPageNumber: Int? = null,
): Int? {
    val fieldAsString =
        getFieldStringValue(urlPathSegment, fieldName, subPageNumber) ?: return null
    return fieldAsString.toInt()
}

fun JourneyData.getFieldLocalDateValue(
    urlPathSegment: String,
    fieldName: String,
    subPageNumber: Int? = null,
): LocalDate? {
    val fieldAsString =
        getFieldStringValue(urlPathSegment, fieldName, subPageNumber) ?: return null
    return fieldAsString.let { LocalDate.parse(fieldAsString) }
}

fun JourneyData.getFieldBooleanValue(
    urlPathSegment: String,
    fieldName: String,
    subPageNumber: Int? = null,
): Boolean? {
    val fieldAsString =
        getFieldStringValue(urlPathSegment, fieldName, subPageNumber) ?: return null
    return fieldAsString == "true"
}

inline fun <reified E : Enum<E>> JourneyData.getFieldEnumValue(
    urlPathSegment: String,
    fieldName: String,
    subPageNumber: Int? = null,
): E? {
    val fieldAsString =
        getFieldStringValue(urlPathSegment, fieldName, subPageNumber) ?: return null
    return enumValueOf<E>(fieldAsString)
}
