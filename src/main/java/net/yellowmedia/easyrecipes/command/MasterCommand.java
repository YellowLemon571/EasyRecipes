package net.yellowmedia.easyrecipes.command;

import net.yellowmedia.easyrecipes.EasyRecipes;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MasterCommand implements CommandExecutor {

    private Plugin plugin;
    private EasyRecipes easyRecipes;

    public static final String RECIPEMENU_NAME = "EasyRecipes - Create Recipe";
    private static final int[] BORDER_SLOTS =  {0, 4, 5, 6, 13, 15, 18, 22, 23, 24};
    public static final List<Integer> CRAFTING_SLOTS = List.of(1, 2, 3, 10, 11, 12, 19, 20, 21);

    public static final int RESULT_SLOT = 14;
    public static final int INFO_SLOT = 9;
    public static final int CREATE_SLOT = 17;

    public MasterCommand(Plugin plugin) {
        this.plugin = plugin;
        this.easyRecipes = (EasyRecipes) plugin;
    }

    public static Inventory recipeMenu() {
        // Create menu items
        // Border for the menu
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta pane_meta = pane.getItemMeta();
        pane_meta.setDisplayName(" ");
        pane.setItemMeta(pane_meta);
        // Info item, tells the player where to place ingredients
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta book_meta = book.getItemMeta();
        book_meta.setDisplayName("Place your ingredients in the grid,");
        book_meta.setLore(List.of(ChatColor.translateAlternateColorCodes('&', "&f&othen place the result in the box.")));
        book.setItemMeta(book_meta);
        // Create button
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emerald_meta = emerald.getItemMeta();
        emerald_meta.setDisplayName("Create Recipe");
        emerald.setItemMeta(emerald_meta);

        // Create empty inventory and add menu items
        Inventory inv = Bukkit.createInventory(null, 27, RECIPEMENU_NAME);
        // Add border items
        for (int slot : BORDER_SLOTS) {
            inv.setItem(slot, pane);
        }
        // Add info item
        inv.setItem(INFO_SLOT, book);
        // Add create button
        inv.setItem(CREATE_SLOT, emerald);
        return inv;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Help (no sub-command)
        if (args.length == 0) {
            EasyRecipes.message(sender, "placeholder");
            return true;
        }

        // Create command
        else if (args[0].equalsIgnoreCase("create")) {
            // Check if sender is a player
            if (!(sender instanceof Player)) {
                EasyRecipes.message(sender, "Only players are allowed to do this!");
                return false;
            }
            Player player = (Player) sender;

            // Make player open recipe menu
            player.openInventory(recipeMenu());
            player.playSound(player, Sound.ENTITY_ITEM_PICKUP, (float) 1.0, (float) 1.0);
            return true;
        }

        // Delete command
        else if (args[0].equalsIgnoreCase("delete")) {
            // Check for bad argument count
            if (args.length != 2) {
                EasyRecipes.message(sender, "&4Invalid arguments! Please specify &oonly &4the name of the recipe to delete.");
                return true;
            }
            // Create new NamespacedKey and compare to the list
            NamespacedKey key = new NamespacedKey(plugin, "er_" + args[1]);
            if (!EasyRecipes.recipe_keys.contains(key)) {
                EasyRecipes.message(sender, "&4This recipe does not exist!");
                return true;
            }
            // Attempt to remove the recipe from the server
            boolean remove = Bukkit.removeRecipe(key);
            if (!remove) {
                EasyRecipes.message(sender, "&4This recipe could not be removed!");
                return true;
            }
            EasyRecipes.recipe_keys.remove(key);
            // Remove the recipe's entry from the file, stupid amounts of sanity checks here
            File file = easyRecipes.getFileConfig("recipes.yml");
            if (file == null) {
                EasyRecipes.LOGGER.severe("Unable to load recipes.yml");
                return true;
            }
            FileConfiguration recipes = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection section = recipes.getConfigurationSection("recipes");
            if (section == null) {
                EasyRecipes.LOGGER.severe("recipes.yml is broken");
                return true;
            }
            section.set(key.getKey(), null);
            // Attempt to save the file
            try {
                recipes.save(file);
            } catch(IOException e) {
                e.printStackTrace();
                return true;
            }
            EasyRecipes.message(sender, "&aRecipe " + args[1] + " has been successfully deleted!");
            return true;
        }

        // List command
        else if (args[0].equalsIgnoreCase("list")) {
            StringBuilder key_list = new StringBuilder().append("&aThe following recipes are loaded into the server:\n");
            for (NamespacedKey key : EasyRecipes.recipe_keys) {
                String key_str = key.getKey().substring(3);
                key_list.append("&e- &f").append(key_str).append("\n");
            }
            EasyRecipes.message(sender, key_list.toString().trim());
            return true;
        }

        // Error message if not a sub-command
        else {
            sender.sendMessage("&4Unrecognized sub-command. Use /easyrecipes without any sub-command for help.");
            return true;
        }
    }

}
