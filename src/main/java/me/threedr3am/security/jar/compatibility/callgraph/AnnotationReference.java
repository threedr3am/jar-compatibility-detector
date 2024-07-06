package me.threedr3am.security.jar.compatibility.callgraph;

import lombok.Data;
import me.threedr3am.security.jar.compatibility.cha.ClassInfo;
import me.threedr3am.security.jar.compatibility.cha.FieldInfo;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;
import me.threedr3am.security.jar.compatibility.cha.ParameterInfo;

import java.util.HashSet;
import java.util.Set;

@Data
public class AnnotationReference {

    private Set<ClassInfo> referencedClasses = new HashSet<>();
    private Set<MethodInfo> referencedMethods = new HashSet<>();
    private Set<FieldInfo> referencedFields = new HashSet<>();
    private Set<ParameterInfo> referencedParameters = new HashSet<>();

    private String annotationName;

    public AnnotationReference(String annotationName) {
        this.annotationName = annotationName;
    }
}
