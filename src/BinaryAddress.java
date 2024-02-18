/**
 * Class to store and get parts of a binary address.
 * Conversion: Hex String → Binary long → Tag, Set and Offset.
 */
public class BinaryAddress {
    /**
     * Initialises a binary address by converting a hex string into a long.
     * @param hexString The hexadecimal string representation of a binary address.
     */
    public BinaryAddress(String hexString) {
        this(Long.parseLong(hexString, 16));
    }

    /**
     * Initialises a binary address with the a long representation of a binary address.
     * @param binaryLong The long representation of a binary address.
     */
    public BinaryAddress(long binaryLong) {
        this.address = binaryLong;
    }

    long address; // A long representing a binary address. Longs use 64-bits, which lets masking be used to check individual parts of the address.

    /**
     * Gets the next block adjacent to the block this binary address represents.
     * @param cache The cache used for translation.
     * @return The next block adjacent to this address.
     */
    public BinaryAddress nextBlock(Cache cache) {
        // Add the long value of the lowest bit associated with the offset.
        long valueToAdd = 1L << cache.getOffsetBits();
        long adjacentAddress = address + valueToAdd;
        return new BinaryAddress(adjacentAddress);
    }

    /**
     * Gets the tag of this address for a specific cache.
     * This is held at the start of an address and identifies the block number within the mapped to set.
     * @param cache The cache to translate to.
     * @return The tag of the address for the given cache.
     */
    public long getTag(Cache cache) {
        // Determine the number of bits to shift the address to the right to remove the set and offset.
        int bitsToShift = Cache.ADDRESS_SPACE_SIZE - cache.getTagBits();

        // Get the tag by doing an unsigned shift to the right, getting rid of the other parts of the address.
        return address >>> bitsToShift;
    }

    /**
     * Gets the set number of this address for a specific cache.
     * This is held in the middle of an address.
     * @param cache The cache to translate to.
     * @return The set number of the address.
     */
    public int getSet(Cache cache) {
        // Determine the number of bits to shift the address to the right to remove the offset.
        int offsetBits = cache.getOffsetBits();

        // Define a mask to take only the final bits of the shifted address, which correspond to the set.
        long setMask = (1L << cache.getSetBits()) - 1;

        // Get the set by shifting the address and then extracting the final bits using the mask.
        return (int) ((address >>> offsetBits) & setMask);
    }

    /**
     * Gets the offset of an address for a specific cache.
     * This is held at the end of an address and identifies a chunk of data within a block.
     * @param cache The cache to translate to.
     * @return The offset of the address.
     */
    public long getOffset(Cache cache) {
        // Define a mask to take only the final bits of the address, which correspond to the offset.
        long offsetMask = (1L << cache.getOffsetBits()) - 1;

        // Get the offset by masking the address using the offset mask.
        return address & offsetMask;
    }

    /// String version of BinaryAddress. This was significantly slower compared to using a long representation due to the large number of string operations required.
    /**
     * Conversion from hex string:
     * Hex String → Binary String → Tag, Set and Offset.
     */

    //String binaryString; // A 64 character string of 0s and 1s representing a 64-bit binary address.

    // public long getTag(Cache cache) {
    //     return Utility.parseBinaryString(binaryString.substring(0, cache.getTagSize()));
    // }

    // public int getSet(Cache cache) {
    //     if (cache.getSetIdentSize() == 0) {
    //         return 0;
    //     }
    //     int startIndex = cache.getTagSize();
    //     int endIndex = startIndex + cache.getSetIdentSize();
    //     return (int) Utility.parseBinaryString(binaryString.substring(startIndex, endIndex));
    // }

    // public long getOffset(Cache cache) {
    //     int startIndex = cache.getTagSize() + cache.getSetIdentSize();
    //     int endIndex = startIndex + cache.getOffsetIdentSize();
    //     return Utility.parseBinaryString(binaryString.substring(startIndex, endIndex));
    // }
}
