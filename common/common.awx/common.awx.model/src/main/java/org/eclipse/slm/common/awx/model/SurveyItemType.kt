package org.eclipse.slm.common.awx.model

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("SurveyItemType")
enum class SurveyItemType {
    text,
    password,
    integer,
    float,
    multiplechoice,
    multiselect
}
