package kyu.cities.Commands;

import java.util.ArrayList;
import java.util.List;

import Kyu.SCommand;
import kyu.cities.Main;
import kyu.cities.Util.Player.CPlayer;
import kyu.cities.Util.Player.Job;

public class JobCommand {

    public static void init() {
        SCommand command = new SCommand(Main.getInstance(), "job", Main.helper);
        command.playerOnly(true);
        command.execPerm("cities.job");
        command.minArgs(1);

        command.exec(e -> {
            CPlayer p = CPlayer.getCPlayer(e.player());
            //TODO: Join, Leave, info, in Join check if max jobs reached, leave cost
            if (e.args()[0].equalsIgnoreCase("list")) {
                List<String> jobNamesTranslated = new ArrayList<>();
                for (String jobName : Job.getJobs()) {
                    jobNamesTranslated.add("- " + Main.helper.getMess(e.player(), jobName.toUpperCase()));
                }
                p.sendMessage(Main.helper.getMess(e.player(), "JobsListMessage", true)
                    .replace("%list", String.join("\n", jobNamesTranslated)));
                return;
            }

            if (e.args()[0].equalsIgnoreCase("help")) {
                p.sendMessage(Main.helper.getMess(e.player(), "JobInfoMessage", true)
                    .replace("%swapCost", Job.swapCost + "")
                    .replace("%maxJobs", Job.maxJobs + ""));
                return;
            }

            if (e.args()[0].equalsIgnoreCase("join")) {
                if (p.getJobs().size() > Job.maxJobs) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MaxJobAmountReached", true));
                    return;
                }
                if (e.args().length < 2) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NEArgs", true));
                    return;
                }
                String jobName = e.args()[1];
                Job job = Job.getJob(jobName);
                if (job == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NoSuchJob", true));
                    return;
                }
                if (p.hasJob(job)) {
                    p.sendMessage(Main.helper.getMess(e.player(), "AlreadyInJob", true));
                    return;
                }
                p.addJob(job);
                p.sendMessage(Main.helper.getMess(e.player(), "JobJoinedSuccess", true)
                    .replace("%jobName", Main.helper.getMess(e.player(), job.getName().toUpperCase())));
                return;
            }

            if (e.args()[0].equalsIgnoreCase("leave")) {
                if (e.args().length < 2) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NEArgs", true));
                    return;
                }
                String jobName = e.args()[1];
                Job job = Job.getJob(jobName);
                if (job == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NoSuchJob", true));
                    return;
                }
                if (!p.hasJob(job)) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NotInJob", true));
                    return;
                }
                //TODO: Cost
                p.removeJob(job);
                p.sendMessage(Main.helper.getMess(e.player(), "JobLeftSuccess", true)
                    .replace("%jobName", Main.helper.getMess(e.player(), job.getName().toUpperCase())));

            }
        });

        Main.commands.add(command);
    }
}
    
