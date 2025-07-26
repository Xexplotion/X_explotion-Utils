package net.xexplotion.x_explotion_utils.config;

import com.google.common.base.Defaults;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.xexplotion.x_explotion_utils.X_explotionUtils;
import net.xexplotion.x_explotion_utils.annotation.Config;
import net.xexplotion.x_explotion_utils.annotation.ConfigValue;

import java.awt.*;
import java.io.*;
import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ConfigManager {
    private List<ModConfig> configs = new ArrayList<>();
    public static File modFolder = new File(FabricLoader.getInstance().getConfigDir().toFile(), X_explotionUtils.MOD_ID);
    public static Path configDir = modFolder.toPath();

    public static <T extends ModConfig> T getConfig(Class<T> target) {
        for (ModConfig obj : X_explotionUtils.getInstance().getConfigManager().configs) {
            if (target.equals(obj.getClass())) {
                return target.cast(obj);
            }
        }
        return null;
    }

    private void initConfigInternal() {
        modFolder.mkdirs(); // ensure folder exists
        this.configs.forEach(config -> {
            String configName = getConfigName(config);
            X_explotionUtils.LOGGER.info("Initialized " + configName + " config!");

            Path configFilePath = configDir.resolve(configName  + ".json5");
            if(Files.notExists(configFilePath)) {
                try {
                    Files.createFile(configFilePath);
                    save(config, configFilePath);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                //create config
            }
            //read config
            try {
                setConfigFields(config, configFilePath);
            } catch (IllegalAccessException | FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void setConfigFields(ModConfig config, Path configFilePath) throws IllegalAccessException, FileNotFoundException {
        Class<? extends ModConfig> clazz = config.getClass();

        Gson gson = new Gson();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Class<?> type = field.getType();

            if (field.isAnnotationPresent(ConfigValue.class)) {
                ConfigValue configValue = field.getAnnotation(ConfigValue.class);

                field.setAccessible(true);

                JsonObject json = JsonParser.parseReader(new FileReader(configFilePath.toFile())).getAsJsonObject();

                if (json.has(configValue.name())) field.set(config,
                        gson.fromJson(json.get(configValue.name()), type)
                );

                field.setAccessible(false);
            }
        }
    }

    private String getConfigName(ModConfig config) {
        Class<? extends ModConfig> clazz = config.getClass();

        return clazz.getAnnotation(Config.class).value();
    }


    private void save(ModConfig config, Path configFilePath) throws IOException, NoSuchMethodException, IllegalAccessException {

        Class<? extends ModConfig> clazz = config.getClass();

        Gson gson = new Gson();

        StringBuilder json = new StringBuilder();
        json.append("{\n");

        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Class<?> type = field.getType();

            ConfigValue value = field.getAnnotation(ConfigValue.class);

            if(value == null) continue;

            if(!Objects.equals(value.comment(), "")) json.append("  // ").append(value.comment()).append("\n");

            json.append("  \"").append(value.name()).append("\": ");
            field.setAccessible(true);
            json.append(field.get(config) != null ? gson.toJson(field.get(config))  : Defaults.defaultValue(type));

            if (i < fields.length - 1) {
                json.append(",");
            }
            json.append("\n");
            field.setAccessible(false);
        }

        json.append("}");

        try (FileWriter writer = new FileWriter(configFilePath.toFile())) {
            writer.write(json.toString());
        }
    }


    public static void initializeConfigs() {

        X_explotionUtils.getInstance().getConfigManager().initConfigInternal();

    }

    public static void registerConfig(Class<? extends ModConfig> config) {
        validateClass(config);

        if (config.isAnnotationPresent(Config.class)) {
            Config name = config.getAnnotation(Config.class);
            System.out.println("Registered config with name: " + name.value());
            try {
                X_explotionUtils.getInstance().getConfigManager().configs.add(config.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {
            throw new AnnotationFormatError("No config name found for class: " + config);
        }
    }

    public static void validateClass(Class<?> clazz) {
        boolean isConfigClass = clazz.isAnnotationPresent(Config.class);

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class) && !isConfigClass) {
                throw new IllegalStateException(
                        "Field '" + field.getName() + "' in class '" + clazz.getName() +
                                "' is annotated with @ConfigValue but the class is not annotated with @Config"
                );
            }
        }
    }
}
