package org.eclipse.slm.resource_management.features.profiler

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.awx.model.SurveyItem
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType

@JsonTypeName("AwxAction")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class AwxAction(): Action(AwxAction::class.java.simpleName) {

    var awxRepo: String = ""

    var awxBranch: String = ""

    var playbook: String = ""

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var username: String = ""

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var password: String = ""

    var parameter: List<SurveyItem>? = null

    constructor(
         awxRepo: String,
         awxBranch: String,
         playbook: String
    ) : this() {
        this.actionType = actionType
        this.awxRepo = awxRepo
        this.awxBranch = awxBranch
        this.playbook = playbook
    }

    constructor(
        awxRepo: String,
        awxBranch: String,
        playbook: String,
        connectionTypes: HashSet<ConnectionType>
    ) : this(awxRepo, awxBranch, playbook) {
        super.connectionTypes = connectionTypes
    }

    fun hasSameParameter(other: AwxAction): Boolean {
        return parameterFields() == other.parameterFields()
    }

    // SurveyItem.equals only covers its constructor fields; null and empty are stored the same way
    private fun parameterFields(): List<List<Any?>> {
        return parameter.orEmpty().map {
            listOf(
                it.type, it.questionName, it.questionDescription, it.variable,
                it.choices, it.min, it.max, it.required, it.default
            )
        }
    }

    // Hibernate uses this to detect changes of the JSON column, so it has to cover every stored field.
    // username and password are write-only and therefore not stored.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AwxAction) return false

        if (actionClass != other.actionClass) return false
        if (actionType != other.actionType) return false
        if (skipable != other.skipable) return false
        if (configParameters != other.configParameters) return false
        if (connectionTypes != other.connectionTypes) return false
        if (awxRepo != other.awxRepo) return false
        if (awxBranch != other.awxBranch) return false
        if (playbook != other.playbook) return false

        return hasSameParameter(other)
    }

    override fun hashCode(): Int {
        var result = actionClass.hashCode()
        result = 31 * result + (actionType?.hashCode() ?: 0)
        result = 31 * result + skipable.hashCode()
        result = 31 * result + configParameters.hashCode()
        result = 31 * result + connectionTypes.hashCode()
        result = 31 * result + awxRepo.hashCode()
        result = 31 * result + awxBranch.hashCode()
        result = 31 * result + playbook.hashCode()
        result = 31 * result + parameterFields().hashCode()
        return result
    }

}