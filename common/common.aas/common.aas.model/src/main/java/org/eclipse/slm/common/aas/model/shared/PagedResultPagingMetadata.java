package org.eclipse.slm.common.aas.model.shared;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PagedResultPagingMetadata {

	@JsonProperty("cursor")
	private String cursor = null;

    @JsonProperty("resultType")
    private String resultType = null;

	public PagedResultPagingMetadata cursor(String cursor) {
		this.cursor = cursor;
		return this;
	}

    public PagedResultPagingMetadata resultType(String resultType) {
        this.resultType = resultType;
        return this;
    }

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PagedResultPagingMetadata pagedResultPagingMetadata = (PagedResultPagingMetadata) o;
		return Objects.equals(this.cursor, pagedResultPagingMetadata.cursor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cursor);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PagedResultPagingMetadata {\n");

		sb.append("    cursor: ").append(toIndentedString(cursor)).append("\n");
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
