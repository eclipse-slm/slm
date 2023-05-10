package org.eclipse.slm.common.awx.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("SurveyItem")
data class SurveyItem (
    @JsonProperty("type")
    val type: SurveyItemType,
    @JsonProperty("question_name")
    val questionName: String,
    @JsonProperty("question_description")
    val questionDescription: String,
    @JsonProperty("variable")
    val variable: String,
) {
    var choices: List<String> = ArrayList()
    var min: Int? = null
    var max: Int? = null
    var required: Boolean = false
    var default: String = ""

    constructor(
        type:SurveyItemType,
        questionName: String,
        questionDescription: String,
        variable: String,
        choices: List<String>,
        min: Int?,
        max: Int?,
        required: Boolean,
        default: String
    ) : this(type, questionName, questionDescription, variable) {
        this.choices = choices
        this.min = min
        this.max = max
        this.required = required
        this.default = default
    }

    constructor(surveyItemDTOApi: SurveyItemDTOApi) : this(
        surveyItemDTOApi.type,
        surveyItemDTOApi.questionName,
        surveyItemDTOApi.questionDescription,
        surveyItemDTOApi.variable,
        surveyItemDTOApi.choicesList,
        surveyItemDTOApi.min,
        surveyItemDTOApi.max,
        surveyItemDTOApi.required,
        surveyItemDTOApi.default
    )


}
