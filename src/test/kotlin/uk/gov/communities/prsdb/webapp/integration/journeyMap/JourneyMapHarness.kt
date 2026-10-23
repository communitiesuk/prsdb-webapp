package uk.gov.communities.prsdb.webapp.integration.journeyMap

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.integration.IntegrationTest
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.SeededAddress
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater
import uk.gov.communities.prsdb.webapp.testHelpers.IntegrationTestHelper

abstract class JourneyMapHarness : IntegrationTest() {
    @MockitoBean
    lateinit var epcRegisterClient: EpcRegisterClient

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    // Session snapshots taken before a journey was committed still refer to the files uploaded so far
    fun resetDatabase(seedScripts: List<String>) {
        val uploads = jdbcTemplate.queryForList("SELECT * FROM file_upload")
        IntegrationTestHelper.resetAndSeedDatabase(seedScripts, jdbcTemplate)
        uploads.forEach { row ->
            val columns = row.keys.toList()
            jdbcTemplate.update(
                "INSERT INTO file_upload (${columns.joinToString()}) VALUES (${columns.joinToString { "?" }}) ON CONFLICT DO NOTHING",
                *columns.map { row[it] }.toTypedArray(),
            )
        }
        jdbcTemplate.execute(
            "SELECT setval(pg_get_serial_sequence('file_upload', 'id'), (SELECT COALESCE(MAX(id), 0) + 1 FROM file_upload), false)",
        )
    }

    fun applyBaseFeatureFlags() {
        val base = BaseFeatureFlags.load()
        FeatureFlagConfigUpdater.resetToConfiguration(featureFlagManager, base.featureFlags, base.releases)
    }

    fun featureFlagStates(): Map<String, Boolean> =
        BaseFeatureFlags.load().featureFlags.associate { it.name to featureFlagManager.checkFeature(it.name) }

    // Journeys that are hidden behind a flag in the base configuration are mapped with the flag on
    fun enableFeature(name: String) = featureFlagManager.enableFeature(name)

    fun firstSeededAddress(): SeededAddress =
        jdbcTemplate
            .query(
                "SELECT postcode, COALESCE(building_number, building_name) AS building FROM address " +
                    "WHERE postcode IS NOT NULL ORDER BY id LIMIT 1",
            ) { row, _ -> SeededAddress(row.getString("postcode"), row.getString("building") ?: "1") }
            .firstOrNull() ?: SeededAddress("EG1 2AA", "1")
}
