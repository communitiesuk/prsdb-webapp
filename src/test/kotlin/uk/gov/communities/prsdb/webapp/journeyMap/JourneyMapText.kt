package uk.gov.communities.prsdb.webapp.journeyMap

object JourneyMapText {
    private val classSuffixes = listOf("StepConfig", "Config", "Step", "Task")

    fun humaniseClassName(simpleName: String): String {
        val base = classSuffixes.fold(simpleName.substringBefore("$$")) { name, suffix -> name.removeSuffix(suffix) }
        return humaniseIdentifier(base)
    }

    fun humaniseIdentifier(identifier: String): String {
        val words =
            identifier
                .replace(Regex("[_\\-/]+"), " ")
                .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
                .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1 $2")
                .trim()
                .split(Regex("\\s+"))
                .filter { it.isNotEmpty() }
        if (words.isEmpty()) return identifier
        return words
            .mapIndexed { index, word ->
                val isAcronym = word.length > 1 && word.all { it.isUpperCase() || it.isDigit() }
                when {
                    isAcronym -> word
                    index == 0 -> word.lowercase().replaceFirstChar { it.uppercase() }
                    else -> word.lowercase()
                }
            }.joinToString(" ")
    }

    fun humaniseMode(mode: String): String = humaniseIdentifier(mode.lowercase())
}
