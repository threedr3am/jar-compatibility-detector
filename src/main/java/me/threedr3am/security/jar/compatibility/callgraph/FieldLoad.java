package me.threedr3am.security.jar.compatibility.callgraph;

import lombok.Data;
import me.threedr3am.security.jar.compatibility.cha.MethodInfo;

import java.util.HashSet;
import java.util.Set;

@Data
public class FieldLoad {

    private Set<MethodInfo> loaders = new HashSet<>();

    private final String owner;
    private final String field;
    private final String type;

    public FieldLoad(String owner, String field, String type) {
        this.owner = owner;
        this.field = field;
        this.type = type;
    }
}
