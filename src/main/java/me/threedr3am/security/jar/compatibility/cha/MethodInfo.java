package me.threedr3am.security.jar.compatibility.cha;

import lombok.Data;


@Data
public class MethodInfo {
    private String declaringClass;

    private int access;
    private String name;
    private String descriptor;
    private String signature;
    private String[] exceptions;

    public MethodInfo(String declaringClass, int access, String name, String descriptor, String signature, String[] exceptions) {
        this.declaringClass = declaringClass;
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.exceptions = exceptions;
    }

    @Override
    public String toString() {
        return String.format("%s.%s%s",
                declaringClass,
                name,
                descriptor);
    }
}
