package me.threedr3am.security.jar.compatibility.cha;

import lombok.Data;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@Data
@ToString
public class ClassInfo {

    private String jar;

    private int accessFlags;
    private String className;
    private String signature;
    private String superName;
    private Set<String> interfaces;
    private Map<String, MethodInfo> methods;
    private Map<String, FieldInfo> fields;

    public ClassInfo(String jar, int accessFlags, String className, String signature, String superName, Set<String> interfaces, Map<String, MethodInfo> methods) {
        this.jar = jar;
        this.accessFlags = accessFlags;
        this.className = className;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
        this.methods = methods;
    }

    public ClassInfo(String jar, int accessFlags, String className, String signature, String superName, Set<String> interfaces, Map<String, MethodInfo> methods, Map<String, FieldInfo> fields) {
        this.jar = jar;
        this.accessFlags = accessFlags;
        this.className = className;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
        this.methods = methods;
        this.fields = fields;
    }
}
