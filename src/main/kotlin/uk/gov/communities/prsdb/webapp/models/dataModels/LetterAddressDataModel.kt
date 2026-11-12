package uk.gov.communities.prsdb.webapp.models.dataModels

data class LetterAddressDataModel(
    val addressLines: List<String>,
) {
    init {
        require(addressLines.size <= MAX_ADDRESS_LINES) {
            "Notify letter addresses cannot have more than $MAX_ADDRESS_LINES lines"
        }
    }

    // Notify takes the address as personalisation - see LetterNotificationService.sendLetter
    fun toPersonalisation(): Map<String, String> = addressLines.mapIndexed { index, line -> "address_line_${index + 1}" to line }.toMap()

    companion object {
        const val MAX_ADDRESS_LINES = 7
    }
}
