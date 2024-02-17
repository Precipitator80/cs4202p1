public class MemoryOp {
    public MemoryOp(String programHex, String memoryHex, char kind, int size) {
        this(memoryHex, size);
        // programCounterAddress = convertHexStringToLong(programHex);
        // memoryAddress = convertHexStringToLong(memoryHex);
        programCounterAddress = new BinaryAddress(Utility.hexToBinary(programHex));
        switch (kind) {
            case 'R':
                this.kind = Kind.R;
                break;
            case 'W':
                this.kind = Kind.W;
                break;
            default:
                throw new IllegalArgumentException("Memory operation kind must be either R or W!");
        }
    }

    public MemoryOp(String memoryHex, int size) {
        memoryAddress = new BinaryAddress(Utility.hexToBinary(memoryHex));
        this.size = size;
    }

    enum Kind {
        R,
        W
    }

    BinaryAddress programCounterAddress;
    BinaryAddress memoryAddress;
    Kind kind;
    public int size;

    // long programCounterAddress;
    // long memoryAddress;
    // Kind kind;
    // byte size;

    // private long convertHexStringToLong(String hexString) {
    //     return Long.parseUnsignedLong(hexString);
    // }
}
