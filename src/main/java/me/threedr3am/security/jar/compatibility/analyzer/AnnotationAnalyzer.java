package me.threedr3am.security.jar.compatibility.analyzer;

import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.World;
import me.threedr3am.security.jar.compatibility.result.CheckType;
import me.threedr3am.security.jar.compatibility.result.Issue;

import java.util.List;

@Slf4j
public class AnnotationAnalyzer implements Analyzer {

    private World world;

    @Override
    public void init(World world) {
        this.world = world;
    }

    @Override
    public List<Issue> analyze() {
        return List.of();
    }

    @Override
    public CheckType type() {
        return CheckType.Annotation;
    }
}
