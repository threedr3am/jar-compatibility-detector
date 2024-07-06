package me.threedr3am.security.jar.compatibility.analyzer;

import me.threedr3am.security.jar.compatibility.World;
import me.threedr3am.security.jar.compatibility.config.DetectionOptions;
import me.threedr3am.security.jar.compatibility.reader.JarReaderSpace;
import me.threedr3am.security.jar.compatibility.result.CheckType;
import me.threedr3am.security.jar.compatibility.result.Issue;

import java.util.List;

public interface Analyzer {

    void init(World world);

    List<Issue> analyze();

    CheckType type();
}
