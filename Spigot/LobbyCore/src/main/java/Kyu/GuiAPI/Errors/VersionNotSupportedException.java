package Kyu.GuiAPI.Errors;

public class VersionNotSupportedException extends RuntimeException {
    public VersionNotSupportedException(int slot, int max) {
        super("Slot is out of bounds!\nProvided: " + slot + "\nMaximum: " + max);
    }
}
