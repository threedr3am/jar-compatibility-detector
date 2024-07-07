package me.threedr3am.security.jar.compatibility.result;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Issue {

    private final String id;
    private final String title;
    private final String description;
    private final CheckType checkType;
    private static AtomicInteger counter = new AtomicInteger(1);

    public Issue(String title, String description, CheckType checkType) {
        this.id = counter.getAndIncrement() + "";
        this.title = title;
        this.description = description;
        this.checkType = checkType;
    }
}
