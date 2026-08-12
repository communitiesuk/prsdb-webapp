package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationGoverningBodyMemberRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

@PrsdbWebService
class OrganisationGoverningBodyMemberService(
    private val organisationGoverningBodyMemberRepository: OrganisationGoverningBodyMemberRepository,
    private val addressService: AddressService,
) {
    @Transactional
    fun createGoverningBodyMembers(
        organisationalLandlord: OrganisationalLandlord,
        members: List<GoverningBodyMemberDataModel>,
    ) {
        members.forEach { member ->
            createGoverningBodyMemberEntity(organisationalLandlord, member)
        }
    }

    @Transactional
    fun clearGoverningBodyMembers(organisationLandlord: OrganisationalLandlord) {
        organisationGoverningBodyMemberRepository.deleteByOrganisationalLandlord_Id(organisationLandlord.id)
    }

    @Transactional
    fun updateGoverningBodyMembers(
        organisationalLandlord: OrganisationalLandlord,
        members: List<GoverningBodyMemberDataModel>,
    ) {
        val existingById = organisationalLandlord.governingBodyMembers.associateBy { it.id }

        val initialTimestamps =
            members
                .filter { it.databaseId != null && it.lastUpdatedAt != null }
                .associate { it.databaseId!! to it.lastUpdatedAt!! }

        val staleIds =
            initialTimestamps.filter { (id, timestamp) ->
                val entity = existingById[id]
                entity != null && entity.getMostRecentlyUpdated().toString() != timestamp
            }.keys
        if (staleIds.isNotEmpty()) {
            throw PrsdbWebException("Governing body members have been modified by another session: $staleIds")
        }

        val requestedIds = members.mapNotNull { it.databaseId }
        if (requestedIds.size != requestedIds.toSet().size) {
            throw PrsdbWebException("Duplicate governing body member IDs in update request")
        }

        val foreignIds = requestedIds.filter { it !in existingById }
        if (foreignIds.isNotEmpty()) {
            throw PrsdbWebException("Governing body member IDs not found for this landlord: $foreignIds")
        }

        val retainedIds = requestedIds.toSet()
        val idsToDelete = existingById.keys - retainedIds
        idsToDelete.forEach { id -> organisationGoverningBodyMemberRepository.deleteById(id) }

        members.forEach { member ->
            if (member.databaseId == null) {
                createGoverningBodyMemberEntity(organisationalLandlord, member)
            } else {
                val entity = existingById[member.databaseId]!!
                updateExistingMember(entity, member)
            }
        }
    }

    private fun createGoverningBodyMemberEntity(
        organisationalLandlord: OrganisationalLandlord,
        member: GoverningBodyMemberDataModel,
    ) {
        val memberAddress = addressService.findOrCreateAddress(member.address)
        organisationGoverningBodyMemberRepository.save(
            OrganisationGoverningBodyMember(
                organisationalLandlord = organisationalLandlord,
                type = member.type,
                name = member.name,
                dateOfBirth = member.dateOfBirth.toJavaLocalDate(),
                address = memberAddress,
            ),
        )
    }

    private fun updateExistingMember(
        entity: OrganisationGoverningBodyMember,
        member: GoverningBodyMemberDataModel,
    ) {
        var changed = false

        if (entity.type != member.type) {
            entity.type = member.type
            changed = true
        }
        if (entity.name != member.name) {
            entity.name = member.name
            changed = true
        }
        if (entity.dateOfBirth != member.dateOfBirth.toJavaLocalDate()) {
            entity.dateOfBirth = member.dateOfBirth.toJavaLocalDate()
            changed = true
        }
        if (AddressDataModel.fromAddress(entity.address) != member.address) {
            entity.address = addressService.findOrCreateAddress(member.address)
            changed = true
        }

        if (changed) {
            organisationGoverningBodyMemberRepository.save(entity)
        }
    }
}
