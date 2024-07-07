package me.threedr3am.security.jar.compatibility.analyzer;

import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.World;
import me.threedr3am.security.jar.compatibility.callgraph.AnnotationReference;
import me.threedr3am.security.jar.compatibility.cha.ClassInfo;
import me.threedr3am.security.jar.compatibility.cha.FieldInfo;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;
import me.threedr3am.security.jar.compatibility.cha.ParameterInfo;
import me.threedr3am.security.jar.compatibility.result.CheckType;
import me.threedr3am.security.jar.compatibility.result.Issue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AnnotationAnalyzer implements Analyzer {

    private World world;

    @Override
    public void init(World world) {
        this.world = world;
    }

    @Override
    public List<Issue> analyze() {
        Set<AnnotationReference> noExistReferences = world.getAnnotationReferences().values().stream()
                .filter(annotationReference -> world.getOptions().getPkg() == null || isTargetAnnotationReference(world.getOptions().getPkg(), annotationReference))
                .filter(annotationReference -> world.getOptions().getJar() == null || isTargetJarAnnotationReference(world.getOptions().getJar(), annotationReference))
                .filter(annotationReference -> !world.getClasses().containsKey(annotationReference.getAnnotationName()))
                .collect(Collectors.toSet());
        if (noExistReferences.isEmpty()) {
            return Collections.emptyList();
        }
        return noExistReferences.stream().map(this::transfer).collect(Collectors.toList());
    }

    private Issue transfer(AnnotationReference annotationReference) {
        String title = "被引用注解不存在: @%s".formatted(annotationReference.getAnnotationName());
        StringBuilder descriptionSB = new StringBuilder("引用点有：\n");
        if (!annotationReference.getReferencedClasses().isEmpty()) {
            descriptionSB.append(annotationReference.getReferencedClasses().stream()
                    .map(classInfo -> "- class: %s <-> %s".formatted(classInfo.getClassName(), classInfo.getJar()))
                    .collect(Collectors.joining("\n")));
        }
        if (!annotationReference.getReferencedFields().isEmpty()) {
            descriptionSB.append(annotationReference.getReferencedFields().stream()
                    .map(fieldInfo -> "- field: %s-> <-> %s".formatted(fieldInfo.getDeclaringClass(), fieldInfo.getName(), fieldInfo.getJar()))
                    .collect(Collectors.joining("\n")));
        }
        if (!annotationReference.getReferencedMethods().isEmpty()) {
            descriptionSB.append(annotationReference.getReferencedMethods().stream()
                    .map(methodInfo -> "- method: %s.%s%s <-> %s".formatted(methodInfo.getDeclaringClass(), methodInfo.getName(), methodInfo.getDescriptor(), methodInfo.getJar()))
                    .collect(Collectors.joining("\n")));
        }
        if (!annotationReference.getReferencedParameters().isEmpty()) {
            descriptionSB.append(annotationReference.getReferencedParameters().stream()
                    .map(parameterInfo -> "- parameter: %s.%s%s->%d <-> %s".formatted(parameterInfo.getDeclaringClass(), parameterInfo.getMethodName(), parameterInfo.getMethodDesc(), parameterInfo.getIndex(), parameterInfo.getJar()))
                    .collect(Collectors.joining("\n")));
        }
        return new Issue(title, descriptionSB.toString(), type());
    }

    @Override
    public CheckType type() {
        return CheckType.AnnotationReference;
    }


    private boolean isTargetAnnotationReference(String pkg, AnnotationReference annotationReference) {
        if (annotationReference.getAnnotationName().startsWith(pkg)) {
            return true;
        }
        for (ClassInfo reference : annotationReference.getReferencedClasses()) {
            if (reference.getClassName().startsWith(pkg)) {
                return true;
            }
        }
        for (FieldInfo reference : annotationReference.getReferencedFields()) {
            if (reference.getDeclaringClass().startsWith(pkg)) {
                return true;
            }
        }
        for (MethodInfo reference : annotationReference.getReferencedMethods()) {
            if (reference.getDeclaringClass().startsWith(pkg)) {
                return true;
            }
        }
        for (ParameterInfo reference : annotationReference.getReferencedParameters()) {
            if (reference.getDeclaringClass().startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTargetJarAnnotationReference(String optionJar, AnnotationReference annotationReference) {
        ClassInfo ownClass = world.getClass(annotationReference.getAnnotationName());
        if (ownClass != null && matchJar(ownClass.getJar(), optionJar)) {
            return true;
        }
        for (ClassInfo reference : annotationReference.getReferencedClasses()) {
            if (matchJar(reference.getJar(), optionJar)) {
                return true;
            }
        }
        for (FieldInfo reference : annotationReference.getReferencedFields()) {
            if (matchJar(reference.getJar(), optionJar)) {
                return true;
            }
        }
        for (MethodInfo reference : annotationReference.getReferencedMethods()) {
            if (matchJar(reference.getJar(), optionJar)) {
                return true;
            }
        }
        for (ParameterInfo reference : annotationReference.getReferencedParameters()) {
            if (matchJar(reference.getJar(), optionJar)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchJar(String jar, String optionJar) {
        if (optionJar.startsWith("/")) {
            if (optionJar.equals(jar)) {
                return true;
            }
        } else {
            if (jar.endsWith(optionJar)) {
                return true;
            }
        }
        return false;
    }
}
