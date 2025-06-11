package org.eclipse.slm.common.aas.repositories;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultQualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubmodelUtils {

    public final static Qualifier QUALIFIER_ZERO_TO_ONE = new DefaultQualifier.Builder()
            .kind(QualifierKind.CONCEPT_QUALIFIER)
            .type("Multiplicity")
            .valueType(DataTypeDefXsd.STRING)
            .value("ZeroToOne")
        .build();

    public final static Qualifier QUALIFIER_ONE = new DefaultQualifier.Builder()
            .kind(QualifierKind.CONCEPT_QUALIFIER)
            .type("Multiplicity")
            .valueType(DataTypeDefXsd.STRING)
            .value("One")
            .build();

    public final static String CATEGORY_CONSTANT = "CONSTANT";
    public final static String CATEGORY_PARAMETER = "PARAMETER";

    public static Reference generateSemanticId(String value) {
        return new DefaultReference.Builder()
                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.GLOBAL_REFERENCE)
                        .value(value)
                        .build())
                .build();
    }

    public static LangStringTextType generateLangString(String language, String text) {
        return new DefaultLangStringTextType.Builder().language(language).text(text).build();
    }

    public static List<LangStringTextType> generateEnglishDescription(String text) {
        return new ArrayList<>(Collections.singletonList(SubmodelUtils.generateLangString("en", text)));
    }

}
