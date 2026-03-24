package org.eclipse.slm.common.aas.model.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.util.Objects;


public class PagedResult {

	@JsonProperty("paging_metadata")
	private PagedResultPagingMetadata pagingMetadata = null;

	public PagedResult pagingMetadata(PagedResultPagingMetadata pagingMetadata) {
		this.pagingMetadata = pagingMetadata;
		return this;
	}

	@Valid
	public PagedResultPagingMetadata getPagingMetadata() {
		return pagingMetadata;
	}

	public void setPagingMetadata(PagedResultPagingMetadata pagingMetadata) {
		this.pagingMetadata = pagingMetadata;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PagedResult pagedResult = (PagedResult) o;
		return Objects.equals(this.pagingMetadata, pagedResult.pagingMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pagingMetadata);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PagedResult {\n");

		sb.append("    pagingMetadata: ").append(toIndentedString(pagingMetadata)).append("\n");
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
