package com.rainlandsociety;

public class AddressParser {
    private int indexSize;
    private int offsetSize;
    private int tagSize;

    public AddressParser(int indexSize, int offsetSize, int tagSize) {
        this.indexSize = indexSize;
        this.offsetSize = offsetSize;
        this.tagSize = tagSize;
    }

    public String getTag(String address) {
        return address.substring(0, tagSize);
    }

    public String getIndex(String address) {
        int startIndex = tagSize;
        int endIndex = startIndex + indexSize;
        return address.substring(startIndex, endIndex);
    }

    public String getOffset(String address) {
        int startIndex = tagSize + indexSize;
        int endIndex = startIndex + offsetSize;
        return address.substring(startIndex, endIndex);
    }
}