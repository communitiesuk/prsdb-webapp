package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser

@Serializable
data class LocalAuthorityUserDataModel(
    val id: Long,
    val userName: String,
    val localAuthorityName: String,
    val isManager: Boolean,
    val email: String,
    val isPending: Boolean = false,
) {
    companion object {
        fun fromLocalAuthorityUser(laUser: LocalAuthorityUser) =
            LocalAuthorityUserDataModel(
                id = laUser.id,
                userName = laUser.name,
                localAuthorityName = laUser.localAuthority.name,
                isManager = laUser.isManager,
                email = laUser.email,
                isPending = false,
            )
    }
}
