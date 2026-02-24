package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
class OneLoginUser(
    @Id val id: String = "",
) : AuditableEntity() {
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private lateinit var formContexts: MutableSet<FormContext>

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private lateinit var savedJourneyStates: MutableSet<SavedJourneyState>

    @OneToOne(mappedBy = "baseUser", orphanRemoval = true)
    private val passcode: Passcode? = null
}
