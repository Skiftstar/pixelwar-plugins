package Kyu.ServerCoreBungee.Bansystem;

import Kyu.ServerCoreBungee.Bansystem.HelperClasses.Pair;
import Kyu.ServerCoreBungee.Main;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class ConfirmCMD extends Command {

    private Main main;
    public Map<CommandSender, Pair<Consumer<Void>, ScheduledTask>> functions = new HashMap<>();

    public ConfirmCMD(Main main) {
        super("banconfirm", "bcore.ban", "bconfirm", "punishconfirm", "pconfirm");
        this.main = main;
        main.getProxy().getPluginManager().registerCommand(main, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!functions.containsKey(sender)) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NoConfirmationPresent", true)));
            return;
        }
        Pair<Consumer<Void>, ScheduledTask> pair = functions.get(sender);
        pair.first.accept(null);
        pair.second.cancel();
        functions.remove(sender);
        sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "BanConfirmed", true)));
    }

    public void addFunction(CommandSender sender, Consumer<Void> function) {
        functions.remove(sender);

        ScheduledTask task = main.getProxy().getScheduler().schedule(main, () -> {
            functions.remove(sender);
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "ConfirmationTimeout", true)));
        }, Main.confirmTimeout, TimeUnit.SECONDS);
        functions.put(sender, new Pair<>(function, task));
    }
}