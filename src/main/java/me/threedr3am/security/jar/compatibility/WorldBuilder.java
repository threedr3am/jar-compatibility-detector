package me.threedr3am.security.jar.compatibility;

import me.threedr3am.security.jar.compatibility.callgraph.FieldLoadStore;
import me.threedr3am.security.jar.compatibility.callgraph.MethodCall;
import me.threedr3am.security.jar.compatibility.cha.ClassInfo;
import me.threedr3am.security.jar.compatibility.cha.FieldInfo;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;
import me.threedr3am.security.jar.compatibility.config.DetectionOptions;
import me.threedr3am.security.jar.compatibility.reader.ClazzReader;
import me.threedr3am.security.jar.compatibility.reader.JarReaderSpace;
import org.objectweb.asm.*;

import java.util.HashSet;
import java.util.Set;

public class WorldBuilder {

    public static World build(JarReaderSpace jarReaderSpace, DetectionOptions options) {
        World world = new World(options);
        jarReaderSpace.getJreReaders().forEach(clazzReader -> parse(world, clazzReader, true));
        jarReaderSpace.getReaders().forEach(clazzReader -> parse(world, clazzReader, false));
        return world;
    }

    private static void parse(World world, ClazzReader clazzReader, boolean isJre) {
        clazzReader.getClassReader().accept(new ClassVisitor(Opcodes.ASM9) {

            private ClassInfo classInfo;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                String className = Type.getObjectType(name).getClassName();
                String superClassName = null;
                if (superName != null) {
                    superClassName = Type.getObjectType(superName).getClassName();
                }
                Set<String> interfacesSet;
                if (interfaces != null) {
                    interfacesSet = new HashSet<>();
                    for (String anInterface : interfaces) {
                        interfacesSet.add(Type.getObjectType(anInterface).getClassName());
                    }
                }
                this.classInfo = new ClassInfo(clazzReader.getJar(), access, className, signature, superClassName);
                world.registerClass(className, this.classInfo);
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
                    public void visitMethodInsn(int opcode, String owner, String methodName, String methodDesc, boolean itf) {
                        Type ownerType = Type.getObjectType(owner);
                        Type[] argTypes = Type.getArgumentTypes(methodDesc);
                        Type returnType = Type.getReturnType(methodDesc);

                        String calleeName = owner + "." + methodName + methodDesc;
                        MethodCall call = world.registerMethodCall(calleeName,
                                new MethodCall(ownerType.getClassName(), methodName, methodDesc, returnType.getClassName(), argTypes));
                        call.getCallers().add(methodInfo);
                        super.visitMethodInsn(opcode, owner, methodName, methodDesc, itf);
                    }

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        Type ownerType = Type.getObjectType(owner);
                        Type fieldType = Type.getObjectType(descriptor);

                        String fieldName = ownerType.getClassName() + "." + name;
                        FieldLoadStore fieldLoadStore = world.registerFieldLoadStore(fieldName,
                                new FieldLoadStore(ownerType.getClassName(), name, fieldType.getClassName()));
                        fieldLoadStore.getLoadStores().add(methodInfo);
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }
}
