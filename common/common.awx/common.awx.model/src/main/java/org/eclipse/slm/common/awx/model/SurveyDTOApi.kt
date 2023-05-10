package org.eclipse.slm.common.awx.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("Survey")
data class SurveyDTOApi(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("description")
    val description: String,
) {
    @JsonProperty("spec")
    var spec: MutableList<SurveyItemDTOApi> = ArrayList()

    constructor(survey: Survey) : this(
        survey.name,
        survey.description
    ) {
        survey.spec.forEach{
            spec.add(SurveyItemDTOApi(it))
        }
    }
}
