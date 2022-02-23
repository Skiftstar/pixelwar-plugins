package kyu.npcshop.CustomVillagers.GUI.Errors;

public class SignLineException extends RuntimeException {

    public SignLineException() {
        super("Signs must have 4 lines provided!");
    }
    
}
