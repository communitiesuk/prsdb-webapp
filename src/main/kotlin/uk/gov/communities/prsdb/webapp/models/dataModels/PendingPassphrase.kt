package uk.gov.communities.prsdb.webapp.models.dataModels

import java.io.Serializable

data class PendingPassphrase(
    val token: String,
    val passphrase: String,
) : Serializable
