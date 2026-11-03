package uk.gov.communities.prsdb.webapp.journeyMap.server

import org.springframework.jdbc.core.JdbcTemplate

// A property registration in progress is also saved to the database, and its payment is taken against that record, so
// restoring a session after the seed data was reset brings back the rows the reset removed
class SavedJourneyRecords(
    private val states: List<Map<String, Any?>>,
    private val incompleteProperties: List<Map<String, Any?>>,
    private val payments: List<Map<String, Any?>>,
) {
    fun restoreMissing(jdbcTemplate: JdbcTemplate) {
        states.filterNot { exists(jdbcTemplate, it) }.forEach { state ->
            val row = state - "id" + ("reminder_email_sent_id" to null)
            val columns = row.keys.toList()
            val stateId =
                jdbcTemplate.queryForObject(
                    "INSERT INTO saved_journey_state (${columns.joinToString()}) VALUES (${columns.joinToString { "?" }}) RETURNING id",
                    Long::class.java,
                    *columns.map { row[it] }.toTypedArray(),
                )
            incompleteProperties
                .filter { it["saved_journey_state_id"] == state["id"] }
                .forEach { insert(jdbcTemplate, "landlord_incomplete_properties", it + ("saved_journey_state_id" to stateId)) }
            payments
                .filter { it[PAYMENT_STATE_ID] == state["id"] }
                .forEach { insert(jdbcTemplate, "payment", it + (PAYMENT_STATE_ID to stateId)) }
        }
    }

    private fun exists(
        jdbcTemplate: JdbcTemplate,
        state: Map<String, Any?>,
    ) = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM saved_journey_state WHERE journey_id = ? AND subject_identifier = ?",
        Int::class.java,
        state["journey_id"],
        state["subject_identifier"],
    )!! > 0

    private fun insert(
        jdbcTemplate: JdbcTemplate,
        table: String,
        row: Map<String, Any?>,
    ) {
        val columns = row.keys.toList()
        jdbcTemplate.update(
            "INSERT INTO $table (${columns.joinToString()}) VALUES (${columns.joinToString { "?" }}) ON CONFLICT DO NOTHING",
            *columns.map { row[it] }.toTypedArray(),
        )
    }

    companion object {
        private const val PAYMENT_STATE_ID = "associated_incomplete_property_saved_journey_state_id"

        fun capture(jdbcTemplate: JdbcTemplate) =
            SavedJourneyRecords(
                jdbcTemplate.queryForList("SELECT * FROM saved_journey_state"),
                jdbcTemplate.queryForList("SELECT * FROM landlord_incomplete_properties"),
                jdbcTemplate.queryForList("SELECT * FROM payment WHERE $PAYMENT_STATE_ID IS NOT NULL"),
            )
    }
}
