package me.threedr3am.security.jar.compatibility.config;

import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Data
@Command(name = "Detection", mixinStandardHelpOptions = true)
public class DetectionOptions {

    @Option(names = {"-t", "--target"}, description = "Target jar or dir to detect compatibility", required = true)
    private String target;

    @Option(names = {"-o", "--output"}, description = "Output file")
    private String output;

    @Option(names = {"-jv", "--javaVersion"}, description = "Target jars need jre(java version:3,4,5,6,7,8) to detect", defaultValue = "8")
    private int javaVersion;

    @Option(names = {"-p", "--package"}, description = "Extract target package detection result")
    private String pkg;

    @Option(names = {"-j", "--jar"}, description = "Extract target jar detection result")
    private String jar;

    public static DetectionOptions parse(String... args) {
        DetectionOptions options = CommandLine.populateCommand(new DetectionOptions(), args);
        return postProcess(options);
    }

    private static DetectionOptions postProcess(DetectionOptions options) {
        return options;
    }
}