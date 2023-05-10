package org.eclipse.slm.common.awx.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("SurveyItem")
data class SurveyItemDTOApi (
    @JsonProperty("type")
    val type: SurveyItemType,
    @JsonProperty("question_name")
    val questionName: String,
    @JsonProperty("question_description")
    val questionDescription: String,
    @JsonProperty("variable")
    val variable: String,
) {
    val choices: String
        get() = this.choicesList.joinToString(separator = "\n")
    @JsonIgnore
    var choicesList: List<String> = ArrayList()
    var min: Int? = null
    var max: Int? = null
    var required: Boolean = false
    var default: String = ""

    constructor(
        type:SurveyItemType,
        questionName: String,
        questionDescription: String,
        variable: String,
        choicesList: List<String>,
        min: Int?,
        max: Int?,
        required: Boolean,
        default: String
    ) : this(type, questionName, questionDescription, variable) {
        this.choicesList = choicesList
        this.min = min
        this.max = max
        this.required = required
        this.default = default
    }

    constructor(survey: SurveyItem) : this(
        survey.type,
        survey.questionName,
        survey.questionDescription,
        survey.variable,
        survey.choices,
        survey.min,
        survey.max,
        survey.required,
        survey.default
    ) {

    }

}
