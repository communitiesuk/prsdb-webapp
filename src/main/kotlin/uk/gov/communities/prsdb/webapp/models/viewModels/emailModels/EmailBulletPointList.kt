package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class EmailBulletPointList(
    val bulletPoints: List<String>,
) {
    override fun toString() = bulletPoints.joinToString("\n") { "* $it" }

    constructor(vararg bulletPoints: String) : this(bulletPoints.toList())
}
