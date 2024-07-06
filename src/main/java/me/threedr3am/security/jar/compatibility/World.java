package me.threedr3am.security.jar.compatibility;

import lombok.Data;
import me.threedr3am.security.jar.compatibility.callgraph.FieldLoadStore;
import me.threedr3am.security.jar.compatibility.callgraph.MethodCall;
import me.threedr3am.security.jar.compatibility.cha.ClassInfo;
import me.threedr3am.security.jar.compatibility.config.DetectionOptions;

import java.util.HashMap;
import java.util.Map;

@Data
public class World {

    private final HashMap<String, ClassInfo> classes = new HashMap<>();
    private final Map<String, MethodCall> methodCalls = new HashMap<>();
    private final Map<String, FieldLoadStore> fieldLoadStores = new HashMap<>();
    private final DetectionOptions options;

    public World(DetectionOptions options) {
        this.options = options;
    }

    public ClassInfo getClass(String name) {
        return classes.get(name);
    }

    public MethodCall getMethodCall(String name) {
        return methodCalls.get(name);
    }

    public FieldLoadStore getFieldLoadStore(String name) {
        return fieldLoadStores.get(name);
    }

    public void registerClass(String name, ClassInfo classInfo) {
        classes.put(name, classInfo);
    }

    public MethodCall registerMethodCall(String name, MethodCall methodCall) {
        return methodCalls.computeIfAbsent(name, k -> methodCall);
    }

    public FieldLoadStore registerFieldLoadStore(String name, FieldLoadStore fieldLoadStore) {
        return fieldLoadStores.computeIfAbsent(name, k -> fieldLoadStore);
    }
}
