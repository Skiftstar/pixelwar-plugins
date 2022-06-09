package kyu.cities.Commands;

import Kyu.SCommand;
import kyu.cities.Main;

public class JobCommand {

    public static void init() {
        SCommand command = new SCommand(Main.getInstance(), "job", Main.helper);
        command.playerOnly(true);
        command.execPerm("cities.job");
        command.minArgs(2);
        command.exec(e -> {
            //TODO: Join, Leave, Info Command, in Join check if max jobs reached, leave cost
        });

        Main.commands.add(command);
    }
}
    
