package net.yellowmedia.easyrecipes;

import net.yellowmedia.easyrecipes.command.MasterCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class EasyRecipes extends JavaPlugin {

    public static final Logger LOGGER = Logger.getLogger("Minecraft");
    public static final String MSG_PREFIX = "&e[&aEasyRecipes&e] ";

    public static List<NamespacedKey> recipe_keys = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("easyrecipes").setExecutor(new MasterCommand(this));
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        loadRecipes();
        LOGGER.info("Enabled EasyRecipes v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        LOGGER.info("Disabled EasyRecipes v" + getDescription().getVersion());
    }

    public File getFileConfig(String name) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    private boolean loadRecipes() {
        // Load recipe file, create one if doesn't exist
        File file = getFileConfig("recipes.yml");
        if (file == null) return false;

        // Load recipe file and attempt to load recipes
        FileConfiguration recipe_config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection recipe_section = recipe_config.getConfigurationSection("recipes");
        if (recipe_section == null) return false;
        // Iterate through every recipe, skip each recipe deemed invalid by the crazy amount of sanity checks
        ingredient_loop:
        for (String section_key : recipe_section.getKeys(false)) {
            ConfigurationSection section = recipe_section.getConfigurationSection(section_key);
            if (section == null) continue;
            List<String> ingredients = section.getStringList("ingredients");
            if (ingredients.size() != 9) continue;

            // Iterate through ingredients and convert to ItemStacks, this is some freaky stuff
            List<RecipeChoice.ExactChoice> ingredients_list = new ArrayList<>();
            for (String ingredient_base64 : ingredients) {
                try {
                    ItemStack ingredient = base64ToItem(ingredient_base64);
                    if (ingredient == null) continue ingredient_loop;
                    RecipeChoice.ExactChoice ingredient_choice = new RecipeChoice.ExactChoice(ingredient);
                    ingredients_list.add(ingredient_choice);
                } catch (IOException e) {
                    continue ingredient_loop;
                }
            }

            // Convert result to ItemStack, even more freaky stuff
            String result_base64 = section.getString("result");
            if (result_base64 == null) continue;
            ItemStack result_item = null;
            try {
                result_item = base64ToItem(result_base64);
            } catch (IOException e) {
                continue;
            }
            if (result_item == null || result_item.getType().equals(Material.AIR)) continue;
            ItemMeta result_meta = result_item.getItemMeta();
            if (result_meta == null) continue;

            // Finally create the recipe
            NamespacedKey recipe_key = new NamespacedKey(this, "er_" + stripColorCode(result_meta.getDisplayName().toLowerCase()).replace(" ", "_").replaceAll("[^a-zA-Z_\\-]+", ""));
            ShapedRecipe recipe = new ShapedRecipe(recipe_key, result_item);
            recipe.shape("012", "345", "678");
            for (int j = 0; j < 9; j++) {
                recipe.setIngredient(Integer.toString(j).charAt(0), ingredients_list.get(j));
            }
            getServer().addRecipe(recipe);
            recipe_keys.add(recipe_key);
            LOGGER.info("Registered recipe " + recipe_key.getKey());
        }
        return true;
    }

    public static String stripColorCode(String s) {
        return s.replaceAll("&.|§.", "");
    }

    public static void message(CommandSender sender, String message) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG_PREFIX + message));
        } else {
            sender.sendMessage(stripColorCode(MSG_PREFIX + message));
        }
    }

    public static String itemToBase64(ItemStack item) throws IllegalStateException {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(outStream);
            dataOut.writeObject(item);
            dataOut.close();
            return Base64Coder.encodeLines(outStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save ItemStack.", e);
        }
    }

    public static ItemStack base64ToItem(String data) throws IOException {
        try {
            ByteArrayInputStream inStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataIn = new BukkitObjectInputStream(inStream);
            ItemStack item = (ItemStack) dataIn.readObject();
            dataIn.close();
            return item;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
