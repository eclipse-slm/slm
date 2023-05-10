package org.eclipse.slm.common.consul.client.utils;

import com.google.common.collect.ImmutableList;
import com.orbitz.consul.model.acl.*;
import com.orbitz.consul.model.catalog.*;
import com.orbitz.consul.model.health.ImmutableService;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.model.health.Service;
import org.eclipse.slm.common.consul.model.acl.BindingRule;
import org.eclipse.slm.common.consul.model.acl.Policy;
import org.eclipse.slm.common.consul.model.acl.PolicyLink;
import org.eclipse.slm.common.consul.model.acl.Role;
import org.eclipse.slm.common.consul.model.catalog.CatalogNode;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MappingContext;

import java.util.*;
import java.util.stream.Collectors;

public class ConsulObjectMapper {

    public static ModelMapper modelMapper;

    /**
     * Model mapper property setting are specified in the following block.
     * Default property matching strategy is set to Strict see {@link MatchingStrategies}
     * Custom mappings are added using {@link ModelMapper#addMappings(PropertyMap)}
     */
    static {
        modelMapper = new ModelMapper();

        var optionalStringToString = new Converter<Optional<String>, String>() {
            @Override
            public String convert(MappingContext<Optional<String>, String> context) {
                if (context.getSource().isPresent()) {
                    var source = context.getSource();
                    return source.orElse("");
                }
                return null;
            }
        };
        var optionalStringToUUID = new Converter<Optional<String>, UUID>() {
            @Override
            public UUID convert(MappingContext<Optional<String>, UUID> context) {
                if (context.getSource().isPresent()) {
                    var source = context.getSource();
                    return UUID.fromString(source.get());
                }
                return null;
            }
        };
        var stringToUUID = new Converter<String, UUID>() {
            @Override
            public UUID convert(MappingContext<String, UUID> context) {
                if (context.getSource() != null) {
                    var source = context.getSource();
                    return UUID.fromString(source);
                }
                return null;
            }
        };
        var stringToOptionalString = new Converter<String, Optional<String>>() {
            @Override
            public Optional<String> convert(MappingContext<String, Optional<String>> context) {

                if (context.getSource() != null) {
                    return Optional.of(context.getSource());
                }
                return Optional.empty();
            }
        };
        var optionalStringListToList = new Converter<Optional<List<String>>, List<String>>() {
            @Override
            public List<String> convert(MappingContext<Optional<List<String>>, List<String>> context) {
                if (context.getSource().isPresent()) {
                    return context.getSource().orElse(new ArrayList<>());
                }
                return new ArrayList<>();
            }
        };

        var optionalStringListToOptionalList = new Converter<Optional<List<String>>, Optional<List<String>>>() {
            @Override
            public Optional<List<String>> convert(MappingContext<Optional<List<String>>, Optional<List<String>>> mappingContext) {
                if(mappingContext.getSource().isPresent()){
                    return mappingContext.getSource();
                }
                return Optional.empty();
            }
        };

        var stringListToOptionalStringList = new Converter<List<String>, Optional<List<String>>>() {
            @Override
            public Optional<List<String>> convert(MappingContext<List<String>, Optional<List<String>>> mappingContext) {
                if(mappingContext.getSource() != null){
                    return Optional.of(mappingContext.getSource());
                }
                return Optional.empty();
            }
        };
        var stringListToList = new Converter<List<String>, List<String>>() {
            @Override
            public List<String> convert(MappingContext<List<String>, List<String>> context) {
                if (context.getSource() != null) {
                    return context.getSource();
                }
                return new ArrayList<>();
            }
        };
        var optionalUUIDToUUID = new Converter<Optional<UUID>, UUID>() {
            @Override
            public UUID convert(MappingContext<Optional<UUID>, UUID> context) {
                if (context.getSource().isPresent()) {
                    return context.getSource().orElse(null);
                }
                return null;
            }
        };
        var UUIDToString = new Converter<UUID, String>() {
            @Override
            public String convert(MappingContext<UUID, String> context) {
                if (context.getSource() != null) {
                    return context.getSource().toString();
                }
                return null;
            }
        };
        var UUIDToOptionalString = new Converter<UUID, Optional<String>>() {
            @Override
            public Optional<String> convert(MappingContext<UUID, Optional<String>> context) {
                if (context.getSource() != null) {
                    return Optional.of(context.getSource().toString());
                }
                return Optional.empty();
            }
        };
        var optionalBoolToBool = new Converter<Optional<Boolean>, Boolean>() {
            @Override
            public Boolean convert(MappingContext<Optional<Boolean>, Boolean> context) {
                if (context.getSource().isPresent()) {
                    return context.getSource().orElse(null);
                }
                return null;
            }
        };
        var optionalIntegerToOptionalInteger = new Converter<Optional<Integer>, OptionalInt>() {
            @Override
            public OptionalInt convert(MappingContext<Optional<Integer>, OptionalInt> context) {
                if (context.getSource().isPresent()) {
                    return OptionalInt.of(context.getSource().get());
                }
                return OptionalInt.empty();
            }
        };
        var optionalIntegerToInt = new Converter<Optional<Integer>, Integer>() {
            @Override
            public Integer convert(MappingContext<Optional<Integer>, Integer> context) {
                if (context.getSource().isPresent()) {
                    return context.getSource().orElse(0);
                }
                return 0;
            }
        };
        var optionalInteger2ToInt = new Converter<OptionalInt, Integer>() {
            @Override
            public Integer convert(MappingContext<OptionalInt, Integer> context) {
                if (context.getSource().isPresent()) {
                    return context.getSource().orElse(0);
                }
                return 0;
            }
        };
        var optionalLongToLong = new Converter<OptionalLong, Long>() {
            @Override
            public Long convert(MappingContext<OptionalLong, Long> context) {
                if (context.getSource().isPresent()) {
                    return context.getSource().orElse(0);
                }
                return 0L;
            }
        };
        var optionalMapperToMapper = new Converter<Optional<LinkedHashMap<String, String>>, LinkedHashMap<String, String>>() {
            @Override
            public LinkedHashMap<String, String> convert(MappingContext<Optional<LinkedHashMap<String, String>>, LinkedHashMap<String, String>> context) {
                if (context.getSource().isPresent()) {
                    return context.getSource().get();
                }
                return new LinkedHashMap<String, String>();
            }
        };
        var mapperToOptionalMapper = new Converter<LinkedHashMap<String, String>, Optional<LinkedHashMap<String, String>>>() {
            @Override
            public Optional<LinkedHashMap<String, String>> convert(MappingContext<LinkedHashMap<String, String>, Optional<LinkedHashMap<String, String>>> context) {
                if (context.getSource() != null) {
                    return Optional.of(context.getSource());
                }
                return Optional.empty();
            }
        };

        var catalogServiceToOptionalImmutableService = new Converter<CatalogNode.Service, Optional<Service>>() {
            @Override
            public Optional<Service> convert(MappingContext<CatalogNode.Service, Optional<Service>> mappingContext) {
                if (mappingContext.getSource() != null) {
                    var service = map(mappingContext.getSource(), Service.class);
                    return Optional.of(service);
                }
                return Optional.empty();
            }
        };

        var catalogServiceToImmutableService = new Converter<CatalogNode.Service, Service>() {
            @Override
            public Service convert(MappingContext<CatalogNode.Service, Service> mappingContext) {
                if (mappingContext.getSource() != null) {
                    var source = mappingContext.getSource();
                    var builder = map(mappingContext.getSource(), ImmutableService.builder());
                    if (source.getPort() != null)
                        builder.setPort(source.getPort());

                    return builder.build();
                }
                return null;
            }
        };

        var optionalTaggedAddressesToTaggedAddresses = new Converter<Optional<TaggedAddresses>, org.eclipse.slm.common.consul.model.catalog.TaggedAddresses>() {
            @Override
            public org.eclipse.slm.common.consul.model.catalog.TaggedAddresses convert(MappingContext<Optional<TaggedAddresses>, org.eclipse.slm.common.consul.model.catalog.TaggedAddresses> mappingContext) {
                if(mappingContext.getSource().isPresent()){
                    return map(mappingContext.getSource().get(), mappingContext.getDestinationType());
                }
                return null;
            }
        };

        var taggedAddressToConsulTaggedAddresses = new Converter<org.eclipse.slm.common.consul.model.catalog.TaggedAddresses, TaggedAddresses>(){
            @Override
            public TaggedAddresses convert(MappingContext<org.eclipse.slm.common.consul.model.catalog.TaggedAddresses, TaggedAddresses> mappingContext) {
                if(mappingContext.getSource() != null){
                    return map(mappingContext.getSource(), ImmutableTaggedAddresses.Builder.class).build();
                }
                return null;
            }
        };

        var taggedAddressesToOptionalTaggedAddresses = new Converter<org.eclipse.slm.common.consul.model.catalog.TaggedAddresses, Optional<TaggedAddresses>>() {

            @Override
            public Optional<TaggedAddresses> convert(MappingContext<org.eclipse.slm.common.consul.model.catalog.TaggedAddresses, Optional<TaggedAddresses>> mappingContext) {

                if(mappingContext.getSource() != null){
                    return Optional.of(map(mappingContext.getSource(), TaggedAddresses.class));
                }
                return Optional.empty();
            }
        };

        var optionalTaggedAddressesToOptionalTaggedAddresses = new Converter<Optional<org.eclipse.slm.common.consul.model.catalog.TaggedAddresses>, Optional<TaggedAddresses>>() {


            @Override
            public Optional<TaggedAddresses> convert(MappingContext<Optional<org.eclipse.slm.common.consul.model.catalog.TaggedAddresses>, Optional<TaggedAddresses>> mappingContext) {
                if(mappingContext.getSource().isPresent()){
                    return Optional.of(map(mappingContext.getSource().get(), TaggedAddresses.class));
                }
                return Optional.empty();
            }
        };

        var optionalTaggedAddressesToConsulTaggedAddresses = new Converter<Optional<org.eclipse.slm.common.consul.model.catalog.TaggedAddresses>, TaggedAddresses>() {


            @Override
            public TaggedAddresses convert(MappingContext<Optional<org.eclipse.slm.common.consul.model.catalog.TaggedAddresses>, TaggedAddresses> mappingContext) {
                if(mappingContext.getSource().isPresent()){
                    return map(mappingContext.getSource().get(), TaggedAddresses.class);
                }
                return null;
            }
        };

        modelMapper.addConverter(optionalTaggedAddressesToTaggedAddresses);
        modelMapper.addConverter(taggedAddressesToOptionalTaggedAddresses);
        modelMapper.addConverter(optionalTaggedAddressesToOptionalTaggedAddresses);
        modelMapper.addConverter(optionalTaggedAddressesToConsulTaggedAddresses);
        modelMapper.addConverter(taggedAddressToConsulTaggedAddresses);


        var optMapToMap = new Converter<Optional<Map<String, String>>, Map<String, String>>() {
            @Override
            public Map<String, String> convert(MappingContext<Optional<Map<String, String>>, Map<String, String>> mappingContext) {
                if(mappingContext.getSource().isPresent())
                    return mappingContext.getSource().get();
                return new HashMap<>();
            }
        };
        modelMapper.addConverter(optMapToMap);

        var immutableListToList = new Converter<ImmutableList<String>, List<String>>() {
            @Override
            public List<String> convert(MappingContext<ImmutableList<String>, List<String>> mappingContext) {
                if(mappingContext.getSource() != null){
                    return mappingContext.getSource().stream().toList();
                }
                return new ArrayList<String>();
            }
        };

        modelMapper.addConverter(immutableListToList);

        modelMapper.addConverter(optionalStringToString);
        modelMapper.addConverter(optionalStringToUUID);
        modelMapper.addConverter(stringToUUID);
        modelMapper.addConverter(stringToOptionalString);
        modelMapper.addConverter(optionalStringListToList);
        modelMapper.addConverter(stringListToList);
        modelMapper.addConverter(stringListToOptionalStringList);
        modelMapper.addConverter(optionalStringListToOptionalList);
        modelMapper.addConverter(optionalUUIDToUUID);
        modelMapper.addConverter(UUIDToOptionalString);
        modelMapper.addConverter(UUIDToString);
        modelMapper.addConverter(optionalBoolToBool);
        modelMapper.addConverter(optionalIntegerToOptionalInteger);
        modelMapper.addConverter(optionalIntegerToInt);
        modelMapper.addConverter(optionalInteger2ToInt);
        modelMapper.addConverter(optionalLongToLong);
        modelMapper.addConverter(optionalMapperToMapper);
        modelMapper.addConverter(mapperToOptionalMapper);


        modelMapper.addConverter(catalogServiceToOptionalImmutableService);
        modelMapper.addConverter(catalogServiceToImmutableService);


        modelMapper.typeMap(org.eclipse.slm.common.consul.model.catalog.TaggedAddresses.class, TaggedAddresses.class)
                .setProvider(p -> map(p.getSource(), ImmutableTaggedAddresses.Builder.class).build());
        modelMapper.typeMap(org.eclipse.slm.common.consul.model.catalog.TaggedAddresses.class, ImmutableTaggedAddresses.Builder.class)
                .setProvider(p -> ImmutableTaggedAddresses.builder());


        modelMapper.typeMap(CatalogNode.Service.class, Service.class)
                .setProvider(p -> map(p.getSource(), ImmutableService.Builder.class).build());
        modelMapper.typeMap(CatalogNode.Service.class, ImmutableService.Builder.class)
                .setProvider(p -> ImmutableService.builder());

        modelMapper.typeMap(CatalogNode.class, CatalogRegistration.class)
                .setProvider(p -> map(p.getSource(), ImmutableCatalogRegistration.Builder.class).build());
        modelMapper.typeMap(CatalogNode.class, ImmutableCatalogRegistration.Builder.class)
                .setProvider(p -> ImmutableCatalogRegistration.builder());

        modelMapper.typeMap(Policy.class, com.orbitz.consul.model.acl.Policy.class)
                        .setProvider(p -> map(p.getSource(), ImmutablePolicy.Builder.class).build());
        modelMapper.typeMap(Policy.class, ImmutablePolicy.Builder.class)
                        .setProvider(p -> ImmutablePolicy.builder());

        modelMapper.typeMap(BindingRule.class, com.orbitz.consul.model.acl.BindingRule.class)
                        .setProvider(p -> map(p.getSource(), ImmutableBindingRule.Builder.class).build());
        modelMapper.typeMap(Policy.class, ImmutableBindingRule.Builder.class)
                        .setProvider(p -> ImmutableBindingRule.builder());

        modelMapper.typeMap(ImmutableRoleResponse.class, Role.class)
                        .setPostConverter(mappingContext -> {
                            var dest = mappingContext.getDestination();
                            if(dest.getPolicies() != null && !dest.getPolicies().isEmpty()){
                                var policies = mapAll(dest.getPolicies(), PolicyLink.class);
                                dest.setPolicies(policies);
                            }
                           return mappingContext.getDestination();
                        });


        modelMapper.typeMap(Node.class, org.eclipse.slm.common.consul.model.catalog.Node.class)
                        .addMappings( mapperContext -> {
                            mapperContext.map(Node::getNodeMeta, org.eclipse.slm.common.consul.model.catalog.Node::setMeta);
                        });



        modelMapper.getConfiguration()
                .setPropertyCondition(Conditions.isNotNull())
                .setMatchingStrategy(MatchingStrategies.STANDARD)
                .setSkipNullEnabled(true)
                .setPreferNestedProperties(true);


    }

    /**
     * Hide from public usage.
     */
    private ConsulObjectMapper() {

    }

    /**
     * <p>Note: outClass object must have default constructor with no arguments</p>
     *
     * @param <D>      type of result object.
     * @param <T>      type of source object to map from.
     * @param entity   entity that needs to be mapped.
     * @param outClass class of result object.
     * @return new object of <code>outClass</code> type.
     */
    public static <D, T> D map(final T entity, Class<D> outClass) {
        return modelMapper.map(entity, outClass);
    }

    /**
     * <p>Note: outClass object must have default constructor with no arguments</p>
     *
     * @param entityList list of entities that needs to be mapped
     * @param outCLass   class of result list element
     * @param <D>        type of objects in result list
     * @param <T>        type of entity in <code>entityList</code>
     * @return list of mapped object with <code><D></code> type.
     */
    public static <D, T> List<D> mapAll(final Collection<T> entityList, Class<D> outCLass) {
        return entityList.stream()
                .map(entity -> map(entity, outCLass))
                .collect(Collectors.toList());
    }

    /**
     * Maps {@code source} to {@code destination}.
     *
     * @param source      object to map from
     * @param destination object to map to
     */
    public static <S, D> D map(final S source, D destination) {
        modelMapper.map(source, destination);
        return destination;
    }
}
