package me.threedr3am.security.jar.compatibility.config;

import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Data
@Command(name = "Detection", mixinStandardHelpOptions = true)
public class DetectionOptions {

    @Option(names = {"-t", "--target"}, description = "Target jar or dir to detect", required = true)
    private String target;

    @Option(names = {"-o", "--output"}, description = "Output file")
    private String output;

    @Option(names = {"-j", "--javaVersion"}, description = "Target java version to detect", defaultValue = "8")
    private int javaVersion;

    @Option(names = {"-p", "--package"}, description = "Target java root package to detect")
    private String pkg;

    public static DetectionOptions parse(String... args) {
        DetectionOptions options = CommandLine.populateCommand(new DetectionOptions(), args);
        return postProcess(options);
    }

    private static DetectionOptions postProcess(DetectionOptions options) {
        return options;
    }
}