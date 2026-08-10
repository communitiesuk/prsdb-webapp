package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember

@Repository
interface OrganisationGoverningBodyMemberRepository : JpaRepository<OrganisationGoverningBodyMember, Long>
