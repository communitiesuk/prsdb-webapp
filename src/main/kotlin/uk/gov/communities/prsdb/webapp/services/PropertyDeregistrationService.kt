package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DEREGISTERED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyDeregistrationEmailDetails
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

@PrsdbWebService
class PropertyDeregistrationService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
) {
    @Transactional
    fun deregisterProperty(propertyOwnershipId: Long): PropertyDeregistrationEmailDetails {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val emailDetails =
            PropertyDeregistrationEmailDetails(
                landlordEmailAddresses = propertyOwnership.landlords.map { it.email },
                prn = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                singleLineAddress = propertyOwnership.address.singleLineAddress,
            )
        propertyOwnershipService.deletePropertyOwnership(propertyOwnershipId)
        return emailDetails
    }

    fun setDeregisteredPropertyInSession(
        propertyOwnershipId: Long,
        singleLineAddress: String,
    ) = session.setAttribute(
        PROPERTY_DEREGISTERED_THIS_SESSION,
        propertyOwnershipId to singleLineAddress,
    )

    fun getDeregisteredPropertyOwnershipIdFromSession(): Long? = getDeregisteredPropertyFromSession()?.first

    fun getDeregisteredPropertyAddress(): String? = getDeregisteredPropertyFromSession()?.second

    @Suppress("UNCHECKED_CAST")
    private fun getDeregisteredPropertyFromSession(): Pair<Long, String>? =
        session.getAttribute(PROPERTY_DEREGISTERED_THIS_SESSION) as Pair<Long, String>?
}
