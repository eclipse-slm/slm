package org.eclipse.slm.common.openapi.test.fixtures;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FixtureController {
    public FixtureController(Dependency dependency) { }

    @GetMapping("/fixture")
    public String fixture() { return "fixture"; }

    public interface Dependency { }
}
