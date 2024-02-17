/**
 * Use to convert hex string to binary string, which is then later converted to a cache-specific address.
     * Parsing required:
     * Hex String → Binary String → Index, Offset and Tag.
 */
public class BinaryAddress {
    public BinaryAddress(String hexString) {
        this(Utility.hexToBinary(hexString));
    }

    public BinaryAddress(long binaryLong) {
        this.binaryLong = binaryLong;
    }

    //String binaryString;
    long binaryLong;

    public BinaryAddress nextBlock(Cache cache) {
        return new BinaryAddress(
                Utility.incrementAddress(binaryLong, cache.getTagSize() + cache.getSetIdentSize() - 1));
    }

    /**
     * An address parser that parses indices, offsets and tags of an address given their size within the address.
     * TODO ARE THESE DATA TYPES OK (INT)?
     */

    /**
     * Gets the tag of this address for a specific cache.
     * This identifies the block number within the mapped to set.
     * @param cache The cache to translate to.
     * @return The tag of the address for the given cache.
     */
    public long getTag(Cache cache) {
        int tagSize = cache.getTagSize();
        // Calculate the number of bits needed for the offset and index parts
        int offsetIndexBits = Long.SIZE - tagSize;

        // Create a mask to extract the tag bits
        long tagMask = (1L << tagSize) - 1;

        // Extract the tag bits using bitwise AND
        long tag = binaryLong >>> offsetIndexBits & tagMask;

        return tag;
    }

    // public long getTag(Cache cache) {
    //     return Utility.parseBinaryString(binaryString.substring(0, cache.getTagSize()));
    // }

    /**
     * Gets the index of this address for a specific cache.
     * This identifies the set number that the address is mapped to.
     * @param cache The cache to translate to.
     * @return The index of the address.
     */
    public int getSet(Cache cache) {
        int tagSize = cache.getTagSize();
        int setIdentSize = cache.getSetIdentSize();

        // Calculate the number of bits needed for the offset, index, and tag parts
        int offsetIndexBits = Long.SIZE - (tagSize + setIdentSize);

        // Create a mask to extract the set bits
        long setMask = (1L << setIdentSize) - 1;

        // Extract the set bits using bitwise AND
        int set = (int) ((binaryLong >>> offsetIndexBits) & setMask);

        return set;
    }

    // public int getSet(Cache cache) {
    //     if (cache.getSetIdentSize() == 0) {
    //         return 0;
    //     }
    //     int startIndex = cache.getTagSize();
    //     int endIndex = startIndex + cache.getSetIdentSize();
    //     return (int) Utility.parseBinaryString(binaryString.substring(startIndex, endIndex));
    // }

    /**
     * Gets the offset of an address for a specific cache.
     * This identifies a chunk of data within a block.
     * @param cache The cache to translate to.
     * @return The offset of the address.
     */
    public long getOffset(Cache cache) {
        int tagSize = cache.getTagSize();
        int setIdentSize = cache.getSetIdentSize();
        int offsetIdentSize = cache.getOffsetIdentSize();

        // Calculate the number of bits needed for the offset, index, and tag parts
        int offsetIndexBits = Long.SIZE - (tagSize + setIdentSize + offsetIdentSize);

        // Create a mask to extract the offset bits
        long offsetMask = (1L << offsetIdentSize) - 1;

        // Extract the offset bits using bitwise AND
        long offset = binaryLong & offsetMask;

        return offset;
    }

    // public long getOffset(Cache cache) {
    //     int startIndex = cache.getTagSize() + cache.getSetIdentSize();
    //     int endIndex = startIndex + cache.getOffsetIdentSize();
    //     return Utility.parseBinaryString(binaryString.substring(startIndex, endIndex));
    // }
}
