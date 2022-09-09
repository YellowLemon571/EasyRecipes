package net.yellowmedia.easyrecipes.tabcomplete;

import net.yellowmedia.easyrecipes.EasyRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MasterTabCompleter implements TabCompleter {

    private final List<String> SUBCOMMANDS = List.of("create", "delete", "list");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length > 1 && args[0].equalsIgnoreCase("delete")) {
            List<String> keys = new ArrayList<>();
            for (NamespacedKey key : EasyRecipes.recipe_keys) {
                keys.add(key.getKey().substring(3));
            }
            StringUtil.copyPartialMatches(args[1], keys, completions);
        } else {
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
        }
        Collections.sort(completions);
        return completions;
    }

}
