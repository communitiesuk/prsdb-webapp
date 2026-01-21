package uk.gov.communities.prsdb.webapp.database.entity

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Entity
@Table(uniqueConstraints = [UniqueConstraint(name = "uc_userJourney", columnNames = ["journey_id", "subject_identifier"])])
class SavedJourneyState() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    lateinit var journeyId: String

    @Column(columnDefinition = "TEXT", nullable = false)
    lateinit var serializedState: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false)
    lateinit var user: OneLoginUser
        private set

    @OneToOne(mappedBy = "savedJourneyState", orphanRemoval = true, optional = true)
    var reminderEmailSent: ReminderEmailSent? = null
        private set

    constructor(serializedState: String, user: OneLoginUser, journeyId: String) : this() {
        this.serializedState = serializedState
        this.user = user
        this.journeyId = journeyId
    }

    constructor(user: OneLoginUser, journeyId: String) : this(serializedState = "{}", user, journeyId)

    fun getPropertyRegistrationSingleLineAddress(): String {
        val stateDataMap = objectMapper.readValue(serializedState, Map::class.java)
        val submittedJourneyData = stateDataMap["journeyData"] as Map<*, *>
        val selectedAddressData = submittedJourneyData["select-address"] as? Map<*, *>
        val selectedAddress = selectedAddressData?.get("address") as? String
        val serializedCachedAddressData = stateDataMap["cachedAddresses"] as String
        val cachedAddressData: List<AddressDataModel> = Json.decodeFromString(serializedCachedAddressData)

        return if (cachedAddressData.any { it.singleLineAddress == selectedAddress }) {
            selectedAddress!!
        } else {
            val manualAddressData = submittedJourneyData["manual-address"] as Map<*, *>
            val localCouncilData = submittedJourneyData["local-council"] as Map<*, *>
            AddressDataModel
                .fromManualAddressData(
                    addressLineOne = manualAddressData["addressLineOne"] as String,
                    addressLineTwo = manualAddressData["addressLineTwo"] as String?,
                    townOrCity = manualAddressData["townOrCity"] as String,
                    county = manualAddressData["county"] as String?,
                    postcode = manualAddressData["postcode"] as String,
                    localCouncilId = localCouncilData["localCouncilId"] as Int?,
                ).singleLineAddress
        }
    }

    companion object {
        private val objectMapper = ObjectMapper()
    }
}
