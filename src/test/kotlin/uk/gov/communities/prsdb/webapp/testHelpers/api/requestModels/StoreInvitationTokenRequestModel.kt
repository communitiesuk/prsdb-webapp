package uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels

import java.util.UUID

data class StoreInvitationTokenRequestModel(
    val token: String,
) {
    constructor(token: UUID) : this(token.toString())
}
