package uk.gov.communities.prsdb.webapp.database.entity

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap

@Entity
class FormContext() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    lateinit var journeyType: JourneyType
        private set

    @Column(columnDefinition = "TEXT", nullable = false)
    lateinit var context: String

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "subject_identifier",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_FORM_CONTEXT_1L_USER"),
    )
    lateinit var user: OneLoginUser
        private set

    constructor(journeyType: JourneyType, context: String, user: OneLoginUser) : this() {
        this.journeyType = journeyType
        this.context = context
        this.user = user
    }

    fun toJourneyData(): JourneyData = objectToStringKeyedMap(ObjectMapper().readValue(context, Any::class.java)) ?: emptyMap()
}
