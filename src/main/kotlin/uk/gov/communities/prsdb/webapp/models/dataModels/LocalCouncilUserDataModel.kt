package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUser

@Serializable
data class LocalCouncilUserDataModel(
    val id: Long,
    val userName: String,
    val localCouncilName: String,
    val isManager: Boolean,
    val email: String,
    val isPending: Boolean = false,
) {
    companion object {
        fun fromLocalCouncilUser(localCouncilUser: LocalCouncilUser) =
            LocalCouncilUserDataModel(
                id = localCouncilUser.id,
                userName = localCouncilUser.name,
                localCouncilName = localCouncilUser.localCouncil.name,
                isManager = localCouncilUser.isManager,
                email = localCouncilUser.email,
                isPending = false,
            )
    }
}
