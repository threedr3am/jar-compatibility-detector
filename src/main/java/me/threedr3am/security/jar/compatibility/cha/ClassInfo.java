package me.threedr3am.security.jar.compatibility.cha;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.HashSet;
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
    private Set<String> interfaces = new HashSet<>();
    private Map<String, MethodInfo> methods = new HashMap<>();
    private Map<String, FieldInfo> fields = new HashMap<>();

    public ClassInfo(String jar, int accessFlags, String className, String signature, String superName) {
        this.jar = jar;
        this.accessFlags = accessFlags;
        this.className = className;
        this.signature = signature;
        this.superName = superName;
    }
}
