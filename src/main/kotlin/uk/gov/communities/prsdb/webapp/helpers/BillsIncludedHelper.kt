package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.models.dataModels.BillsIncludedDataModel
import kotlin.collections.joinToString

class BillsIncludedHelper {
    companion object {
        fun getBillsIncludedForCYAStep(
            billsIncludedDataModel: BillsIncludedDataModel,
            messageSource: MessageSource,
        ): String {
            return buildBillsIncludedString(
                billsIncluded = billsIncludedDataModel.standardBillsIncludedListAsEnums,
                customBillsIncluded = billsIncludedDataModel.customBillsIncluded,
                messageSource = messageSource,
            )
        }

        fun getBillsIncludedForPropertyDetails(
            propertyOwnership: PropertyOwnership,
            messageSource: MessageSource,
        ): String {
            val allBillsIncluded = propertyOwnership.billsIncludedList!!.split(",").map { BillsIncluded.valueOf(it) }
            return buildBillsIncludedString(
                billsIncluded = allBillsIncluded,
                customBillsIncluded = propertyOwnership.customBillsIncluded,
                messageSource = messageSource,
            )
        }

        private fun buildBillsIncludedString(
            billsIncluded: List<BillsIncluded>,
            customBillsIncluded: String?,
            messageSource: MessageSource,
        ): String =
            billsIncluded.joinToString(", ") { bill ->
                if (bill != BillsIncluded.SOMETHING_ELSE) {
                    messageSource.getMessageForKey(MessageKeyConverter.convert(bill))
                } else {
                    customBillsIncluded!!.replaceFirstChar { it.uppercase() }
                }
            }
    }
}
