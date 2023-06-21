package de.fhg.ipa.ced.tests.stack;

public class TestServiceVendor {
    String id;
    String name;

    public TestServiceVendor() {
    }

    public TestServiceVendor(String name) {
        this.name = name;
    }

    public TestServiceVendor(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TestServiceVendor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
