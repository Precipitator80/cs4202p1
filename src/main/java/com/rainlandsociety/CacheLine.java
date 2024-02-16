package com.rainlandsociety;

public class CacheLine {
    private long tag;
    private boolean valid;

    public void updateTag(long tag) {
        this.tag = tag;
        valid = true;
    }

    public long getTag() {
        return tag;
    }

    public boolean getValid() {
        return valid;
    }
}
