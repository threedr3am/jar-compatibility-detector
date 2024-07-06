package me.threedr3am.security.jar.compatibility.cha;

import lombok.Data;

@Data
public class ParameterInfo {
    private String jar;
    private String declaringClass;
    private String methodName;
    private String methodDesc;

    private int index;
    private String type;

    public ParameterInfo(String jar, String declaringClass, String methodName, String methodDesc, int index, String type) {
        this.jar = jar;
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.index = index;
        this.type = type;
    }
}
