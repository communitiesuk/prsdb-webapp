package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded

data class BillsIncludedDataModel(
    val standardBillsIncludedListAsStrings: List<String>,
    val standardBillsIncludedListAsEnums: List<BillsIncluded>,
    val customBillsIncludedIfRequired: String?,
) {
    companion object {
        fun fromFormData(
            billsIncluded: List<String?>,
            customBillsIncluded: String,
        ): BillsIncludedDataModel =
            BillsIncludedDataModel(
                standardBillsIncludedListAsStrings = billsIncluded.filterNotNull(),
                standardBillsIncludedListAsEnums = getStandardBillsIncludedListAsEnums(billsIncluded),
                customBillsIncludedIfRequired = getCustomBillsIncludedIfRequired(billsIncluded, customBillsIncluded),
            )

        fun getStandardBillsIncludedListAsEnums(billsIncluded: List<String?>) =
            billsIncluded.filterNotNull().map { BillsIncluded.valueOf(it) }

        fun getCustomBillsIncludedIfRequired(
            billsIncluded: List<String?>,
            customBillsIncluded: String,
        ): String? =
            if (shouldIncludeCustomBillsIncluded(billsIncluded, customBillsIncluded)) {
                customBillsIncluded
            } else {
                null
            }

        fun shouldIncludeCustomBillsIncluded(
            billsIncluded: List<String?>,
            customBillsIncluded: String,
        ): Boolean = billsIncluded.contains(BillsIncluded.SOMETHING_ELSE.toString()) && customBillsIncluded.isNotEmpty()
    }
}
