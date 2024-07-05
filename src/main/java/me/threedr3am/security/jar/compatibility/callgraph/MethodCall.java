package me.threedr3am.security.jar.compatibility.callgraph;

import lombok.Data;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.Set;

@Data
public class MethodCall {

    private Set<MethodInfo> callers = new HashSet<>();

    private final String owner;
    private final String name;
    private final String desc;
    private final String returnType;
    private final String[] argumentTypes;

    public MethodCall(String owner, String name, String desc, String returnType, Type[] argTypes) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.returnType = returnType;
        this.argumentTypes = new String[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            this.argumentTypes[i] = argTypes[i].getClassName();
        }
    }

    @Override
    public String toString() {
        return String.format("%s.%s(%s)",
                owner,
                name,
                desc);
    }
}
