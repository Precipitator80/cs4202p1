package com.rainlandsociety;

/**
 * Use to convert hex string to binary string, which is then later converted to a cache-specific address.
     * Parsing required:
     * Hex String → Binary String → Index, Offset and Tag.
 */
public class BinaryAddress {
    public BinaryAddress(String hexString) {
        this.binaryString = Utility.hexToBinary(hexString);
    }

    String binaryString;

    /**
     * An address parser that parses indices, offsets and tags of an address given their size within the address.
     */

    /**
     * Gets the tag of this address for a specific cache.
     * This identifies the block number within the mapped to set.
     * @param cache The cache to translate to.
     * @return The tag of the address for the given cache.
     */
    public int getTag(Cache cache) {
        return Utility.parseBinaryString(binaryString.substring(0, cache.getTagSize()));
    }

    /**
     * Gets the index of this address for a specific cache.
     * This identifies the set number that the address is mapped to.
     * @param cache The cache to translate to.
     * @return The index of the address.
     */
    public int getIndex(Cache cache) {
        int startIndex = cache.getTagSize();
        int endIndex = startIndex + cache.getIndexSize();
        return Utility.parseBinaryString(binaryString.substring(startIndex, endIndex));
    }

    /**
     * Gets the offset of an address for a specific cache.
     * This identifies a chunk of data within a block.
     * @param cache The cache to translate to.
     * @return The offset of the address.
     */
    public int getOffset(Cache cache) {
        int startIndex = cache.getTagSize() + cache.getIndexSize();
        int endIndex = startIndex + cache.getOffsetSize();
        return Utility.parseBinaryString(binaryString.substring(startIndex, endIndex));
    }
}
