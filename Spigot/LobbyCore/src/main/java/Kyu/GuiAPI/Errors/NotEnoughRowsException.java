package Kyu.GuiAPI.Errors;

public class NotEnoughRowsException extends RuntimeException {

    public NotEnoughRowsException() {
        super("You need at least 2 rows for this!");
    }

}
