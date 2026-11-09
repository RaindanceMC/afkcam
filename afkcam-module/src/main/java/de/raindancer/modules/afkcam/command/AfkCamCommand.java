package de.raindancer.modules.afkcam.command;

import de.raindancer.modules.afkcam.AfkCamServices;
import de.raindancer.modules.afkcam.rules.CamRule.Verdict;
import de.raindancer.modules.afkcam.util.PermissionNodes;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/** {@code /afkcam} switches your camera; {@code on | off | status}; {@code preview} shows it right now. */
public final class AfkCamCommand implements BasicCommand {

    private static final List<String> OPTIONS = List.of("on", "off", "status", "preview");

    private final Supplier<AfkCamServices> services;

    public AfkCamCommand(Supplier<AfkCamServices> services) {
        this.services = services;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        AfkCamServices live = services.get();
        if (!(source.getSender() instanceof Player player)) {
            live.messages().send(source.getSender(), "afkcam.only-a-player");
            return;
        }
        String first = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        var wanted = live.cams().wanted();
        switch (first) {
            case "" -> live.messages().send(player, wanted.toggle(player) ? "afkcam.switched-on" : "afkcam.switched-off");
            case "on" -> {
                wanted.set(player, true);
                live.messages().send(player, "afkcam.switched-on");
            }
            case "off" -> {
                wanted.set(player, false);
                live.messages().send(player, "afkcam.switched-off");
            }
            case "status" -> live.messages().send(player, wanted.isOn(player) ? "afkcam.status-on" : "afkcam.status-off");
            case "preview" -> preview(live, player);
            default -> live.messages().send(player, "afkcam.usage");
        }
    }

    private static void preview(AfkCamServices live, Player player) {
        Verdict verdict = live.cams().preview(player);
        String key = switch (verdict) {
            case START -> null;
            case OFF -> "afkcam.preview-off";
            case UNSUPPORTED -> "afkcam.preview-unsupported";
            case SPECTATING -> "afkcam.preview-spectating";
            case HURT -> "afkcam.preview-hurt";
            default -> "afkcam.preview-not-permitted";
        };
        if (key != null) {
            live.messages().send(player, key);
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        String typed = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return args.length <= 1 ? OPTIONS.stream().filter(option -> option.startsWith(typed)).toList() : List.of();
    }

    @Override
    public String permission() {
        return PermissionNodes.USE;
    }
}
