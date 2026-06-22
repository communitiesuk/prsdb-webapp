package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_LEFT_THIS_SESSION
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

@PrsdbWebService
class NoLongerALandlordService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
) {
    fun getPropertyOwnershipIfUserCanLeave(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): PropertyOwnership {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val isLandlordOnProperty = propertyOwnership.landlords.any { it.baseUser.id == baseUserId }
        val isJointlyOwned = propertyOwnership.landlords.size >= 2
        if (!isLandlordOnProperty || !isJointlyOwned) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User $baseUserId is not authorised to leave property ownership $propertyOwnershipId",
            )
        }
        return propertyOwnership
    }

    fun addLeftPropertyOwnershipToSession(propertyOwnership: PropertyOwnership) =
        session.setAttribute(
            PROPERTIES_LEFT_THIS_SESSION,
            getLeftPropertyOwnershipsFromSession() + (propertyOwnership.id to propertyOwnership.address.singleLineAddress),
        )

    @Suppress("UNCHECKED_CAST")
    fun getLeftPropertyOwnershipsFromSession(): MutableMap<Long, String> =
        session.getAttribute(PROPERTIES_LEFT_THIS_SESSION) as MutableMap<Long, String>?
            ?: mutableMapOf()
}
