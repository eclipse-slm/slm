package org.eclipse.slm.common.consul.client.requests.catalog

import org.eclipse.slm.common.consul.client.requests.ConsulRequest
import org.eclipse.slm.common.consul.client.requests.parameters.*
import java.util.*

class CatalogServiceRequest private constructor(
    val datacenter: String?,
    tags: Array<String>?,
    near: String?,
    nodeMeta: Map<String, String>?,
    queryParams: QueryParams?,
    token: String?
) : ConsulRequest {
    val tags: Array<String>?
    val near: String?
    val nodeMeta: Map<String, String>?
    private val queryParams: QueryParams?
    val token: String?
    val tag: String?
        get() = if (tags != null && tags.size > 0) tags[0] else null

    fun getQueryParams(): QueryParams? {
        return queryParams
    }

    class Builder {
        private var datacenter: String? = null
        private var tags: Array<String>? = null
        private var near: String? = null
        private var nodeMeta: Map<String, String>? = null
        private var queryParams: QueryParams? = null
        private var token: String? = null
        fun setDatacenter(datacenter: String?): Builder {
            this.datacenter = datacenter
            return this
        }

        fun setTag(tag: String): Builder {
            tags = arrayOf(tag)
            return this
        }

        fun setTags(tags: Array<String>): Builder {
            this.tags = tags
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

        fun setToken(token: String?): Builder {
            this.token = token
            return this
        }

        fun build(): CatalogServiceRequest {
            return CatalogServiceRequest(datacenter, tags, near, nodeMeta, queryParams, token)
        }
    }

    override fun asUrlParameters(): List<UrlParameters> {
        val params: MutableList<UrlParameters> = ArrayList<UrlParameters>()
        if (datacenter != null) {
            params.add(
                SingleUrlParameters(
                    "dc",
                    datacenter
                )
            )
        }
        if (tags != null) {
            params.add(TagsParameters(tags))
        }
        if (near != null) {
            params.add(
                SingleUrlParameters(
                    "near",
                    near
                )
            )
        }
        if (nodeMeta != null) {
            params.add(NodeMetaParameters(nodeMeta))
        }
        if (queryParams != null) {
            params.add(queryParams)
        }
        if (token != null) {
            params.add(
                SingleUrlParameters(
                    "token",
                    token
                )
            )
        }
        return params
    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    init {
        this.tags = tags
        this.near = near
        this.nodeMeta = nodeMeta
        this.queryParams = queryParams
        this.token = token
    }
}
