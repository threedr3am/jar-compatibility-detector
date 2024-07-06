package me.threedr3am.security.jar.compatibility.analyzer;

import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.World;
import me.threedr3am.security.jar.compatibility.callgraph.MethodCall;
import me.threedr3am.security.jar.compatibility.cha.ClassInfo;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;
import me.threedr3am.security.jar.compatibility.result.CheckType;
import me.threedr3am.security.jar.compatibility.result.Issue;

import java.util.*;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;


@Slf4j
public class MethodCallAnalyzer implements Analyzer {

    private World world;

    @Override
    public void init(World world) {
        this.world = world;
    }

    @Override
    public List<Issue> analyze() {
        Set<MethodCall> noExistCalls = world.getMethodCalls().values().stream()
                .filter(methodCall -> world.getOptions().getPkg() == null || isTargetCall(world.getOptions().getPkg(), methodCall))
                .filter(methodCall -> !existCallee(methodCall.getOwner(), methodCall))
                .collect(Collectors.toSet());
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
        return CheckType.MethodCall;
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
        ClassInfo classInfo = world.getClass(owner);
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
