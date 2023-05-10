package org.eclipse.slm.common.consul.client.requests.catalog

import org.eclipse.slm.common.consul.client.requests.ConsulRequest
import org.eclipse.slm.common.consul.client.requests.parameters.NodeMetaParameters
import org.eclipse.slm.common.consul.client.requests.parameters.QueryParams
import org.eclipse.slm.common.consul.client.requests.parameters.SingleUrlParameters
import org.eclipse.slm.common.consul.client.requests.parameters.UrlParameters
import java.util.*

class CatalogServicesRequest(
    val datacenter: String?,
    val nodeMeta: Map<String, String>?,
    queryParams: QueryParams?,
    token: String?
) : ConsulRequest {
    private val queryParams: QueryParams?
    val token: String?
    fun getQueryParams(): QueryParams? {
        return queryParams
    }

    class Builder {
        private var datacenter: String? = null
        private var nodeMeta: Map<String, String>? = null
        private var queryParams: QueryParams? = null
        private var token: String? = null
        fun setDatacenter(datacenter: String?): Builder {
            this.datacenter = datacenter
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

        fun setToken(token: String?): Builder {
            this.token = token
            return this
        }

        fun build(): CatalogServicesRequest {
            return CatalogServicesRequest(datacenter, nodeMeta, queryParams, token)
        }
    }

    override fun asUrlParameters(): List<UrlParameters> {
        val params: MutableList<UrlParameters> = ArrayList<UrlParameters>()
        if (datacenter != null) {
            params.add(SingleUrlParameters("dc", datacenter))
        }
        if (nodeMeta != null) {
            params.add(NodeMetaParameters(nodeMeta))
        }
        if (queryParams != null) {
            params.add(queryParams)
        }
        if (token != null) {
            params.add(SingleUrlParameters("token", token))
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
        this.token = token
    }
}
