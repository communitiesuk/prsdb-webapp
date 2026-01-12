package uk.gov.communities.prsdb.webapp.models.dataModels

data class RentAmountDataModel(
    val formattedRentAmount: List<String>,
) {
    companion object {
        fun fromFormData(
            rentAmount: String,
            isCustomRentFrequency: Boolean,
        ): RentAmountDataModel =
            RentAmountDataModel(
                formattedRentAmount = getFormattedRentAmount(rentAmount, isCustomRentFrequency),
            )

        private fun getFormattedRentAmount(
            rentAmount: String,
            isCustomRentFrequency: Boolean,
        ): List<String> {
            val formattedRentAmount = mutableListOf("commonText.poundSign", "$rentAmount ")
            if (isCustomRentFrequency) formattedRentAmount.add("forms.checkPropertyAnswers.tenancyDetails.customFrequencyRentAmountSuffix")
            return formattedRentAmount
        }
    }
}
