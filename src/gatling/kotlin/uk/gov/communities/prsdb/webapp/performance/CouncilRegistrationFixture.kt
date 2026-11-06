package uk.gov.communities.prsdb.webapp.performance

import uk.gov.communities.prsdb.webapp.performance.CouncilFixtureFields.boolean
import uk.gov.communities.prsdb.webapp.performance.CouncilFixtureFields.text
import java.time.OffsetDateTime
import java.util.UUID

data class CouncilRegistrationFixture(
    val subject: String,
    val validToken: String,
    val expiredToken: String,
    val email: String,
    val councilName: String,
    val invitedAsAdmin: Boolean,
) {
    companion object {
        fun parse(
            json: String,
            required: Int,
        ): List<CouncilRegistrationFixture> {
            val fixtures =
                CouncilFixtureFields.rows(json, required).map {
                    require(OffsetDateTime.parse(it.text("validUntil")).isAfter(OffsetDateTime.now().plusMinutes(5))) {
                        "Council invitations are expired or about to expire; export fresh fixtures"
                    }
                    CouncilRegistrationFixture(
                        it.text("subject"),
                        UUID.fromString(it.text("validToken")).toString(),
                        UUID.fromString(it.text("expiredToken")).toString(),
                        it.text("email"),
                        it.text("councilName"),
                        it.boolean("invitedAsAdmin"),
                    )
                }
            val tokens = fixtures.flatMap { listOf(it.validToken, it.expiredToken) }
            require(tokens.distinct().size == tokens.size) { "Invitations must not be shared across passes" }
            return fixtures.take(required)
        }
    }
}
