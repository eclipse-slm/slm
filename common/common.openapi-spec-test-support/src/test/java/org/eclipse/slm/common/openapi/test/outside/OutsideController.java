package org.eclipse.slm.common.openapi.test.outside;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OutsideController {
    @GetMapping("/outside")
    public String outside() { return "outside"; }
}
