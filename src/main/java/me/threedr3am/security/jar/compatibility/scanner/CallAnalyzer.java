package me.threedr3am.security.jar.compatibility.scanner;

import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.cha.ClassInfo;
import me.threedr3am.security.jar.compatibility.callgraph.MethodCall;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;
import me.threedr3am.security.jar.compatibility.config.DetectionOptions;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;


@Slf4j
public class CallAnalize implements Checker {

    private HashMap<String, ClassInfo> classes = new HashMap<>();
    private Map<String, MethodCall> methodCalls = new HashMap<>();
    private final DetectionOptions options;

    protected static final String JREs = "JREs";

    public CallAnalize(DetectionOptions options) {
        this.options = options;
    }

    @Override
    public void check() {
        scanJreClasses();
        Path target = Path.of(options.getTarget());
        if (Files.isDirectory(target)) {
            scanDirectory(target);
        } else {
            processJar(target);
        }
        analyze();
    }

    private void scanJreClasses() {
        File jreDir = new File(JREs);
        if (!jreDir.exists()) {
            throw new RuntimeException("JREs directory may be required for normal operation!");
        }
        String jrePath = String.format("%s/jre1.%d",
                JREs, options.getJavaVersion());
        // 遍历lib目录下的所有.jar文件
        File[] jarFiles = new File(jrePath).listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles != null) {
            try {
                for (File jarFile : jarFiles) {
                    JarFile jar = new JarFile(jarFile);
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            analyzeClass(jar, entry, true);
                        }
                    }
                    jar.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void scanDirectory(Path dir) {
        try {
            Files.walk(dir).filter(path -> path.toString().endsWith(".jar")).forEach(this::processJar);
        } catch (IOException e) {
            log.error("Error scanning directory", e);
        }
    }

    private void processJar(Path jarPath) {
        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            zipFile.stream().forEach(entry -> {
                if (entry.getName().endsWith(".class")) {
                    analyzeClass(zipFile, entry, false);
                } else if (entry.getName().endsWith(".jar")) {
                    try {
                        Path nestedJarPath = Files.createTempFile("nested-jar-", ".tmp");
                        Files.copy(zipFile.getInputStream(entry), nestedJarPath);
                        processJar(nestedJarPath);
                        Files.delete(nestedJarPath);
                    } catch (IOException e) {
                        log.error("Error processing nested JAR", e);
                    }
                }
            });
        } catch (IOException e) {
            log.error("Error processing JAR", e);
        }
    }

    private void analyzeClass(ZipFile zipFile, ZipEntry entry, boolean isJre) {
        try {
            ClassReader reader = new ClassReader(zipFile.getInputStream(entry));
            reader.accept(new ClassVisitor(Opcodes.ASM9) {

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
                    this.classInfo = new ClassInfo(zipFile.getName(), access, className, signature,
                            superClassName, interfacesSet, new HashMap<>());
                    classes.put(className, this.classInfo);
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    MethodInfo methodInfo = new MethodInfo(classInfo.getClassName(), access, name, descriptor, signature, exceptions);
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
        } catch (IOException e) {
            log.error("Error analyzing class", e);
        }
    }

    public Set<MethodCall> analyze() {
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
