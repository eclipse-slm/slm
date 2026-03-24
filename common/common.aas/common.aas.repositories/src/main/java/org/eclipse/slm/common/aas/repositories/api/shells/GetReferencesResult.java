package org.eclipse.slm.common.aas.repositories.api.shells;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GetReferencesResult extends PagedResult {

	@JsonProperty("result")
	@Valid
	private List<Reference> result = null;

	public GetReferencesResult result(List<Reference> result) {
		this.result = result;
		return this;
	}

	public GetReferencesResult addResultItem(Reference resultItem) {
		if (this.result == null) {
			this.result = new ArrayList<Reference>();
		}
		this.result.add(resultItem);
		return this;
	}

	@Schema(description = "")
	@Valid
	public List<Reference> getResult() {
		return result;
	}

	public void setResult(List<Reference> result) {
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
		GetReferencesResult getReferencesResult = (GetReferencesResult) o;
		return Objects.equals(this.result, getReferencesResult.result) && super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(result, super.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GetReferencesResult {\n");
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
