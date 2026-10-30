package uk.gov.communities.prsdb.webapp.models.dataModels

data class LetterAddressDataModel(
    val lines: List<String>,
) {
    // Notify takes the address as personalisation - see LetterNotificationService.sendLetter
    fun toPersonalisation(): Map<String, String> = lines.mapIndexed { index, line -> "address_line_${index + 1}" to line }.toMap()
}
