package com.rainlandsociety;

public class MemoryOp {
    public MemoryOp(String programHex, String memoryHex, char kind, String size) {
        programCounterAddress = convertHexStringToLong(programHex);
        memoryAddress = convertHexStringToLong(memoryHex);
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
        this.size = Byte.parseByte(size);
    }

    enum Kind {
        R,
        W
    }

    long programCounterAddress;
    long memoryAddress;
    Kind kind;
    byte size;

    private long convertHexStringToLong(String hexString) {
        return Long.parseUnsignedLong(hexString);
    }
}
