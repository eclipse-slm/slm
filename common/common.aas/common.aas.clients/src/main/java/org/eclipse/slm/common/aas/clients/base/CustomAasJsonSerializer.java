package org.eclipse.slm.common.aas.clients.base;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;

public class CustomAasJsonSerializer extends JsonSerializer {

    public CustomAasJsonSerializer() {
        super();
        mapper.setDefaultPrettyPrinter(new MinimalPrettyPrinter());
    }

}
