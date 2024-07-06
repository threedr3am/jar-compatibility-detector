package me.threedr3am.security.jar.compatibility.analyzer;

import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.World;
import me.threedr3am.security.jar.compatibility.callgraph.FieldLoadStore;
import me.threedr3am.security.jar.compatibility.callgraph.MethodCall;
import me.threedr3am.security.jar.compatibility.cha.ClassInfo;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;
import me.threedr3am.security.jar.compatibility.result.CheckType;
import me.threedr3am.security.jar.compatibility.result.Issue;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FieldLoadStoreAnalyzer implements Analyzer {

    private World world;

    @Override
    public void init(World world) {
        this.world = world;
    }

    @Override
    public List<Issue> analyze() {
        Set<FieldLoadStore> noExistLoadStores = world.getFieldLoadStores().values().stream()
                .filter(fieldLoadStore -> world.getOptions().getPkg() == null || isTargetLoadStore(world.getOptions().getPkg(), fieldLoadStore))
                .filter(fieldLoadStore -> world.getOptions().getJar() == null || isTargetJarLoadStore(world.getOptions().getJar(), fieldLoadStore))
                .filter(fieldLoadStore -> !existField(fieldLoadStore.getOwner(), fieldLoadStore))
                .collect(Collectors.toSet());
        if (noExistLoadStores.isEmpty()) {
            return Collections.emptyList();
        }
        return noExistLoadStores.stream().map(this::transfer).collect(Collectors.toList());
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
        return CheckType.FieldLoadStore;
    }


    private boolean isTargetLoadStore(String pkg, FieldLoadStore fieldLoadStore) {
        if (fieldLoadStore.getOwner().startsWith(pkg)) {
            return true;
        }
        for (MethodInfo loader : fieldLoadStore.getLoadStores()) {
            if (loader.getDeclaringClass().startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTargetJarLoadStore(String optionJar, FieldLoadStore fieldLoadStore) {
        ClassInfo ownClass = world.getClass(fieldLoadStore.getOwner());
        if (ownClass != null && matchJar(ownClass.getJar(), optionJar)) {
            return true;
        }
        for (MethodInfo loadStore : fieldLoadStore.getLoadStores()) {
            if (matchJar(loadStore.getJar(), optionJar)) {
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

    private boolean existField(String owner, FieldLoadStore fieldLoadStore) {
        ClassInfo classInfo = world.getClass(owner);
        if (classInfo == null || classInfo.getMethods().isEmpty()) {
            return false;
        }
        if (classInfo.getFields().containsKey(fieldLoadStore.getField())) {
            return true;
        }
        return existField(classInfo.getSuperName(), fieldLoadStore);
    }
}
