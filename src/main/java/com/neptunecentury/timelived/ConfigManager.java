package com.neptunecentury.timelived;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/**
 * Manages the configuration loading and saving
 *
 * @param <T> The type of the config class
 */
public class ConfigManager<T extends IConfig> {
    private final String _name;
    private final Logger _logger;
    private T _cfg;

    /**
     * Constructor
     *
     * @param name The name of the config file
     */
    public ConfigManager(String name, Logger logger) {
        _name = name;
        _logger = logger;
    }

    /**
     * Loads the configuration file
     *
     * @param clazz The class object for the class type to load
     */
    public T load(Class<T> clazz) {
        // Create a new Gson instance
        var gson = new Gson();
        // Parse the config file into a JSON Object
        try {
            // Locate the file in the config folder and return it as a File object
            var configFile = FabricLoader.getInstance().getConfigDir().resolve(_name + ".json");
            // Load the file
            var reader = Files.newBufferedReader(configFile);
            // Deserialize
            _cfg = gson.fromJson(reader, clazz);
            // Close the reader
            reader.close();

            // Set defaults
            if (_cfg.setDefaults()) {
                save();
            }

        } catch (NoSuchFileException ex) {
            _logger.warn("[{}] Could not load config: {}: Using default values.", _name, ex.getMessage());
            // Create defaults
            try {
                _cfg = clazz.getDeclaredConstructor().newInstance();
                // Set defaults
                _cfg.setDefaults();
                // Save file
                save();

            } catch (Exception innerEx) {
                _logger.error("[{}] Error creating config: {}", _name, innerEx.getMessage());
                throw new RuntimeException(ex);
            }

        } catch (Exception ex) {
            _logger.error("[{}] Error loading config: {}", _name, ex.getMessage());
            throw new RuntimeException(ex);

        }

        return _cfg;

    }

    /**
     * Saves the configuration to a file
     */
    public void save() {
        if (_cfg == null) {
            return;
        }
        try {
            // Locate the file in the config folder and return it as a File object
            var configFile = FabricLoader.getInstance().getConfigDir().resolve(_name + ".json");

            var gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            var writer = Files.newBufferedWriter(configFile);
            gson.toJson(_cfg, writer);

            writer.flush();
            writer.close();

        } catch (IOException e) {
            _logger.warn("[{}] Could not save config: {}", _name, e.getMessage());

        }
    }

}