package net.yellowmedia.easyrecipes.command;

import net.yellowmedia.easyrecipes.EasyRecipes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CreateRecipe implements CommandExecutor {

    public static final String RECIPEMENU_NAME = "EasyRecipes - Create Recipe";
    private static final int[] BORDER_SLOTS =  {0, 4, 5, 6, 13, 15, 18, 22, 23, 24};
    public static final List<Integer> CRAFTING_SLOTS = List.of(1, 2, 3, 10, 11, 12, 19, 20, 21);

    public static final int RESULT_SLOT = 14;
    public static final int INFO_SLOT = 9;
    public static final int CREATE_SLOT = 17;

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
        // Check if executor is a player
        if (!(sender instanceof Player)) {
            EasyRecipes.message(sender, "Only players are allowed to do this!");
            return false;
        }
        Player player = (Player) sender;

        // Make player open recipe menu
        player.openInventory(recipeMenu());
        return true;
    }

}
