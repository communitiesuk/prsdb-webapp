package uk.gov.communities.prsdb.webapp.forms.pages

import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService

class PropertyRegistrationCheckAnswersPage(
    addressDataService: AddressDataService,
) : Page(
        NoInputFormModel::class,
        "forms/propertyRegistrationCheckAnswersForm",
        mapOf(
            "title" to "registerProperty.title",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        ),
    )
