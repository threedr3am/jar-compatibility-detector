package me.threedr3am.security.jar.compatibility.reader;

import lombok.Data;
import org.objectweb.asm.ClassReader;

@Data
public class ClazzReader {
    private String jar;
    private ClassReader classReader;

    public ClazzReader(String jar, ClassReader classReader) {
        this.jar = jar;
        this.classReader = classReader;
    }
}
