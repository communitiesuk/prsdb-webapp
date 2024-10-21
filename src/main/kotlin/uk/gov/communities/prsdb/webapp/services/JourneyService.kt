package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
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
        principalName: String,
        formContextId: Long? = null,
    ): String {
        if (formContextId != null) {
            val formContext = formContextRepository.findById(formContextId).get()
            if (principalName == formContext.id.toString()) {
                validateFormContextForStep(journeyType, journeyStep, formContext)
            } else {
                throw Exception("Should this be a 404??")
            }
        }
        return getView(journeyType, journeyStep)
    }

    // TODO - throws Exception("Invalid journey step")
    private fun validateFormContextForStep(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
        formContext: FormContext,
    ): Boolean {
        // TODO add validation process
        // TODO - if not valid throws Exception("Invalid journey step")

        return true
    }

    // TODO
    private fun getView(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
    ): String = "view"

    fun updateFormContextAndGetNextStep(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
        principalName: String,
        formData: String,
    ): String {
        validateFormData(journeyStep, formData)
        val user = oneLoginUserRepository.findById(principalName).get()
        val context = getMappedData(formData)
        val formContext = FormContext(journeyType, context.toString(), user)
        val formContextId = formContextRepository.save(formContext).id
        return getRedirectUrl(journeyType, journeyStep, context, formContextId)

//        if (formContextId != null) {
//            val retrieveFormContext = formContextRepository.findById(formContextId)
//            if (retrieveFormContext.isPresent) {
//                formContext = retrieveFormContext.get()
//                // TODO:
//                //      formContext.context -> journeyModel (based on journeyType)
//                //      journeyModel.add(formData)
//                //      journeyModel -> string
//                formContext.context += formData
//                formContextRepository.save(formContext)
//            } else {
//                throw Exception("Invalid Form Context Id")
//            }
//        } else {
//            val user = oneLoginUserRepository.findById(principal.name).get()
//            // TODO:
//            //       new journeyModel (based on journeyType)
//            //       journeyModel.add(formData)
//            //       journeyModel -> string
//            formContext = FormContext(journeyType, formData, user)
//            formContextRepository.save(formContext)
//        }
//        return getRedirectUrl(journeyType, journeyStep, formContext.context)
    }

    private fun getMappedData(formData: String): Map<String, JsonElement> {
        val data = Json.parseToJsonElement(formData)
        require(data is JsonObject) { "Only Json Objects can be converted to a Map" }
        return data
    }

    private fun validateFormData(
        step: JourneyStep,
        formData: String,
    ): Boolean {
        // TODO add validation process
        // TODO - if not valid throws Exception("Invalid form data")

        return true
    }

    // TODO get the redirect url AND add the formContextId as a Request param!!
    private fun getRedirectUrl(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
        context: Map<String, JsonElement>,
        id: Long?,
    ): String = "redirect"
}
