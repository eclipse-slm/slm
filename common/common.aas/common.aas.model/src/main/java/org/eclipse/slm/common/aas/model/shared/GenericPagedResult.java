package org.eclipse.slm.common.aas.model.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenericPagedResult<T> extends PagedResult {

    @JsonProperty("result")
    @Valid
    private List<T> result = null;

    public GenericPagedResult<T> result(List<T> result) {
        this.result = result;
        return this;
    }

    public GenericPagedResult<T>  addResultItem(T resultItem) {
        if (this.result == null) {
            this.result = new ArrayList<T>();
        }
        this.result.add(resultItem);
        return this;
    }

    @Valid
    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var genericPagedResult = (GenericPagedResult<T> ) o;
        return Objects.equals(this.result, genericPagedResult.result) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GenericPagedResult<T>  {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    result: ").append(toIndentedString(result)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
