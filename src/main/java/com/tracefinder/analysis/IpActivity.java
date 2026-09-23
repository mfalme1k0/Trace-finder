package com.tracefinder.analysis;

public class IpActivity {
    private final String ip;
    private final int entryCount;

    public IpActivity(String ip, int entryCount) {
        this.ip = ip;
        this.entryCount = entryCount;
    }

    public String getIp() { return ip; }
    public int getEntryCount() { return entryCount; }
}