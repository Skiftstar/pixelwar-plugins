package PixelWar.Aurelia.API.GuiAPI.Exceptions;

public class SlotOutOfBoundsException extends RuntimeException{

    public SlotOutOfBoundsException(int slot, int max) {
        super("Slot is out of bounds!\nProvided: " + slot + "\nMaximum: " + max);
    }

}
