package com.dungeonderps.resourcefulbees.config;

import com.dungeonderps.resourcefulbees.ResourcefulBees;
import com.google.gson.Gson;
import net.minecraft.entity.passive.CustomBeeEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;

import static com.dungeonderps.resourcefulbees.ResourcefulBees.LOGGER;

public class ResourcefulBeesConfig {

    public static final String MOD_OPTIONS = "Mod Options";

    public static final String ASSETS_DIR = "/assets/resourcefulbees/default_bees/";
    public static final String[] DEFAULT_BEES = new String[]{
            "Diamond.json",
            "Emerald.json",
            "Gold.json",
            "Iron.json"
    };


    public static Path BEE_PATH;

    // CONFIGS
    public static ForgeConfigSpec.BooleanValue GENERATE_DEFAULTS;
    public static ForgeConfigSpec.BooleanValue ENABLE_EASTER_EGG_BEES;
    public static ForgeConfigSpec.BooleanValue DEBUG_MODE;
    public static ForgeConfigSpec.DoubleValue HIVE_OUTPUT_MODIFIER;
    public static ForgeConfigSpec.IntValue HIVE_MAX_BEES;

    public static class CommonConfig {

        public static ForgeConfigSpec COMMON_CONFIG;

        static {
            ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

            COMMON_BUILDER.comment("Mod options").push(MOD_OPTIONS);
            COMMON_BUILDER.pop();

            GENERATE_DEFAULTS = COMMON_BUILDER.comment("Set to true if you want our default bees to populate the bees folder [true/false]")
                    .define("generateDefaults",true);
            ENABLE_EASTER_EGG_BEES = COMMON_BUILDER.comment("Set to true if you want easter egg bees to generate [true/false]")
                    .define("enableEasterEggBees", true);
            DEBUG_MODE = COMMON_BUILDER.comment("Extra logger info [true/false]")
                    .define("debugMode", false);
            HIVE_OUTPUT_MODIFIER = COMMON_BUILDER.comment("Output modifier for the haves when ready to be harvested[range 0.0 - 8.0]")
                    .defineInRange("hiveOutputModifier", 1.0,0.0,8.0);
            HIVE_MAX_BEES = COMMON_BUILDER.comment("Maximum amount of bees in the hive at any given time[range 0 - 16")
                    .defineInRange("hiveMaxBees", 4, 0, 16);

            COMMON_CONFIG = COMMON_BUILDER.build();

            setupDefaultBees();
        }
    }

    public static void setupDefaultBees() {// CONFIG FOLDER AND FILES MUST BE RUN BEFORE THIS
        // check config for hardcoded bee flag
        /*
        if (GENERATE_DEFAULTS.get()) {
        }*/
        for (String bee : DEFAULT_BEES) {
            String path = ASSETS_DIR + bee;   //This allows me to dynamically change the path and later create the new path
                try {
                    Path filePath = Paths.get(ResourcefulBees.class.getResource(path).toURI());  //This gets the existing file from the assets directory
                    Path newPath =  Paths.get(BEE_PATH.toString() + "/" + bee);  //This is necessary because the target path needs to include the file name
                    Files.copy(filePath, newPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }
        }
        addBees();
    }

    // setup the mod config folder
    public static void setup() {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path rbConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "resourcefulbees");
        // subfolder for bees
        Path rbBeesPath = Paths.get(rbConfigPath.toAbsolutePath().toString(), "bees");
        BEE_PATH = rbBeesPath;
        try {
            Files.createDirectory(rbConfigPath);
            Files.createDirectory(rbBeesPath);
        } catch (FileAlreadyExistsException e) {
            // do nothing
        } catch (IOException e) {
            LOGGER.error("failed to create resourcefulbees config directory");
        }
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.COMMON_CONFIG, "resourcefulbees/common.toml");

    }

    private static void parseBee(File file) throws IOException {
        String name = file.getName(); // find good way to cut the file name
        name = name.substring(0, name.indexOf('.'));

        Gson gson = new Gson();
        Reader r = new FileReader(file);
        BeeInfo bee = gson.fromJson(r, BeeInfo.class);
        bee.setName(name);
        CustomBeeEntity.BEE_INFO.put(name, bee);//bee.getInfo());
    }

    public static void addBees() {
        for (File f: BEE_PATH.toFile().listFiles()) {
            String s = f.getName();
            if (s.substring(s.indexOf('.')).equals(".json")) {
                try {
                    parseBee(f);
                } catch (IOException e) {
                    ResourcefulBees.LOGGER.error("File not found when parsing bees");
                }
            }
        }
    }
}
