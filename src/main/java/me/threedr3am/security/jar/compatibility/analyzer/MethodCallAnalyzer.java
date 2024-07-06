package me.threedr3am.security.jar.compatibility.analyzer;

import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.reader.ClazzReader;
import me.threedr3am.security.jar.compatibility.reader.JarReaderSpace;
import me.threedr3am.security.jar.compatibility.callgraph.MethodCall;
import me.threedr3am.security.jar.compatibility.cha.ClassInfo;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;
import me.threedr3am.security.jar.compatibility.config.DetectionOptions;
import me.threedr3am.security.jar.compatibility.result.CheckType;
import me.threedr3am.security.jar.compatibility.result.Issue;
import org.objectweb.asm.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;


@Slf4j
public class MethodCallAnalyzer implements Analyzer {

    private HashMap<String, ClassInfo> classes = new HashMap<>();
    private Map<String, MethodCall> methodCalls = new HashMap<>();
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
        Set<MethodCall> noExistCalls = executeResult();
        if (noExistCalls.isEmpty()) {
            return Collections.emptyList();
        }
        return noExistCalls.stream().map(this::transfer).collect(Collectors.toList());
    }

    private Issue transfer(MethodCall methodCall) {
        return new Issue("被调用方法不存在: %s.%s%s".formatted(methodCall.getOwner(), methodCall.getName(), methodCall.getDesc())
                , "调用点有：\n%s".formatted(
                methodCall.getCallers().stream()
                        .map(methodInfo -> "- %s.%s%s <-> %s".formatted(methodInfo.getDeclaringClass(), methodInfo.getName(), methodInfo.getDescriptor(), methodInfo.getJar()))
                        .collect(Collectors.joining("\n"))
        )
        );
    }

    @Override
    public CheckType type() {
        return CheckType.Call;
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
                Set<String> interfacesSet = null;
                if (interfaces != null) {
                    interfacesSet = new HashSet<>();
                    for (String anInterface : interfaces) {
                        interfacesSet.add(Type.getObjectType(anInterface).getClassName());
                    }
                }
                this.classInfo = new ClassInfo(clazzReader.getJar(), access, className, signature, superClassName);
                classes.put(className, this.classInfo);
                super.visit(version, access, name, signature, superName, interfaces);
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
                    public void visitMethodInsn(int opcode, String owner, String methodName, String methodDesc, boolean itf) {
                        Type ownerType = Type.getObjectType(owner);
                        Type[] argTypes = Type.getArgumentTypes(methodDesc);
                        Type returnType = Type.getReturnType(methodDesc);

                        String calleeName = owner + "." + methodName + methodDesc;
                        MethodCall call = methodCalls.computeIfAbsent(calleeName, k ->
                                new MethodCall(ownerType.getClassName(), methodName, methodDesc, returnType.getClassName(), argTypes));
                        call.getCallers().add(methodInfo);
                        super.visitMethodInsn(opcode, owner, methodName, methodDesc, itf);
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    private Set<MethodCall> executeResult() {
        return methodCalls.values().stream()
                .filter(methodCall -> options.getPkg() == null || isTargetCall(options.getPkg(), methodCall))
                .filter(methodCall -> !existCallee(methodCall.getOwner(), methodCall))
                .collect(Collectors.toSet());
    }

    private boolean isTargetCall(String pkg, MethodCall methodCall) {
        if (methodCall.getOwner().equals(pkg)) {
            return true;
        }
        for (MethodInfo caller : methodCall.getCallers()) {
            if (caller.getDeclaringClass().startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean existCallee(String owner, MethodCall methodCall) {
        ClassInfo classInfo = classes.get(owner);
        if (classInfo == null || classInfo.getMethods().isEmpty()) {
            return false;
        }
        if (classInfo.getMethods().containsKey(methodCall.getName() + methodCall.getDesc())) {
            return true;
        }
        if ((classInfo.getAccessFlags() & ACC_ABSTRACT) != 0 && classInfo.getInterfaces() != null) {
            for (String anInterface : classInfo.getInterfaces()) {
                if (existCallee(anInterface, methodCall)) {
                    return true;
                }
            }
        }
        return existCallee(classInfo.getSuperName(), methodCall);
    }
}
