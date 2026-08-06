package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationGoverningBodyMemberRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

@PrsdbWebService
class OrganisationGoverningBodyMemberService(
    private val organisationGoverningBodyMemberRepository: OrganisationGoverningBodyMemberRepository,
    private val addressService: AddressService,
) {
    @Transactional
    fun createGoverningBodyMembers(
        organisationLandlord: OrganisationLandlord,
        members: List<GoverningBodyMemberDataModel>,
    ) {
        members.forEach { member ->
            val memberAddress = addressService.findOrCreateAddress(member.address)
            organisationGoverningBodyMemberRepository.save(
                OrganisationGoverningBodyMember(
                    organisationLandlord = organisationLandlord,
                    type = member.type,
                    name = member.name,
                    dateOfBirth = member.dateOfBirth.toJavaLocalDate(),
                    address = memberAddress,
                ),
            )
        }
    }
}
