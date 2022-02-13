package kyu.npcshop.CustomVillagers.GUI.Errors;

public class AttemptToChangePlaceholderException extends RuntimeException {

    public AttemptToChangePlaceholderException() {
        super("You cannot edit/replace a placeholder.");
    }

}
