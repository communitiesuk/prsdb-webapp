package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.ConfirmIdentityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityNotVerifiedStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityVerifyingStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import java.time.LocalDate

interface IdentityState : JourneyState {
    val identityVerifyingStep: IdentityVerifyingStep
    val confirmIdentityStep: ConfirmIdentityStep
    val identityNotVerifiedStep: IdentityNotVerifiedStep
    val nameStep: NameStep
    val dateOfBirthStep: DateOfBirthStep
    var verifiedIdentity: VerifiedIdentityDataModel?

    fun getNotNullVerifiedIdentity(): VerifiedIdentityDataModel =
        verifiedIdentity ?: throw NotNullFormModelValueIsNullException("No verified identity found in IdentityState")

    fun getIsIdentityVerified(): Boolean = verifiedIdentity != null

    fun getName(): String = verifiedIdentity?.name ?: nameStep.formModel.notNullValue(NameFormModel::name)

    fun getDateOfBirth(): LocalDate {
        val dobOrNull = verifiedIdentity?.birthDate ?: dateOfBirthStep.formModel.toLocalDateOrNull()
        return dobOrNull ?: throw NotNullFormModelValueIsNullException("No date of birth found in IdentityState")
    }
}
