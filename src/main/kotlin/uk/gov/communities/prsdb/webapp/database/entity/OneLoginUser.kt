package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class OneLoginUser(
    @Id val id: String = "",
) : AuditableEntity()
