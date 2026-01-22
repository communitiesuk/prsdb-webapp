package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.ConfirmIdentityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityNotVerifiedStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityVerifyingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel

interface IdentityState : JourneyState {
    val identityVerifyingStep: IdentityVerifyingStep
    val confirmIdentityStep: ConfirmIdentityStep
    val identityNotVerifiedStep: IdentityNotVerifiedStep
    val nameStep: NameStep
    val dateOfBirthStep: DateOfBirthStep
    var verifiedIdentity: VerifiedIdentityDataModel?

    fun getNotNullVerifiedIdentity(): VerifiedIdentityDataModel =
        verifiedIdentity ?: throw NotNullFormModelValueIsNullException("No verified identity found in IdentityState")
}
