package org.eclipse.slm.common.openapi.test.shared;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SharedController {
    @GetMapping("/shared")
    public String shared() { return "shared"; }
}
