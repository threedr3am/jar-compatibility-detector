package me.threedr3am.security.jar.compatibility.reader;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.threedr3am.security.jar.compatibility.config.DetectionOptions;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
@Data
public class JarReaderSpace {

    private List<ClazzReader> readers = new ArrayList<>();
    private List<ClazzReader> jreReaders = new ArrayList<>();

    protected static final String JREs = "JREs";

    public void read(DetectionOptions options) {
        scanJreClasses(options);
        Path target = Path.of(options.getTarget());
        if (Files.isDirectory(target)) {
            scanDirectory(target);
        } else {
            processJar(target);
        }
    }

    private void scanJreClasses(DetectionOptions options) {
        File jreDir = new File(JREs);
        if (!jreDir.exists()) {
            throw new RuntimeException("JREs directory may be required for normal operation!");
        }
        String jrePath = String.format("%s/jre1.%d",
                JREs, options.getJavaVersion());
        // 遍历lib目录下的所有.jar文件
        File[] jarFiles = new File(jrePath).listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles != null) {
            try {
                for (File jarFile : jarFiles) {
                    JarFile jar = new JarFile(jarFile);
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            read(jar, entry, true);
                        }
                    }
                    jar.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void scanDirectory(Path dir) {
        try {
            Files.walk(dir).filter(path -> path.toString().endsWith(".jar")).forEach(this::processJar);
        } catch (IOException e) {
            log.error("Error scanning directory", e);
        }
    }

    private void processJar(Path jarPath) {
        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            zipFile.stream().forEach(entry -> {
                if (entry.getName().endsWith(".class")) {
                    read(zipFile, entry, false);
                } else if (entry.getName().endsWith(".jar")) {
                    try {
                        Path nestedJarPath = Files.createTempFile("nested-jar-", ".tmp");
                        Files.copy(zipFile.getInputStream(entry), nestedJarPath);
                        processJar(nestedJarPath);
                        Files.delete(nestedJarPath);
                    } catch (IOException e) {
                        log.error("Error processing nested JAR", e);
                    }
                }
            });
        } catch (IOException e) {
            log.error("Error processing JAR", e);
        }
    }

    private void read(ZipFile zipFile, ZipEntry entry, boolean isJre) {
        try {
            ClazzReader clazzReader = new ClazzReader(zipFile.getName(), new ClassReader(zipFile.getInputStream(entry)));
            (isJre ? jreReaders : readers).add(clazzReader);
        } catch (IOException e) {
            log.error("Error read class", e);
        }
    }
}
