package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

data class FileNameInfo(
    val propertyOwnershipId: Long,
    val fileCategory: FileCategory,
    val extension: String,
) {
    override fun toString() = "property_${propertyOwnershipId}_${fileCategory.categoryName}.$extension"

    enum class FileCategory(
        val categoryName: String,
    ) {
        Eirc("eicr"),
        GasSafetyCert("gas_safety_certificate"),
        ;

        companion object {
            fun fromCategoryNameOrNull(name: String): FileCategory? = FileCategory.entries.singleOrNull { it.categoryName == name }
        }
    }

    companion object {
        fun parse(fileName: String): FileNameInfo {
            val nameAndExtension = fileName.split('.')
            if (nameAndExtension.size != 2) {
                throw invalidFilenameException(fileName)
            }

            val extension = nameAndExtension[1]
            val nameParts = nameAndExtension[0].split("_")
            if (nameParts.size < 3) {
                throw invalidFilenameException(fileName)
            }
            val propertyOwnershipId = nameParts[1].toLongOrNull() ?: throw invalidFilenameException(fileName)

            val categoryString = nameParts.drop(2).joinToString("_") { it }
            val fileCategory = FileCategory.fromCategoryNameOrNull(categoryString) ?: throw invalidFilenameException(fileName)

            return FileNameInfo(
                propertyOwnershipId,
                fileCategory,
                extension,
            )
        }

        private fun invalidFilenameException(fileName: String): PrsdbWebException =
            PrsdbWebException("Filename \"$fileName\" does not have form \"property_{propertyOwnershipId}_{fileCategory}.{extension}\"")
    }
}
