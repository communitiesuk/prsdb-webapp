package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyStep
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository

@Service
class JourneyService(
    val formContextRepository: FormContextRepository,
    val oneLoginUserRepository: OneLoginUserRepository,
) {
    fun getJourneyView(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
        formContextId: Long? = null,
    ): String {
        if (formContextId != null) {
            val retrieveFormContext = formContextRepository.findById(formContextId)
            if (retrieveFormContext.isPresent) {
                val formContext = retrieveFormContext.get()
                validateStep(journeyType, journeyStep, formContext.context)
            } else {
                throw Exception("Invalid Form Context Id")
            }
        }
        return getView(journeyType, journeyStep)
    }

    // TODO - throws Exception("Invalid journey step")
    private fun validateStep(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
        context: String,
    ): Boolean = true

    // TODO
    private fun getView(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
    ): String = "view"

    fun updateFormContextAndGetNextStep(
        journeyType: JourneyType,
        previousStep: JourneyStep,
        userId: String,
        formData: String,
        formContextId: Long? = null,
    ): String {
        validateFormData(previousStep, formData)
        val formContext: FormContext
        if (formContextId != null) {
            val retrieveFormContext = formContextRepository.findById(formContextId)
            if (retrieveFormContext.isPresent) {
                formContext = retrieveFormContext.get()
                // TODO:
                //      formContext.context -> journeyModel (based on journeyType)
                //      journeyModel.add(formData)
                //      journeyModel -> string
                formContext.context += formData
                formContextRepository.save(formContext)
            } else {
                throw Exception("Invalid Form Context Id")
            }
        } else {
            val user = oneLoginUserRepository.getReferenceById(userId)
            // TODO:
            //       new journeyModel (based on journeyType)
            //       journeyModel.add(formData)
            //       journeyModel -> string
            formContext = FormContext(journeyType, formData, user)
            formContextRepository.save(formContext)
        }
        return getRedirectUrl(journeyType, previousStep, formContext.context)
    }

    // TODO - throws Exception("Invalid form data")
    private fun validateFormData(
        step: JourneyStep,
        formData: String,
    ): Boolean = true

    // TODO
    private fun getRedirectUrl(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
        context: String,
    ): String = "redirect"
}
