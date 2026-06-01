package uk.gov.communities.prsdb.webapp.models.viewModels.landlordsTab

import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.time.LocalDate

// TODO PDJB-299: pendingInvitations, expiredInvitations and joinRequests are
// always empty for now. Populating them depends on data and workflow that do
// not yet exist (invitation expiry / accept-decline flow, joint landlord
// requests). The DOM scaffolding is already in place in
// `fragments/propertyDetails/landlordsTab.html` so that future tickets only
// need to wire up data.
data class PropertyLandlordsTabViewModel(
    val registeredLandlords: List<RegisteredLandlordCard>,
    val pendingInvitations: List<PendingInvitation>,
    val expiredInvitations: List<ExpiredInvitation>,
    val joinRequests: List<JoinRequest>,
) {
    data class RegisteredLandlordCard(
        val name: String,
        val isCurrentUser: Boolean,
        val landlordRegistrationNumber: String,
        val email: String,
    )

    data class PendingInvitation(
        val email: String,
        val expiresIn: String,
        val expiresOn: LocalDate,
        val sentOn: LocalDate,
    )

    data class ExpiredInvitation(
        val email: String,
        val expiredOn: LocalDate,
    )

    data class JoinRequest(
        val name: String,
        val receivedOn: LocalDate,
    )

    companion object {
        fun fromPropertyOwnership(
            propertyOwnership: PropertyOwnership,
            currentBaseUserId: String,
        ): PropertyLandlordsTabViewModel {
            val primaryLandlord = propertyOwnership.primaryLandlord
            val primaryCard =
                RegisteredLandlordCard(
                    name = primaryLandlord.name,
                    isCurrentUser = primaryLandlord.baseUser.id == currentBaseUserId,
                    landlordRegistrationNumber =
                        RegistrationNumberDataModel
                            .fromRegistrationNumber(primaryLandlord.registrationNumber)
                            .toString(),
                    email = primaryLandlord.email,
                )

            return PropertyLandlordsTabViewModel(
                registeredLandlords = listOf(primaryCard),
                pendingInvitations = emptyList(),
                expiredInvitations = emptyList(),
                joinRequests = emptyList(),
            )
        }
    }
}
