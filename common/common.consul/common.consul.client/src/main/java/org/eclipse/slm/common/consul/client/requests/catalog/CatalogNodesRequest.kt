package org.eclipse.slm.common.consul.client.requests.catalog

import org.eclipse.slm.common.consul.client.requests.ConsulRequest
import org.eclipse.slm.common.consul.client.requests.parameters.NodeMetaParameters
import org.eclipse.slm.common.consul.client.requests.parameters.QueryParams
import org.eclipse.slm.common.consul.client.requests.parameters.SingleUrlParameters
import org.eclipse.slm.common.consul.client.requests.parameters.UrlParameters
import java.util.*

class CatalogNodesRequest private constructor(
    val datacenter: String?,
    val near: String?,
    val nodeMeta: Map<String, String>?,
    queryParams: QueryParams?
) : ConsulRequest {
    private val queryParams: QueryParams?
    fun getQueryParams(): QueryParams? {
        return queryParams
    }

    class Builder {
        private var datacenter: String? = null
        private var near: String? = null
        private var nodeMeta: Map<String, String>? = null
        private var queryParams: QueryParams? = null
        fun setDatacenter(datacenter: String?): Builder {
            this.datacenter = datacenter
            return this
        }

        fun setNear(near: String?): Builder {
            this.near = near
            return this
        }

        fun setNodeMeta(nodeMeta: Map<String, String>?): Builder {
            if (nodeMeta == null) {
                this.nodeMeta = null
            } else {
                this.nodeMeta = Collections.unmodifiableMap(nodeMeta)
            }
            return this
        }

        fun setQueryParams(queryParams: QueryParams?): Builder {
            this.queryParams = queryParams
            return this
        }

        fun build(): CatalogNodesRequest {
            return CatalogNodesRequest(datacenter, near, nodeMeta, queryParams)
        }
    }

    override fun asUrlParameters(): List<UrlParameters> {
        val params: MutableList<UrlParameters> = ArrayList<UrlParameters>()
        if (datacenter != null) {
            params.add(SingleUrlParameters("dc", datacenter))
        }
        if (near != null) {
            params.add(SingleUrlParameters("near", near))
        }
        if (nodeMeta != null) {
            params.add(NodeMetaParameters(nodeMeta))
        }
        if (queryParams != null) {
            params.add(queryParams)
        }
        return params
    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    init {
        this.queryParams = queryParams
    }
}
