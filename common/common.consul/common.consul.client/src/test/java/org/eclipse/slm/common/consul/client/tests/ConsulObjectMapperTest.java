package org.eclipse.slm.common.consul.client.tests;

import com.orbitz.consul.model.acl.*;
import com.orbitz.consul.model.catalog.*;
import com.orbitz.consul.model.health.ImmutableHealthCheck;
import com.orbitz.consul.model.health.ImmutableNode;
import com.orbitz.consul.model.kv.ImmutableValue;
import org.eclipse.slm.common.consul.client.utils.ConsulObjectMapper;
import org.eclipse.slm.common.consul.model.acl.Policy;
import org.eclipse.slm.common.consul.model.catalog.CatalogNode;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.keyvalue.KeyValueReturnValue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsulObjectMapperTest {


    @Test
    public void mapHashMapToTaggedAddresses() {
        var taggedAddresses = new org.eclipse.slm.common.consul.model.catalog.TaggedAddresses();
        taggedAddresses.setLan("test");
        taggedAddresses.setWan( "test");

        var immutableTaggedAddresses = ConsulObjectMapper.map(taggedAddresses, TaggedAddresses.class);
        assertThat(immutableTaggedAddresses).isNotNull();
        assertThat(immutableTaggedAddresses.getLan()).isNotNull().isNotEmpty().get().isEqualTo(taggedAddresses.getLan());
        assertThat(immutableTaggedAddresses.getWan()).isNotNull().isNotEmpty().hasValue(taggedAddresses.getWan());
    }

    /**
     * Test Mapping multiple times
     */
    @Test
    public void mapCatalogNodeToImmutableCatalogNodeTests() {
        var catalogNode = new CatalogNode();
        var service = new CatalogNode.Service();
        var serviceTags = new ArrayList<String>();
        serviceTags.add("test");
        var serviceMeta = new HashMap<String, String>();
        serviceMeta.put("Test", "Test");

            service.setId(UUID.randomUUID());
            service.setService("Service");
            service.setAddress("Address");
            service.setPort(10);
            service.setTags(serviceTags);
            service.setServiceMeta(serviceMeta);

            catalogNode.setNode("Node");
            catalogNode.setId(UUID.randomUUID());
            var taggedAddresses = new org.eclipse.slm.common.consul.model.catalog.TaggedAddresses();
            taggedAddresses.setLan("test");
            taggedAddresses.setWan("test");
            catalogNode.setTaggedAddresses(taggedAddresses);
            catalogNode.setService(service);

            var immutableCatalogNode = ConsulObjectMapper.map(catalogNode, CatalogRegistration.class);
            var immutableService = immutableCatalogNode.getService();
            assertThat(immutableCatalogNode.getId()).isNotEmpty().get().isEqualTo(catalogNode.getId().toString());
            assertThat(immutableCatalogNode.getTaggedAddresses()).isNotEmpty();
            var lan = immutableCatalogNode.getTaggedAddresses().get().getLan();
            assertThat(lan).isNotEmpty().get().isEqualTo(taggedAddresses.getLan());
            assertThat(immutableCatalogNode.getTaggedAddresses().get().getWan()).isNotEmpty().hasValue(taggedAddresses.getWan());

        assertThat(immutableService).isNotNull().isNotEmpty();
        assertThat(immutableService.get().getId()).isEqualTo(service.getId().toString());
        assertThat(immutableService.get().getService()).isEqualTo(service.getService());
        assertThat(immutableService.get().getAddress()).isEqualTo(service.getAddress());
        assertThat(immutableService.get().getPort()).isPresent().hasValue(service.getPort());
        assertThat(immutableService.get().getTags()).isEqualTo(service.getTags());
        assertThat(immutableService.get().getMeta()).isEqualTo(service.getServiceMeta());
    }

    @Test
    public void mapImmutableNodeToNode(){
        var meta = new HashMap<String, String>();
        meta.put("Test1", "test");
        meta.put("Test2", "test");
        meta.put("Test3", "test");
        var immutableNode = ImmutableNode.builder().setId(UUID.randomUUID()).setNode("Node").setAddress("Address")
                .setDatacenter("Datacenter").setNodeMeta(meta);
        var node = ConsulObjectMapper.map(immutableNode.build(), Node.class);

        assertThat(node).isNotNull();
        assertThat(node.getMeta()).isEqualTo(meta);

    }

    @Test
    public void mapConsulPolicyToPolicyTests() {
        var datacenters = new ArrayList<String>();
        datacenters.add("Test");
        var consulPolicy = ImmutablePolicy.builder().setId("Id").setName("Name").setDescription("Description").setRules("Rules").setDatacenters(datacenters).build();
        var policy = ConsulObjectMapper.map(consulPolicy, Policy.class);

        assertThat(policy).isNotNull();
        assertThat(policy.getId()).isNotEmpty().isSameAs(consulPolicy.getId().get());
        assertThat(policy.getName()).isNotEmpty().isSameAs(consulPolicy.getName());
        assertThat(policy.getDescription()).isNotEmpty().isSameAs(consulPolicy.getDescription().get());
        assertThat(policy.getRules()).isNotEmpty().isSameAs(consulPolicy.getRules().get());
        assertThat(policy.getDatacenters()).isNotEmpty().hasSameElementsAs(datacenters);
    }

    @Test
    public void mapPolicyToConsulPolicyTests() {
        var datacenters = new ArrayList<String>();
        datacenters.add("Test");
        var policy = new Policy("Name", "Description", "Rules", datacenters);
        var consulPolicy = ConsulObjectMapper.map(policy, com.orbitz.consul.model.acl.Policy.class);

        assertThat(consulPolicy).isNotNull();
        assertThat(consulPolicy.getId()).isEmpty();
        assertThat(consulPolicy.getName()).isSameAs(policy.getName());
        assertThat(consulPolicy.getDescription()).hasValue(policy.getDescription());
        assertThat(consulPolicy.getRules()).hasValue(policy.getRules());
        assertThat(consulPolicy.getDatacenters()).hasValue(policy.getDatacenters());
    }

    @Test
    public void mapConsulRoleToRole() {
        var consulPolicy = ImmutableRolePolicyLink.builder().setId("Id").setName("Name").build();
        var consulNodeIdentity = ImmutableRoleNodeIdentity.builder().setName("Name").setDatacenter("Datacenter").build();
        var datacenters = List.of("Datacenters");
        var consulServiceIdentity = ImmutableRoleServiceIdentity.builder().setName("Name").setDatacenters(datacenters).build();
        var consulRole = ImmutableRole.builder().setId("Id").setName("Name").setDescription("Description")
                .setPolicies(List.of(consulPolicy)).setNodeIdentities(List.of(consulNodeIdentity))
                .setServiceIdentities(List.of(consulServiceIdentity)).build();

        var role = ConsulObjectMapper.map(consulRole, Role.class);

        assertThat(role).isNotNull();
        assertThat(role.getId()).hasValue(consulRole.getId().get());
        assertThat(role.getName()).isSameAs(consulRole.getName());
        assertThat(role.getDescription()).hasValue(consulRole.getDescription().get());

        assertThat(role.getPolicies()).isNotEmpty();
        var policy = role.getPolicies().get(0);
        assertThat(policy.getId()).hasValue(consulPolicy.getId().get());
        assertThat(policy.getName()).hasValue(consulPolicy.getName().get());

        assertThat(role.getNodeIdentities()).isNotEmpty();
        var nodeIdentities = role.getNodeIdentities().get(0);
        assertThat(nodeIdentities.getName()).isSameAs(consulNodeIdentity.getName());
        assertThat(nodeIdentities.getDatacenter()).isSameAs(consulNodeIdentity.getDatacenter());

        assertThat(role.getServiceIdentities()).isNotEmpty();
        var serviceIdentities = role.getServiceIdentities().get(0);
        assertThat(serviceIdentities.getName()).isSameAs(consulServiceIdentity.getName());
        assertThat(serviceIdentities.getDatacenters()).isNotEmpty().hasSameElementsAs(datacenters);

    }

    @Test
    public void mapConsulBindingRuleToBindingRule() {
        var consulBindingRule = ImmutableBindingRule.builder().setId("Id").setBindName("BindingName").setDescription("Description")
                .setSelector("Selector").setBindType("BindType").setAuthMethod("AuthMethod").build();
        var bindingRule = ConsulObjectMapper.map(consulBindingRule, org.eclipse.slm.common.consul.model.acl.BindingRule.class);

        assertThat(bindingRule).isNotNull();
        assertThat(bindingRule.getId()).isSameAs(consulBindingRule.getId().get());
        assertThat(bindingRule.getDescription()).isSameAs(consulBindingRule.getDescription().get());
        assertThat(bindingRule.getBindName()).isSameAs(consulBindingRule.getBindName());
        assertThat(bindingRule.getBindType()).isSameAs(consulBindingRule.getBindType());
        assertThat(bindingRule.getSelector()).isSameAs(consulBindingRule.getSelector().get());
        assertThat(bindingRule.getAuthMethod()).isSameAs(consulBindingRule.getAuthMethod());

    }

    @Test
    public void mapBindingRuleToConsulBindingRule() {
        var bindingRule = new org.eclipse.slm.common.consul.model.acl.BindingRule("Description", "Auth", "Selector", "Type", "BindName");
        var consulBindingRule = ConsulObjectMapper.map(bindingRule, BindingRule.class);

        assertThat(consulBindingRule).isNotNull();
        assertThat(consulBindingRule.getId()).isEmpty();
        assertThat(consulBindingRule.getDescription()).isNotEmpty().hasValue(bindingRule.getDescription());
        assertThat(consulBindingRule.getBindName()).isSameAs(bindingRule.getBindName());
        assertThat(consulBindingRule.getBindType()).isSameAs(bindingRule.getBindType());
        assertThat(consulBindingRule.getSelector()).isNotEmpty().hasValue(bindingRule.getSelector());
        assertThat(consulBindingRule.getAuthMethod()).isSameAs(bindingRule.getAuthMethod());

    }

    @Test
    public void mapConsulNodeChecksToCatalogNodeCheck(){
        var consulHealthCheck = ImmutableHealthCheck.builder().setCheckId("CheckId").setName("Name").setNode("Node")
                .setNotes("Notes").setOutput("Output").setServiceId("ServiceId").setServiceName("ServiceName")
                .setStatus("Status").build();
        var healthCheck = ConsulObjectMapper.map(consulHealthCheck, CatalogNode.Check.class);

        assertThat(healthCheck).isNotNull();
        assertThat(healthCheck.getCheckId()).isSameAs(consulHealthCheck.getCheckId());
        assertThat(healthCheck.getName()).isSameAs(consulHealthCheck.getName());
        assertThat(healthCheck.getStatus()).isSameAs(consulHealthCheck.getStatus());

    }

    @Test
    public void mapConsulKeyValueToKeyValueReturnValue(){
        var consulKeyValue = ImmutableValue.builder().setKey("Key").setValue("Value").setFlags(38).setSession("Session")
                .setCreateIndex(123).setModifyIndex(345).setLockIndex(8).build();
        var keyValue = ConsulObjectMapper.map(consulKeyValue, KeyValueReturnValue.class);

        assertThat(keyValue).isNotNull();
        assertThat(keyValue.getKey()).isSameAs(consulKeyValue.getKey());
        assertThat(keyValue.getValue()).isSameAs(consulKeyValue.getValue().get());
        assertThat(keyValue.getFlags()).isEqualTo((int)consulKeyValue.getFlags());
        assertThat(keyValue.getSession()).isSameAs(consulKeyValue.getSession().get());
        assertThat(keyValue.getCreateIndex()).isEqualTo((int)consulKeyValue.getCreateIndex());
        assertThat(keyValue.getModifyIndex()).isEqualTo((int)consulKeyValue.getModifyIndex());
        assertThat(keyValue.getLockIndex()).isEqualTo((int)consulKeyValue.getLockIndex());

    }

    @Test
    public void mapConsulCatalogServiceToCatalogServiceTests(){
        var map = new HashMap<String, String>();
        map.put("Test", "Test");
        var consulCatalogService = ImmutableCatalogService.builder().setServiceId("Id").setServiceName("Name")
                .setServicePort(12).setServiceTags(List.of("Tags")).setServiceAddress("Address").setServiceEnableTagOverride(true)
                .setDatacenter("Datacenter").setID(UUID.randomUUID()).setNode("Node")
                .setTaggedAddresses(ImmutableTaggedAddresses.builder().setWan("").setLan("Lan").build())
                .setNodeMeta(map).setAddress("Address")
                .build();

        var catalogService = ConsulObjectMapper.map(consulCatalogService, CatalogService.class);

        assertThat(catalogService).isNotNull();
        assertThat(catalogService.getServiceId()).isSameAs(consulCatalogService.getServiceId());
        assertThat(catalogService.getServiceName()).isSameAs(consulCatalogService.getServiceName());
        assertThat(catalogService.getServicePort()).isSameAs(consulCatalogService.getServicePort());
        assertThat(catalogService.getServiceTags()).isSameAs(consulCatalogService.getServiceTags());
        assertThat(catalogService.getServiceAddress()).isSameAs(consulCatalogService.getServiceAddress());
        assertThat(catalogService.getAddress()).isSameAs(consulCatalogService.getAddress());
        assertThat(catalogService.getServiceEnableTagOverride()).isSameAs(consulCatalogService.getServiceEnableTagOverride().get());
        assertThat(catalogService.getDatacenter()).isSameAs(consulCatalogService.getDatacenter().get());
        assertThat(catalogService.getId()).isSameAs(consulCatalogService.getID().get());
        assertThat(catalogService.getNode()).isSameAs(consulCatalogService.getNode());
        assertThat(catalogService.getTaggedAddresses().getLan()).isSameAs(consulCatalogService.getTaggedAddresses().get().getLan().get());
        assertThat(catalogService.getNodeMeta()).isEqualTo(consulCatalogService.getNodeMeta());


    }

    @Test
    public void mapConsulNodeServiceToNodeService(){
        var map = new HashMap<String, String>();
        map.put("Test", "Test");
        var consulNodeService = ImmutableNodeService.builder().setId("Id").setService("Service").setAddress("Address")
                .setPort(12).setNamespace("Namespace").setMeta(map).setTags(List.of("Test"))
                .setTaggedAddresses(ImmutableTaggedAddresses.builder().setWan("").setLan("Test").build())
                .build();
        var nodeService = ConsulObjectMapper.map(consulNodeService, org.eclipse.slm.common.consul.model.catalog.NodeService.class);

        assertThat(nodeService).isNotNull();
        assertThat(nodeService.getID()).isEqualTo(consulNodeService.getId());
        assertThat(nodeService.getService()).isEqualTo(consulNodeService.getService());
        assertThat(nodeService.getAddress()).isEqualTo(consulNodeService.getAddress().get());
        assertThat(nodeService.getPort()).isEqualTo(consulNodeService.getPort().get());
        assertThat(nodeService.getMeta()).isEqualTo(consulNodeService.getMeta());
        assertThat(nodeService.getTags()).isEqualTo(consulNodeService.getTags());
    }


}
