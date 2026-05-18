package com.kotak.onboarding.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RmRoundRobinService {

    public record Rm(String email, String name) {}

    private final List<Rm> rmList = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public RmRoundRobinService(@Value("${moxo.rm.list}") String rmListConfig) {
        for (String entry : rmListConfig.split(",")) {
            String[] parts = entry.trim().split(":", 2);
            if (parts.length == 2) {
                rmList.add(new Rm(parts[0].trim(), parts[1].trim()));
            }
        }
    }

    public Rm pickNext() {
        int index = counter.getAndIncrement() % rmList.size();
        return rmList.get(index);
    }
}
