package me.threedr3am.security.jar.compatibility.scanner;

import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.config.DetectionOptions;
import me.threedr3am.security.jar.compatibility.reader.JarReaderSpace;
import me.threedr3am.security.jar.compatibility.result.CheckType;
import me.threedr3am.security.jar.compatibility.result.Issue;

import java.util.List;

@Slf4j
public class LoadAnalyzer implements Analyzer {

    @Override
    public void init(JarReaderSpace jarReaderSpace, DetectionOptions options) {

    }

    @Override
    public List<Issue> analyze() {
        return List.of();
    }

    @Override
    public CheckType type() {
        return CheckType.Load;
    }
}
