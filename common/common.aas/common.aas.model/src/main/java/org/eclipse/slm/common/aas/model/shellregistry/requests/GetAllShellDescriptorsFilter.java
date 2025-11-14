package org.eclipse.slm.common.aas.model.shellregistry.requests;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;

public class GetAllShellDescriptorsFilter {
    private Integer limit = 500;
    private String cursor = null;
    private AssetKind assetKind = null;
    private String assetType = null;

    private GetAllShellDescriptorsFilter(Builder builder) {
        this.limit = builder.limit;
        this.cursor = builder.cursor;
        this.assetKind = builder.assetKind;
        this.assetType = builder.assetType;
    }

    public Integer getLimit() {
        return limit;
    }

    public String getCursor() {
        return cursor;
    }

    public AssetKind getAssetKind() {
        return assetKind;
    }

    public String getAssetType() {
        return assetType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer limit;
        private String cursor;
        private AssetKind assetKind;
        private String assetType;

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder cursor(String cursor) {
            this.cursor = cursor;
            return this;
        }

        public Builder assetKind(AssetKind assetKind) {
            this.assetKind = assetKind;
            return this;
        }

        public Builder assetType(String assetType) {
            this.assetType = assetType;
            return this;
        }

        public GetAllShellDescriptorsFilter build() {
            return new GetAllShellDescriptorsFilter(this);
        }
    }
}
