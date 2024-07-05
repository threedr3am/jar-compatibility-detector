package me.threedr3am.security.jar.compatibility;

import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.config.DetectionOptions;
import me.threedr3am.security.jar.compatibility.reader.JarReaderSpace;
import me.threedr3am.security.jar.compatibility.result.Issue;
import me.threedr3am.security.jar.compatibility.scanner.Analyzer;
import me.threedr3am.security.jar.compatibility.scanner.CallAnalyzer;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

@Slf4j
public class Detection {

    public static void main(String[] args) {
        DetectionOptions options = DetectionOptions.parse(args);
        JarReaderSpace jarReaderSpace = new JarReaderSpace();
        jarReaderSpace.read(options);
        ServiceLoader<Analyzer> analyzerServiceLoader = ServiceLoader.load(Analyzer.class);
        Iterator<Analyzer> analyzerIterator = analyzerServiceLoader.iterator();
        while (analyzerIterator.hasNext()) {
            Analyzer analyzer = analyzerIterator.next();
            analyzer.init(jarReaderSpace, options);
            List<Issue> issues = analyzer.analyze();
            issues.forEach(issue -> log.info("""
            ------------------------------------------------------------------------------------------------------
            id: {}
            title: {}
            description: {}
            """, issue.getId(), issue.getTitle(), issue.getDescription()));
        }
    }
}
