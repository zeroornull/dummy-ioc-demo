package org.springframework.test.beans.beandefinition.support;

import org.springframework.context.annotation.Component;

@Component("person")
public class ComponentPerson {

    private String name;

    private String age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
