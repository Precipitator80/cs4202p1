package com.rainlandsociety;

public class MemoryOp {
    public MemoryOp(String programHex, String memoryHex, char kind, int size) {
        // programCounterAddress = convertHexStringToLong(programHex);
        // memoryAddress = convertHexStringToLong(memoryHex);
        programCounterAddress = programHex;
        memoryAddress = memoryHex;
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
        // this.size = Byte.parseByte(size);
        this.size = size;
    }

    enum Kind {
        R,
        W
    }

    String programCounterAddress;
    public String memoryAddress;
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
