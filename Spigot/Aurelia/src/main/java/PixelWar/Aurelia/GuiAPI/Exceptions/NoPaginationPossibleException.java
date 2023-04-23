package PixelWar.Aurelia.GuiAPI.Exceptions;


public class NoPaginationPossibleException extends RuntimeException {

    public NoPaginationPossibleException() {
        super("You need to have atleast 2 rows for pagination!");
    }
    
}
