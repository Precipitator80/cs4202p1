import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.alibaba.fastjson2.annotation.JSONField;

public class Cache {
    /**
     * JSON fields used in serialisation and / or deserialisation.
     */
    @JSONField(name = "name")
    public String name; // The name of the cache.

    @JSONField(name = "size", serialize = false)
    public int size; // The total size of the cache in bytes.

    @JSONField(name = "line_size", serialize = false)
    public int lineSize; // The size of each cache line in bytes.

    @JSONField(name = "kind", serialize = false)
    public String kind; // The kind of cache.

    @JSONField(name = "replacement_policy", serialize = false)
    public String replacementPolicyString = "rr"; // A string representation of the replacement / eviction policy.

    @JSONField(name = "hits", deserialize = false)
    public int hits; // The number of hits of this cache after simulating a program.

    @JSONField(name = "misses", deserialize = false)
    public int misses; // The number of misses of this cache after simulating a program.

    /**
     * Other variables used throughout the code.
     */
    public static final int ADDRESS_SPACE_SIZE = 64; // The size of the address space in bits. At 64 this is the same as Long.SIZE.
    private ArrayList<LinkedList<CacheLine>> sets; // Sets of cache lines holding memory addresses.
    private ReplacementPolicy replacementPolicy; // The replacement / eviction policy of the cache as an enum value.
    @JSONField(name = "accesses", serialize = false, deserialize = false)
    private int accesses; // The number of accesses of this cache. Used for diagnostics.
    @JSONField(name = "overruns", serialize = false, deserialize = false)
    public int overruns; // The number of overruns of this cache. Used for diagnostics.
    @JSONField(name = "set_size", serialize = false, deserialize = false)
    private int setSize; // The number of cache lines per set.
    @JSONField(serialize = false, deserialize = false)
    private int setBits; // The number of bits used for the set (i.e. index in direct caches) in cache addressing.
    @JSONField(serialize = false, deserialize = false)
    private int offsetBits; // The number of bits used for the offset in cache addressing.
    @JSONField(serialize = false, deserialize = false)
    private int tagBits; // The number of bits used for the tag in cache addressing.

    /**
     * An enum holding replacement / eviction policies to make checking simpler and faster.
     */
    enum ReplacementPolicy {
        RR,
        LRU,
        LFU
    }

    /**
     * An initialisation method used instead of a constructor to let JSON parsing to use the default constructor.
     * @throws Exception if the cache kind specified in the JSON is unsupported.
     */
    void initialise() throws Exception {
        // Define set properties by considering the number of lines in the cache and the kind of cache.
        int numberOfLines = size / lineSize;
        int numberOfSets;
        switch (kind) {
            case ("direct"):
                numberOfSets = numberOfLines;
                break;
            case ("full"):
                numberOfSets = 1;
                break;
            case ("2way"):
                numberOfSets = numberOfLines / 2;
                break;
            case ("4way"):
                numberOfSets = numberOfLines / 4;
                break;
            case ("8way"):
                numberOfSets = numberOfLines / 8;
                break;
            default:
                throw new Exception("The cache type '" + kind + "' is not supported by the simulator!");
        }
        setSize = numberOfLines / numberOfSets;

        // Initialise each set with cache lines.
        sets = new ArrayList<>(numberOfSets);
        for (int setNumber = 0; setNumber < numberOfSets; setNumber++) {
            LinkedList<CacheLine> cacheLines = new LinkedList<>();
            sets.add(cacheLines);
            for (int lineNumber = 0; lineNumber < setSize; lineNumber++) {
                cacheLines.add(new CacheLine());
            }
        }

        // Set the replacement policy.
        switch (replacementPolicyString) {
            case ("lru"):
                replacementPolicy = ReplacementPolicy.LRU;
                break;
            case ("lfu"):
                replacementPolicy = ReplacementPolicy.LFU;
                break;
            default:
                replacementPolicy = ReplacementPolicy.RR;
        }

        // Calculate the number of bits used for each part for addressing this cache.
        setBits = (int) log2(numberOfSets);
        offsetBits = (int) log2(lineSize);
        tagBits = ADDRESS_SPACE_SIZE - setBits - offsetBits;
    }

    /**
     * Returns the logarithm with base 2 of a int value.
     * @param value The value to get the logarithm of.
     * @return The logarithm with base 2 of the input value.
     */
    public double log2(int value) {
        return Math.log(value) / Math.log(2);
    }

    /**
     * Performs a memory operation on a cache line with consideration for replacement policy.
     * @param memoryAddress The memory address of the memory operation.
     * @return Whether the memory operation resulted in a hit or not (miss).
     */
    public boolean performOperation(BinaryAddress memoryAddress) {
        // Keep track of cache accesses.
        accesses++;

        // Get the correct set and block number by translating the memory address to get its set and tag for this cache.
        int setNumber = memoryAddress.getSet(this);
        long tag = memoryAddress.getTag(this);
        LinkedList<CacheLine> set = sets.get(setNumber);

        // Look through the set to look for a line matching the existing tag.
        Iterator<CacheLine> setIterator = set.iterator();
        while (setIterator.hasNext()) {
            CacheLine line = setIterator.next();
            // If the line does not have its valid bit set, it is a compulsory miss.
            if (!line.getValid()) {
                // Track the miss and update the line.
                misses++;
                line.updateTag(tag);

                // Initialise the line.
                switch (replacementPolicy) {
                    case LFU:
                        // Reset the frequency of the line for LFU.
                        line.resetFrequency();
                        break;
                    default:
                        // Move back to the front of the list for LRU and RR.
                        if (line != set.peek()) {
                            setIterator.remove();
                            set.addFirst(line);
                        }
                }
                return false; // Return as a miss.
            } else if (line.getTag() == tag) { // If the line is valid and the tag matches, it is a hit.
                hits++;

                // Refresh the line.
                switch (replacementPolicy) {
                    case LRU:
                        // Move the line back to the front of the cache line list if not already for LRU.
                        if (line != set.peek()) {
                            setIterator.remove();
                            set.addFirst(line);
                        }
                        break;
                    case LFU:
                        // Increment the frequency for LFU.
                        line.incrementFrequency();
                        break;
                    default: // Nothing to do for RR.
                }
                return true; // Return as a hit.
            }
        }

        // If we didn't miss because of the valid bit, then this is either a capacity or a conflict miss.
        // Evict and replace the line.
        misses++;
        replace(set, tag);
        return false; // Return as a miss.
    }

    /**
     * Evicts a block from the cache and replaces it.
     * @param set The set that the cache line is a part of.
     * @param newTag The tag of the new block.
     */
    void replace(LinkedList<CacheLine> set, long newTag) {
        switch (replacementPolicy) {
            case LFU:
                // Evict the least frequently used cache line for LFU by checking the frequency of each.
                CacheLine leastFrequentlyUsed = set.getFirst();
                for (CacheLine line : set) {
                    if (line.getFrequency() < leastFrequentlyUsed.getFrequency()) {
                        leastFrequentlyUsed = line;
                    }
                }
                leastFrequentlyUsed.updateTag(newTag);
                leastFrequentlyUsed.resetFrequency();
                break;
            default:
                // Move the line at the back of the cache line list to the front and update it with the new tag for LRU and RR.
                CacheLine lastLine = set.removeLast();
                lastLine.updateTag(newTag);
                set.addFirst(lastLine);
        }
    }

    /**
     * Getters
     */

    /**
     * Gets the number of tag bits used by this cache.
     * @return The number of tag bits used by this cache.
     */
    public int getTagBits() {
        return tagBits;
    }

    /**
     * Gets the number of set bits used by this cache.
     * @return The number of set bits used by this cache.
     */
    public int getSetBits() {
        return setBits;
    }

    /**
     * Gets the number of offset bits used by this cache.
     * @return The number of offset bits used by this cache.
     */
    public int getOffsetBits() {
        return offsetBits;
    }
}