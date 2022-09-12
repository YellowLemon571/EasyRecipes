package net.yellowmedia.easyrecipes.listener;

import net.yellowmedia.easyrecipes.EasyRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class CraftingListener implements Listener {

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof ShapedRecipe)) return;
        ShapedRecipe recipe_shaped = (ShapedRecipe) recipe;
        NamespacedKey key = recipe_shaped.getKey();
        String keyName = key.getKey().substring(3);
        if (EasyRecipes.recipe_keys.contains(key)) {
            if (!player.hasPermission("easyrecipes.craft." + keyName) && !player.hasPermission("easyrecipes.craft.*")) {
                EasyRecipes.message(player, "&4You do not have permission to craft this item!");
                event.setCancelled(true);
            }
        }
    }

}
