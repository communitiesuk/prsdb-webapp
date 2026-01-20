package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.BillsIncludedFormModel

data class BillsIncludedDataModel(
    val standardBillsIncludedString: String,
    val standardBillsIncludedListAsEnums: List<BillsIncluded>,
    val customBillsIncluded: String?,
) {
    companion object {
        fun fromFormData(formModel: BillsIncludedFormModel): BillsIncludedDataModel =
            BillsIncludedDataModel(
                standardBillsIncludedString = formModel.billsIncluded.filterNotNull().joinToString(separator = ","),
                standardBillsIncludedListAsEnums = getStandardBillsIncludedListAsEnums(billsIncluded = formModel.billsIncluded),
                customBillsIncluded = getCustomBillsIncludedIfRequired(formModel),
            )

        private fun getStandardBillsIncludedListAsEnums(billsIncluded: List<String?>) =
            billsIncluded.filterNotNull().map { BillsIncluded.valueOf(it) }

        private fun getCustomBillsIncludedIfRequired(formModel: BillsIncludedFormModel): String? =
            if (formModel.billsIncluded.contains(BillsIncluded.SOMETHING_ELSE.toString())) formModel.customBillsIncluded else null
    }
}
