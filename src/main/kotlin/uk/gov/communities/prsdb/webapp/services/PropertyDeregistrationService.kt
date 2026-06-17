package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordEmailRecipient
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyDeregistrationEmailDetails

@PrsdbWebService
class PropertyDeregistrationService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val session: HttpSession,
) {
    @Transactional
    fun deregisterProperty(propertyOwnershipId: Long): PropertyDeregistrationEmailDetails {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val (pendingInvitations, _) = jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership)
        val emailDetails =
            PropertyDeregistrationEmailDetails(
                landlordRecipients = propertyOwnership.landlords.map { LandlordEmailRecipient(it.name, it.email) },
                cancelledInvitationEmailAddresses = pendingInvitations.map { it.invitedEmail },
                singleLineAddress = propertyOwnership.address.singleLineAddress,
                multiLineAddress = propertyOwnership.address.toMultiLineAddress(),
            )
        propertyOwnershipService.deletePropertyOwnership(propertyOwnershipId)
        return emailDetails
    }

    fun addDeregisteredPropertyOwnershipIdToSession(
        propertyOwnershipId: Long,
        singleLineAddress: String? = null,
    ) = session.setAttribute(
        PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES,
        getDeregisteredPropertiesFromSession() + (propertyOwnershipId to singleLineAddress),
    )

    fun getDeregisteredPropertyOwnershipIdsFromSession(): MutableList<Long> = getDeregisteredPropertiesFromSession().keys.toMutableList()

    fun getDeregisteredPropertyAddress(propertyOwnershipId: Long): String? = getDeregisteredPropertiesFromSession()[propertyOwnershipId]

    @Suppress("UNCHECKED_CAST")
    private fun getDeregisteredPropertiesFromSession(): MutableMap<Long, String?> =
        session.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES) as MutableMap<Long, String?>?
            ?: mutableMapOf()
}
