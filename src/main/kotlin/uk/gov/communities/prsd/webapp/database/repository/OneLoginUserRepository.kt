package uk.gov.communities.prsd.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsd.webapp.database.entity.OneLoginUser

interface OneLoginUserRepository : JpaRepository<OneLoginUser?, String?>
