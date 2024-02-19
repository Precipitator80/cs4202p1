/**
 * Represents the line of a cache, storing information about its current tag and valid bit.
 */
public class CacheLine {
    private long tag; // The tag of the memory address "stored" in the line.
    private boolean valid; // Valid bit representing whether the line has valid data or not.
    private int frequency; // Frequency counter used for the least frequently used replacement / eviction policy.

    /**
     * Updates the tag in the line and sets the valid bit to true as part of a memory operation.
     * @param tag The tag of the memory address of the operation.
     */
    public void updateTag(long tag) {
        this.tag = tag;
        valid = true;
    }

    /**
     * Getters and methods for managing frequency.
     */

    /**
     * Gets the tag of the line.
     * @return The tag of the line.
     */
    public long getTag() {
        return tag;
    }

    /**
     * Gets the valid bit of the line.
     * @return The valid bit of the line.
     */
    public boolean getValid() {
        return valid;
    }

    /**
     * Gets the frequency of the line.
     * @return The frequency of the line.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Increments the frequency counter.
     */
    public void incrementFrequency() {
        frequency++;
    }

    /**
     * Resets the frequency counter to 1.
     * Should be used after the line is updated.
     */
    public void resetFrequency() {
        frequency = 1;
    }
}
