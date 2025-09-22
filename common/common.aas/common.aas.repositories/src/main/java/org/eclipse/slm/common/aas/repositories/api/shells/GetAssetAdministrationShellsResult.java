package org.eclipse.slm.common.aas.repositories.api.shells;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GetAssetAdministrationShellsResult extends PagedResult {

	@JsonProperty("result")
	@Valid
	private List<AssetAdministrationShell> result = null;

	public GetAssetAdministrationShellsResult result(List<AssetAdministrationShell> result) {
		this.result = result;
		return this;
	}

	public GetAssetAdministrationShellsResult addResultItem(AssetAdministrationShell resultItem) {
		if (this.result == null) {
			this.result = new ArrayList<AssetAdministrationShell>();
		}
		this.result.add(resultItem);
		return this;
	}

	@Valid
	public List<AssetAdministrationShell> getResult() {
		return result;
	}

	public void setResult(List<AssetAdministrationShell> result) {
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
		GetAssetAdministrationShellsResult getAssetAdministrationShellsResult = (GetAssetAdministrationShellsResult) o;
		return Objects.equals(this.result, getAssetAdministrationShellsResult.result) && super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(result, super.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GetAssetAdministrationShellsResult {\n");
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
