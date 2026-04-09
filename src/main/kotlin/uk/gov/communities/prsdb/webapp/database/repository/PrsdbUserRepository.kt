package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser

interface PrsdbUserRepository : JpaRepository<PrsdbUser, String>
