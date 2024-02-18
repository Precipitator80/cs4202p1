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
    @JSONField(name = "block_overruns", serialize = false, deserialize = false)
    public int blockOverruns; // The number of block overruns of this cache. Used for diagnostics.
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
        setBits = (int) Utility.log2(numberOfSets);
        offsetBits = (int) Utility.log2(lineSize);
        tagBits = ADDRESS_SPACE_SIZE - setBits - offsetBits;
    }

    /**
     * TODO
     * @param memoryAddress
     * @return
     */
    public boolean performOperation(BinaryAddress memoryAddress) {
        accesses++;
        int setNumber = memoryAddress.getSet(this);
        long tag = memoryAddress.getTag(this);
        LinkedList<CacheLine> set = sets.get(setNumber);
        Iterator<CacheLine> setIterator = set.iterator();
        while (setIterator.hasNext()) {
            CacheLine line = setIterator.next();
            if (!line.getValid()) {
                // Compulsory miss!
                misses++;
                line.updateTag(tag);
                // Move back to the front of the list for LRU and RR.
                switch (replacementPolicy) {
                    case LFU:
                        line.resetFrequency();
                        break;
                    default:
                        if (line != set.peek()) {
                            setIterator.remove();
                            set.addFirst(line);
                        }
                }
                return false;
            } else if (line.getTag() == tag) {
                // Hit!
                hits++;

                // Refresh
                switch (replacementPolicy) {
                    case LRU:
                        if (line != set.peek()) {
                            setIterator.remove();
                            set.addFirst(line);
                        }
                        break;
                    case LFU:
                        // Refresh frequency
                        line.incrementFrequency();
                        break;
                    default: // Nothing to do for RR.
                }
                return true;
            }
        }
        // Capacity / conflict miss! Evict and replace.
        misses++;
        replace(set, tag);
        return false;
    }

    /**
     * Evicts a block from the cache and replaces it.
     * @param set The set that the cache line is a part of.
     * @param newTag The tag of the new block.
     */
    void replace(LinkedList<CacheLine> set, long newTag) {
        switch (replacementPolicy) {
            case LRU:
                CacheLine leastRecentlyUsed = set.removeLast();
                leastRecentlyUsed.updateTag(newTag);
                set.addFirst(leastRecentlyUsed);
                break;
            case LFU:
                // Evict least frequently used cache line
                CacheLine leastFrequentlyUsed = set.getFirst();
                for (CacheLine line : set) {
                    if (line.getFrequency() < leastFrequentlyUsed.getFrequency()) {
                        leastFrequentlyUsed = line;
                    }
                }
                leastFrequentlyUsed.updateTag(newTag);
                leastFrequentlyUsed.resetFrequency();
                break;
            default: // RR
                CacheLine last = set.removeLast();
                last.updateTag(newTag);
                set.addFirst(last);
        }
    }

    /**
     * Getters
     */
    public int getSetBits() {
        return setBits;
    }

    public int getOffsetBits() {
        return offsetBits;
    }

    public int getTagBits() {
        return tagBits;
    }
}