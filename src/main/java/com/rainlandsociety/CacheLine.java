package com.rainlandsociety;

public class CacheLine {
    private long tag;
    private boolean valid;

    public boolean updateTag(Cache cache, long tag) {
        // Compulsory miss || capacity / conflict miss
        if (!valid || valid && this.tag != tag) {
            this.tag = tag;
            valid = true;
            return false;
        } else {
            return true;
        }
    }
}
