package uk.gov.communities.prsdb.webapp.journeyMap.discovery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CandidateGeneratorTests {
    private val generator = CandidateGenerator(ValueGenerator(LocalDate.of(2026, 10, 20), SeededAddress("EG1 2AA", "1")))

    @Test
    fun `a radio group produces one candidate per option, labelled with the option label`() {
        val page =
            page(
                radio("landlordType", "INDIVIDUAL", "An individual"),
                radio("landlordType", "ORGANISATION", "An organisation"),
            )

        val candidates = generator.generate(page)

        assertEquals(listOf("An individual", "An organisation"), candidates.map { it.label })
        assertEquals(listOf("landlordType=INDIVIDUAL", "landlordType=ORGANISATION"), candidates.map { it.key })
        assertTrue(candidates[1].params.contains("landlordType" to "ORGANISATION"))
    }

    @Test
    fun `hidden inputs are submitted with every candidate`() {
        val page = page(csrf(), radio("occupied", "true", "Yes"), radio("occupied", "false", "No"))

        generator.generate(page).forEach { assertTrue(it.params.contains("_csrf" to "token")) }
    }

    @Test
    fun `inputs inside a conditional reveal are only filled when their controlling option is chosen`() {
        val page =
            page(
                radio("propertyType", "DETACHED_HOUSE", "Detached house"),
                radio("propertyType", "OTHER", "Other", controls = "propertyType-OTHER-conditional"),
                FormField(name = "customPropertyType", id = "customPropertyType", conditional = "propertyType-OTHER-conditional"),
            )

        val (detached, other) = generator.generate(page)

        assertTrue(detached.params.none { it.first == "customPropertyType" })
        assertTrue(other.params.contains("customPropertyType" to "Example text"))
    }

    @Test
    fun `named submit buttons become alternative candidates`() {
        val page =
            PageSnapshot(
                url = "http://localhost/step",
                hasForm = true,
                fields = listOf(FormField(name = "households", id = "households", inputmode = "numeric")),
                buttons =
                    listOf(
                        FormButton(text = "Save and continue"),
                        FormButton(name = "action", value = "provideLater", text = "Provide this later"),
                    ),
            )

        val candidates = generator.generate(page)

        assertEquals(listOf("Save and continue", "Provide this later"), candidates.map { it.label })
        assertTrue(candidates[0].params.none { it.first == "action" })
        assertTrue(candidates[1].params.contains("action" to "provideLater"))
    }

    @Test
    fun `checkbox groups produce one candidate per option using their indexed names`() {
        val page = page(checkbox("orgType[0]", "CHARITY", "Charity"), checkbox("orgType[1]", "TRUST", "Trust"))

        val candidates = generator.generate(page)

        assertEquals(listOf("Charity", "Trust"), candidates.map { it.label })
        assertTrue(candidates[1].params.contains("orgType[1]" to "TRUST"))
        assertTrue(candidates[1].params.none { it.first == "orgType[0]" })
    }

    @Test
    fun `a page with no choices produces a single default candidate with generated text`() {
        val page = page(FormField(name = "emailAddress", id = "emailAddress", type = "email"))

        val candidate = generator.generate(page).single()

        assertEquals("default", candidate.key)
        assertTrue(candidate.params.contains("emailAddress" to "jane.smith@example.com"))
    }

    @Test
    fun `a long select list contributes only its first non-blank option`() {
        val options = listOf(SelectOption("", "Select a local council")) + (1..300).map { SelectOption("$it", "Council $it") }
        val page = page(FormField(tag = "select", type = "select", name = "localCouncilId", id = "localCouncilId", options = options))

        val candidate = generator.generate(page).single()

        assertTrue(candidate.params.contains("localCouncilId" to "1"))
    }

    @Test
    fun `a short select list contributes one candidate per non-blank option`() {
        val options = listOf(SelectOption("WEEKLY", "Weekly"), SelectOption("MONTHLY", "Monthly"))
        val page = page(FormField(tag = "select", type = "select", name = "rentFrequency", id = "rentFrequency", options = options))

        assertEquals(listOf("Weekly", "Monthly"), generator.generate(page).map { it.label })
    }

    @Test
    fun `date groups are filled from a single candidate date`() {
        val page = page(dateField("day", "dateOfBirth"), dateField("month", "dateOfBirth"), dateField("year", "dateOfBirth"))

        val candidate = generator.generate(page).single()

        assertTrue(candidate.params.containsAll(listOf("day" to "1", "month" to "1", "year" to "1980")))
    }

    @Test
    fun `retrying moves fields linked from the error summary to their next value`() {
        val page = page(FormField(name = "numberOfPeople", id = "numberOfPeople", inputmode = "numeric"))
        val first = generator.generate(page).single()

        val retried = generator.retry(page, first, listOf(ErrorLink("#numberOfPeople", "Enter a number")))

        assertNotNull(retried)
        assertEquals("1", retried!!.params.single { it.first == "numberOfPeople" }.second)
    }

    @Test
    fun `retrying returns null when the failing fields have no more values`() {
        val page = page(FormField(name = "name", id = "name"))
        val first = generator.generate(page).single()

        assertNull(generator.retry(page, first, listOf(ErrorLink("#name", "Enter a name"))))
    }

    @Test
    fun `alternative values vary one text field at a time`() {
        val page =
            page(
                FormField(name = "postcode", id = "postcode"),
                FormField(name = "houseNameOrNumber", id = "houseNameOrNumber"),
            )
        val base = generator.generate(page).single()

        val alternatives = generator.alternatives(page, base)

        assertEquals(1, alternatives.size)
        assertTrue(alternatives.single().params.contains("postcode" to "ZZ9 9ZZ"))
        assertTrue(alternatives.single().params.contains("houseNameOrNumber" to "1"))
    }

    private fun page(vararg fields: FormField) =
        PageSnapshot(
            url = "http://localhost/step",
            hasForm = true,
            fields = fields.toList(),
            buttons = listOf(FormButton(text = "Continue")),
        )

    private fun csrf() = FormField(type = "hidden", name = "_csrf", value = "token")

    private fun radio(
        name: String,
        value: String,
        label: String,
        controls: String? = null,
    ) = FormField(type = "radio", name = name, id = "$name-$value", value = value, label = label, controls = controls)

    private fun checkbox(
        name: String,
        value: String,
        label: String,
    ) = FormField(type = "checkbox", name = name, id = "${name.substringBefore('[')}-$value", value = value, label = label)

    private fun dateField(
        name: String,
        group: String,
    ) = FormField(name = name, id = name, inputmode = "numeric", dateGroup = group)
}
