package org.eclipse.slm.common.consul.model.catalog

class NodeService
{
        var ID: String = ""
        var Service: String = ""
        var Tags: List<String> = ArrayList<String>()
        var Address: String = ""
        var Meta: Map<String, String> = HashMap<String, String>()
        var Port: Int = 0
        var Weights: Map<String, Int> =  HashMap<String, Int>()
        var EnableTagOverride: Boolean = false
        var Proxy: Map<String, Any> = HashMap<String, Any>()
        var Connect: Any? = null
        var CreateIndex: Int = 0
        var ModifyIndex: Int = 0

        constructor(){}
        constructor(
                ID: String,
                Service: String,
                Tags: List<String>,
                Address: String,
                Meta: Map<String, String>,
                Port: Int,
                Weights: Map<String, Int>,
                EnableTagOverride: Boolean,
                Proxy: Map<String, Any>,
                Connect: Any?,
                CreateIndex: Int,
                ModifyIndex: Int
        ) {
                this.ID = ID
                this.Service = Service
                this.Tags = Tags
                this.Address = Address
                this.Meta = Meta
                this.Port = Port
                this.Weights = Weights
                this.EnableTagOverride = EnableTagOverride
                this.Proxy = Proxy
                this.Connect = Connect
                this.CreateIndex = CreateIndex
                this.ModifyIndex = ModifyIndex
        }


        override fun toString(): String {
                return "NodeService [ID='${ID}', Service='${Service}']";
        }
}
