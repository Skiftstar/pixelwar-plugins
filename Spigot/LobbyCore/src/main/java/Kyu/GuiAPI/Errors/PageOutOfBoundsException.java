package Kyu.GuiAPI.Errors;

public class PageOutOfBoundsException extends RuntimeException {

    public PageOutOfBoundsException(int page, int max) {
        super("Page is out of bounds!\nProvided: " + page + "\nMaximum: " + max);
    }
}
