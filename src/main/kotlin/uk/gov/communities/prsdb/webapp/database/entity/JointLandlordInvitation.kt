package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.util.UUID

@Entity
class JointLandlordInvitation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : AuditableEntity() {
    @Column(nullable = false, unique = true)
    lateinit var token: UUID
        private set

    @Column(nullable = false)
    lateinit var invitedEmail: String
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "registered_propertyid", nullable = false)
    lateinit var registeredOwnership: PropertyOwnership
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "inviting_landlord_id", nullable = false)
    lateinit var invitingLandlord: Landlord
        private set

    constructor(
        token: UUID,
        email: String,
        registeredPropertyId: PropertyOwnership,
        invitingLandlord: Landlord,
    ) : this() {
        this.token = token
        this.invitedEmail = email
        this.registeredOwnership = registeredPropertyId
        this.invitingLandlord = invitingLandlord
    }

    val isExpired: Boolean
        get() {
            val dateTimeHelper = DateTimeHelper()

            val expiresOnDate =
                DateTimeHelper
                    .getDateInUK(createdDate.toKotlinInstant())
                    .plus(DatePeriod(days = JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS))

            return dateTimeHelper.getCurrentDateInUK() > expiresOnDate
        }

    constructor(
        id: Long,
        token: UUID,
        email: String,
        registeredPropertyId: PropertyOwnership,
        invitingLandlord: Landlord,
    ) : this(id) {
        this.token = token
        this.invitedEmail = email
        this.registeredOwnership = registeredPropertyId
        this.invitingLandlord = invitingLandlord
    }
}
