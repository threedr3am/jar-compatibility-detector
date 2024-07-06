package me.threedr3am.security.jar.compatibility.analyzer;

import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.callgraph.FieldLoadStore;
import me.threedr3am.security.jar.compatibility.cha.ClassInfo;
import me.threedr3am.security.jar.compatibility.cha.FieldInfo;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;
import me.threedr3am.security.jar.compatibility.config.DetectionOptions;
import me.threedr3am.security.jar.compatibility.reader.ClazzReader;
import me.threedr3am.security.jar.compatibility.reader.JarReaderSpace;
import me.threedr3am.security.jar.compatibility.result.CheckType;
import me.threedr3am.security.jar.compatibility.result.Issue;
import org.objectweb.asm.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FieldLoadStoreAnalyzer implements Analyzer {

    private HashMap<String, ClassInfo> classes = new HashMap<>();
    private Map<String, FieldLoadStore> fieldLoads = new HashMap<>();
    private JarReaderSpace jarReaderSpace;
    private DetectionOptions options;

    @Override
    public void init(JarReaderSpace jarReaderSpace, DetectionOptions options) {
        this.jarReaderSpace = jarReaderSpace;
        this.options = options;
    }

    @Override
    public List<Issue> analyze() {
        jarReaderSpace.getJreReaders().forEach(clazzReader -> parse(clazzReader, true));
        jarReaderSpace.getReaders().forEach(clazzReader -> parse(clazzReader, false));
        Set<FieldLoadStore> noExistCalls = executeResult();
        if (noExistCalls.isEmpty()) {
            return Collections.emptyList();
        }
        return noExistCalls.stream().map(this::transfer).collect(Collectors.toList());
    }

    private Issue transfer(FieldLoadStore fieldLoadStore) {
        return new Issue("被引用字段不存在: %s->%s <-> %s".formatted(fieldLoadStore.getOwner(), fieldLoadStore.getField(), fieldLoadStore.getType())
                , "引用点有：\n%s".formatted(
                fieldLoadStore.getLoadStores().stream()
                        .map(loader -> "- %s.%s%s <-> %s".formatted(loader.getDeclaringClass(), loader.getName(), loader.getDescriptor(), loader.getJar()))
                        .collect(Collectors.joining("\n"))
        )
        );
    }

    @Override
    public CheckType type() {
        return CheckType.LoadStore;
    }

    private void parse(ClazzReader clazzReader, boolean isJre) {
        clazzReader.getClassReader().accept(new ClassVisitor(Opcodes.ASM9) {

            private ClassInfo classInfo;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                String className = Type.getObjectType(name).getClassName();
                String superClassName = null;
                if (superName != null) {
                    superClassName = Type.getObjectType(superName).getClassName();
                }
                this.classInfo = new ClassInfo(clazzReader.getJar(), access, className, signature, superClassName);
                classes.put(className, this.classInfo);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                Type fieldType = Type.getObjectType(descriptor);
                FieldInfo fieldInfo = new FieldInfo(classInfo.getJar(), classInfo.getClassName(), access, name, fieldType.getClassName(), signature, value);
                classInfo.getFields().put(fieldInfo.getName(), fieldInfo);
                return super.visitField(access, name, fieldType.getClassName(), signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodInfo methodInfo = new MethodInfo(classInfo.getJar(), classInfo.getClassName(), access, name, descriptor, signature, exceptions);
                classInfo.getMethods().put(name + descriptor, methodInfo);
                if (isJre) {
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        Type ownerType = Type.getObjectType(owner);
                        Type fieldType = Type.getObjectType(descriptor);

                        String fieldName = ownerType.getClassName() + "." + name;
                        FieldLoadStore fieldLoadStore = fieldLoads.computeIfAbsent(fieldName, k ->
                                new FieldLoadStore(ownerType.getClassName(), name, fieldType.getClassName()));
                        fieldLoadStore.getLoadStores().add(methodInfo);
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    private Set<FieldLoadStore> executeResult() {
        return fieldLoads.values().stream()
                .filter(fieldLoadStore -> options.getPkg() == null || isTargetLoad(options.getPkg(), fieldLoadStore))
                .filter(fieldLoadStore -> !existField(fieldLoadStore.getOwner(), fieldLoadStore))
                .collect(Collectors.toSet());
    }

    private boolean isTargetLoad(String pkg, FieldLoadStore fieldLoadStore) {
        if (fieldLoadStore.getOwner().equals(pkg)) {
            return true;
        }
        for (MethodInfo loader : fieldLoadStore.getLoadStores()) {
            if (loader.getDeclaringClass().startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean existField(String owner, FieldLoadStore fieldLoadStore) {
        ClassInfo classInfo = classes.get(owner);
        if (classInfo == null || classInfo.getMethods().isEmpty()) {
            return false;
        }
        if (classInfo.getFields().containsKey(fieldLoadStore.getField())) {
            return true;
        }
        return existField(classInfo.getSuperName(), fieldLoadStore);
    }
}
