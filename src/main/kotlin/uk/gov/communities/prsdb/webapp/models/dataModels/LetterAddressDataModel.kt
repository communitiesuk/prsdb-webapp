package uk.gov.communities.prsdb.webapp.models.dataModels

data class LetterAddressDataModel(
    val lines: List<String>,
) {
    init {
        require(lines.size in 3..7) { "A letter address must contain between 3 and 7 lines" }
        require(lines.take(2).all { line -> line.any { it.isLetterOrDigit() } }) {
            "The first two letter address lines must contain alphanumeric characters"
        }
        require(lines.none { line -> line.trimStart().firstOrNull()?.let { it in INVALID_LINE_START_CHARACTERS } == true }) {
            "Letter address lines must not start with any of: $INVALID_LINE_START_CHARACTERS"
        }
    }

    fun toPersonalisation(): Map<String, String> = lines.mapIndexed { index, line -> "address_line_${index + 1}" to line }.toMap()

    companion object {
        // Taken from Notify's postal address validation
        private const val INVALID_LINE_START_CHARACTERS = "@()=[]\"\\/,<>~"
    }
}
