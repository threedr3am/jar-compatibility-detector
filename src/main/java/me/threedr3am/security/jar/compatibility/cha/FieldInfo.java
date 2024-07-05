package me.threedr3am.security.jar.compatibility.cha;

import lombok.Data;

@Data
public class FieldInfo {

    private String jar;
    private String declaringClass;

    private int access;
    private String name;
    private String type;
    private String signature;
    private Object value;

    public FieldInfo(String jar, String declaringClass, int access, String name, String type, String signature, Object value) {
        this.jar = jar;
        this.declaringClass = declaringClass;
        this.access = access;
        this.name = name;
        this.type = type;
        this.signature = signature;
        this.value = value;
    }
}
