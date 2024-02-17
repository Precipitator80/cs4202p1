public class CacheLine {
    private long tag;
    private boolean valid;
    private int frequency;

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

    public int getFrequency() {
        return frequency;
    }

    public void incrementFrequency() {
        frequency++;
    }

    public void resetFrequency() {
        frequency = 1;
    }
}
