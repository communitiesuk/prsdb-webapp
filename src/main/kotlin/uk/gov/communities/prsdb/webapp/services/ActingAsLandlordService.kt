package uk.gov.communities.prsdb.webapp.services

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository

@PrsdbWebService
class ActingAsLandlordService(
    private val actingAsLandlordSessionService: ActingAsLandlordSessionService,
    private val landlordService: LandlordService,
    private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
    private val systemOperatorRepository: SystemOperatorRepository,
    private val securityContextService: SecurityContextService,
) {
    // Every check runs before the reset, so a rejected request leaves the system operator's session untouched
    fun startActingAsLandlord(
        landlordId: Long,
        memberId: Long?,
    ) {
        val userId = getUserIdToActAs(landlordId, memberId)
        if (systemOperatorRepository.findByBaseUser_Id(userId) != null) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot act as a user who is a system operator")
        }

        securityContextService.resetSession()
        actingAsLandlordSessionService.setActingAsUserId(userId)
    }

    fun stopActing() = securityContextService.resetSession()

    private fun getUserIdToActAs(
        landlordId: Long,
        memberId: Long?,
    ): String =
        when (val landlord = landlordService.retrieveLandlordById(landlordId)) {
            null -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Landlord $landlordId not found")
            is IndividualLandlord -> landlord.baseUser.id
            is OrganisationalLandlord -> {
                if (memberId == null) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A member ID is needed to act as an organisation")
                }
                organisationalLandlordUserRepository
                    .findById(memberId)
                    .orElse(null)
                    ?.takeIf { it.organisationalLandlord.id == landlord.id }
                    ?.baseUser
                    ?.id
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Member $memberId not found in landlord $landlordId")
            }
            else -> throw IllegalStateException("Unknown landlord type ${landlord::class}")
        }
}
