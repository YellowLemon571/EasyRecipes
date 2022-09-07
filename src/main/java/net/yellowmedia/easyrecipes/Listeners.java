package net.yellowmedia.easyrecipes;

import net.yellowmedia.easyrecipes.command.CreateRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Listeners implements Listener {

    private EasyRecipes easyRecipes;
    private Plugin plugin;

    public Listeners(EasyRecipes easyRecipes) {
        this.easyRecipes = easyRecipes;
        this.plugin = easyRecipes;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Get data from event
        Player player = (Player) event.getWhoClicked();
        InventoryView view = event.getView();
        Inventory inv = event.getInventory();

        // Data checks
        if (!view.getTitle().equals(CreateRecipe.RECIPEMENU_NAME)) return;
        if (CreateRecipe.CRAFTING_SLOTS.contains(event.getRawSlot()) || event.getRawSlot() == CreateRecipe.RESULT_SLOT || event.getRawSlot() > 26) return;

        event.setCancelled(true);

        // Create button
        if (event.getSlot() == CreateRecipe.CREATE_SLOT) {
            boolean ingredientsEmpty = true, resultEmpty = true;
            // Scan the ingredient table and give ingredients back to player
            List<ItemStack> ingredients_list = new ArrayList<>();
            for (int i : CreateRecipe.CRAFTING_SLOTS) {
                ItemStack item = inv.getItem(i);
                if (item != null && !item.getType().equals(Material.AIR)) {
                    ingredients_list.add(item);
                    ingredientsEmpty = false;
                } else {
                    ItemStack item_air = new ItemStack(Material.AIR);
                    ingredients_list.add(item_air);
                }
            }
            for (ItemStack item_entry : ingredients_list) {
                if (!item_entry.getType().equals(Material.AIR)) {
                    player.getInventory().addItem(item_entry);
                }
            }
            // Scan the result and give result back to player
            ItemStack result = inv.getItem(CreateRecipe.RESULT_SLOT);
            if (result != null && !result.getType().equals(Material.AIR)) {
                resultEmpty = false;
                player.getInventory().addItem(result);
            }
            view.close();

            // Check if recipe is correct then process it, this is some freaky stuff
            if (!ingredientsEmpty && !resultEmpty) {
                // Load recipe file, create new config stuff if empty
                File file = easyRecipes.getFileConfig("recipes.yml");
                if (file == null) {
                    EasyRecipes.LOGGER.warning("File is null");
                    return;
                }
                FileConfiguration recipes = YamlConfiguration.loadConfiguration(file);
                ConfigurationSection recipes_section = recipes.getConfigurationSection("recipes");
                if (recipes_section == null) {
                    recipes_section = recipes.createSection("recipes");
                }
                int recipes_length = recipes_section.getInt("length");

                // Increase index and create section for this recipe
                recipes_section.set("length", recipes_length + 1);
                ConfigurationSection recipe = recipes_section.createSection(Integer.toString(recipes_length));

                // Convert ingredients to base64 and save to file
                List<String> ingredients_list_base64 = new ArrayList<>();
                for (ItemStack item_entry : ingredients_list) {
                    try {
                        String item_base64 = EasyRecipes.itemToBase64(item_entry);
                        ingredients_list_base64.add(item_base64);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                if (ingredients_list_base64.size() != 9) {
                    EasyRecipes.LOGGER.warning("Base64 ingredient list is not 9 long");
                    return;
                }
                recipe.set("ingredients", ingredients_list_base64);

                // Convert result to base64 and save to file
                String result_base64;
                try {
                    result_base64 = EasyRecipes.itemToBase64(result);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return;
                }
                recipe.set("result", result_base64);

                // Apply the recipe to the server
                ItemMeta result_meta = result.getItemMeta();
                if (result_meta == null) {
                    EasyRecipes.LOGGER.warning("Result metadata is null");
                    return;
                }
                NamespacedKey key = new NamespacedKey(plugin, "er_" + result_meta.getDisplayName().toLowerCase().replaceAll("[^a-zA-Z]+", ""));
                ShapedRecipe shape = new ShapedRecipe(key, result);
                shape.shape("012", "345", "678");
                for (int i = 0; i < 9; i++) {
                    shape.setIngredient(Integer.toString(i).charAt(0), new RecipeChoice.ExactChoice(ingredients_list.get(i)));
                }
                boolean recipeAdded = plugin.getServer().addRecipe(shape);
                if (recipeAdded) {
                    EasyRecipes.message(player, "&aDone! Try the recipe.");
                } else {
                    EasyRecipes.message(player, "&4Something went wrong.");
                }

                // Finally save the file
                try {
                    recipes.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                EasyRecipes.message(player, "&4Either the ingredient table or the result is empty!");
            }
        }
    }

}