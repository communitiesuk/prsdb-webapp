package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityNotVerifiedStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import java.time.LocalDate

interface IdentityStateBuilder<out SelfType : IdentityStateBuilder<SelfType>> {
    fun self(): SelfType

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun withIdentityNotVerified(): SelfType {
        withSubmittedValue(IdentityNotVerifiedStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withName(name: String = "Arthur Dent"): SelfType {
        val nameFormModel = NameFormModel().apply { this.name = name }
        withSubmittedValue(NameStep.ROUTE_SEGMENT, nameFormModel)
        return self()
    }

    fun withDateOfBirth(dob: LocalDate = LocalDate.of(2000, 6, 8)): SelfType {
        val dobFormModel =
            DateOfBirthFormModel().apply {
                day = dob.dayOfMonth.toString()
                month = dob.monthValue.toString()
                year = dob.year.toString()
            }
        withSubmittedValue(DateOfBirthStep.ROUTE_SEGMENT, dobFormModel)
        return self()
    }
}
