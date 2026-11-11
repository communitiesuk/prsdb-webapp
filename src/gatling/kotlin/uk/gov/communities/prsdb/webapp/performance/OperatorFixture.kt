package uk.gov.communities.prsdb.webapp.performance

import uk.gov.communities.prsdb.webapp.performance.CouncilFixtureFields.boolean
import uk.gov.communities.prsdb.webapp.performance.CouncilFixtureFields.id
import uk.gov.communities.prsdb.webapp.performance.CouncilFixtureFields.text

data class OperatorFixture(
    val subject: String,
    val councilId: Long,
    val councilName: String,
    val targetId: Long,
    val targetName: String,
    val targetEmail: String,
    val targetSubject: String,
    val targetManager: Boolean,
) {
    companion object {
        fun parse(
            json: String,
            required: Int,
        ): List<OperatorFixture> {
            val fixtures =
                CouncilFixtureFields.rows(json, required).map {
                    OperatorFixture(
                        it.text("subject"),
                        it.id("councilId"),
                        it.text("councilName"),
                        it.id("targetId"),
                        it.text("targetName"),
                        it.text("targetEmail"),
                        it.text("targetSubject"),
                        it.boolean("targetManager"),
                    )
                }
            require(fixtures.map { it.targetId }.distinct().size == fixtures.size) { "Administrator targets must not be shared" }
            require(fixtures.none { target -> fixtures.any { it.subject == target.targetSubject } }) { "Actors cannot be mutation targets" }
            return fixtures.take(required)
        }
    }
}
