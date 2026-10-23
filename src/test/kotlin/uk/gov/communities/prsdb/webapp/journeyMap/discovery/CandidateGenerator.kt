package uk.gov.communities.prsdb.webapp.journeyMap.discovery

import uk.gov.communities.prsdb.webapp.journeyMap.JourneyMapText
import java.time.format.DateTimeFormatter

data class Choice(
    val group: String,
    val name: String?,
    val value: String,
    val label: String,
    val controls: String?,
)

data class Candidate(
    val key: String,
    val label: String,
    val choices: List<Choice>,
    val valueIndices: Map<String, Int>,
    val params: List<Pair<String, String>>,
    val requiresUpload: Boolean,
)

class CandidateGenerator(
    private val values: ValueGenerator,
    private val maxCandidates: Int = 20,
) {
    fun generate(
        page: PageSnapshot,
        validators: Map<String, List<String>> = emptyMap(),
    ): List<Candidate> =
        combinations(choiceGroups(page))
            .take(maxCandidates)
            .map { build(page, it, emptyMap(), validators) }
            .toList()

    fun retry(
        page: PageSnapshot,
        candidate: Candidate,
        errors: List<ErrorLink>,
        validators: Map<String, List<String>> = emptyMap(),
    ): Candidate? {
        val references = errors.mapNotNull { it.href?.removePrefix("#") }
        val failingKeys =
            activeTextFields(page, candidate.choices)
                .filter { field -> references.any { matches(field, it) } }
                .map { fieldKey(it) }
                .toSet()
        if (failingKeys.isEmpty()) return null

        val nextIndices = candidate.valueIndices.toMutableMap()
        var advanced = false
        failingKeys.forEach { key ->
            val next = (nextIndices[key] ?: 0) + 1
            if (next < valueCount(page, key, validators)) {
                nextIndices[key] = next
                advanced = true
            }
        }
        return if (advanced) build(page, candidate.choices, nextIndices, validators) else null
    }

    fun alternatives(
        page: PageSnapshot,
        base: Candidate,
        validators: Map<String, List<String>> = emptyMap(),
    ): List<Candidate> =
        activeTextFields(page, base.choices)
            .map { fieldKey(it) }
            .distinct()
            .flatMap { key ->
                val current = base.valueIndices[key] ?: 0
                (0 until valueCount(page, key, validators))
                    .filter { it != current }
                    .map { build(page, base.choices, base.valueIndices + (key to it), validators) }
            }

    private fun build(
        page: PageSnapshot,
        choices: List<Choice>,
        valueIndices: Map<String, Int>,
        validators: Map<String, List<String>>,
    ): Candidate {
        val params = mutableListOf<Pair<String, String>>()
        page.fields
            .filter { it.type == "hidden" && it.name != null }
            .forEach { params += it.name!! to (it.value ?: "") }
        choices
            .filter { it.name != null }
            .forEach { params += it.name!! to it.value }
        val textValues = activeTextFields(page, choices).associateWith { valueFor(page, it, valueIndices, validators) }
        textValues.forEach { (field, value) -> params += field.name!! to value }

        val choiceKey =
            choices
                .filter { it.name != null }
                .joinToString("&") { if (it.group == BUTTON_GROUP) "button:${it.name}=${it.value}" else "${it.group}=${it.value}" }
                .ifEmpty { "default" }
        val alternativeKeys = valueIndices.filterValues { it > 0 }.toSortedMap()
        val key = choiceKey + alternativeKeys.entries.joinToString("") { "|${it.key}#${it.value}" }

        val alternativeLabels =
            alternativeKeys.keys.mapNotNull { fieldKey ->
                val fields = textValues.keys.filter { fieldKey(it) == fieldKey }
                val first = fields.firstOrNull() ?: return@mapNotNull null
                if (first.dateGroup != null) {
                    val date = values.datesFor(first.dateGroup, page.url)[valueIndices.getValue(fieldKey)]
                    "${JourneyMapText.humaniseIdentifier(first.dateGroup)}: ${date.format(DATE_FORMAT)}"
                } else {
                    "${first.label.ifBlank { JourneyMapText.humaniseIdentifier(fieldKey) }}: ${textValues.getValue(first)}"
                }
            }
        val label = (choices.map { it.label } + alternativeLabels).filter { it.isNotBlank() }.joinToString(", ")

        return Candidate(key, label, choices, valueIndices, params, page.hasFileInput)
    }

    private fun choiceGroups(page: PageSnapshot): List<List<Choice>> {
        val groups = linkedMapOf<String, MutableList<Choice>>()
        page.fields.forEach { field ->
            val name = field.name ?: return@forEach
            when (field.type) {
                "radio" -> {
                    groups.getOrPut(name) { mutableListOf() } +=
                        Choice(name, name, field.value ?: "", field.label.ifBlank { field.value ?: "" }, field.controls)
                }
                "checkbox" -> {
                    val baseName = name.substringBefore('[')
                    groups.getOrPut(baseName) { mutableListOf() } +=
                        Choice(baseName, name, field.value ?: "", field.label.ifBlank { field.value ?: "" }, field.controls)
                }
                "select" -> {
                    val options = field.options.filter { it.value.isNotBlank() }
                    groups[name] =
                        options
                            .take(if (options.size > MAX_BRANCHING_SELECT_OPTIONS) 1 else options.size)
                            .map { Choice(name, name, it.value, it.label.ifBlank { it.value }, null) }
                            .toMutableList()
                }
            }
        }
        val submitButtons = page.buttons.filter { it.type == "submit" }
        val buttonGroup =
            if (submitButtons.any { it.name != null }) {
                submitButtons.map { Choice(BUTTON_GROUP, it.name, it.value ?: "", it.text, null) }
            } else {
                emptyList()
            }
        return groups.values.filter { it.isNotEmpty() } + listOfNotNull(buttonGroup.ifEmpty { null })
    }

    private fun combinations(groups: List<List<Choice>>): Sequence<List<Choice>> =
        groups.fold(sequenceOf(emptyList())) { partials, group ->
            partials.flatMap { partial -> group.asSequence().map { partial + it } }
        }

    private fun activeTextFields(
        page: PageSnapshot,
        choices: List<Choice>,
    ): List<FormField> {
        val revealed = choices.mapNotNull { it.controls }.toSet()
        return page.fields.filter { it.name != null && it.type in textTypes && (it.conditional == null || it.conditional in revealed) }
    }

    private fun valueFor(
        page: PageSnapshot,
        field: FormField,
        valueIndices: Map<String, Int>,
        validators: Map<String, List<String>>,
    ): String {
        val kind = values.kindOf(field, validatorsFor(field, validators))
        val index = valueIndices[fieldKey(field)] ?: 0
        return if (kind.isDatePart) {
            val dates = values.datesFor(field.dateGroup ?: "date", page.url)
            values.datePart(kind, dates[index.coerceAtMost(dates.lastIndex)])
        } else {
            val options = values.valuesFor(kind)
            options[index.coerceAtMost(options.lastIndex)]
        }
    }

    private fun valueCount(
        page: PageSnapshot,
        key: String,
        validators: Map<String, List<String>>,
    ): Int {
        val field = page.fields.firstOrNull { it.name != null && it.type in textTypes && fieldKey(it) == key } ?: return 1
        val kind = values.kindOf(field, validatorsFor(field, validators))
        return if (kind.isDatePart) values.datesFor(field.dateGroup ?: "date", page.url).size else values.valuesFor(kind).size
    }

    private fun validatorsFor(
        field: FormField,
        validators: Map<String, List<String>>,
    ): List<String> = validators[field.name] ?: validators[field.name?.substringBefore('[')] ?: emptyList()

    private fun fieldKey(field: FormField): String = field.dateGroup ?: field.name!!

    private fun matches(
        field: FormField,
        reference: String,
    ): Boolean = field.id == reference || field.name == reference || field.dateGroup == reference

    companion object {
        const val BUTTON_GROUP = "__button"

        // Long lists such as local councils are data rather than branches, so only the first option is submitted
        private const val MAX_BRANCHING_SELECT_OPTIONS = 6
        private val textTypes = setOf("text", "email", "tel", "number", "password", "search", "url", "textarea")
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy")
    }
}
