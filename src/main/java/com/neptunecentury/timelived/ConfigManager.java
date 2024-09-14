package com.neptunecentury.timelived;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.spongepowered.include.com.google.gson.Gson;

import java.io.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;

/**
 * Manages the configuration loading and saving
 *
 * @param <T> The type of the config class
 */
public class ConfigManager<T> {
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
     * Gets the configuration
     *
     * @return An object containing the configuration
     */
    public T getConfig() {
        return _cfg;
    }

    /**
     * Loads the configuration file
     *
     * @param clazz The class object for the class type to load
     */
    public void load(Class<T> clazz) {
        // Create a new Gson instance
        var gson = new Gson();
        // Parse the config file into a JSON Object
        try {
            // Locate the file in the config folder and return it as a File object
            var configFile = FabricLoader.getInstance().getConfigDir().resolve(_name + ".json");
            // Load the file
            var jsonString = Files.newBufferedReader(configFile);
            // Deserialize
            _cfg = gson.fromJson(jsonString, clazz);

        } catch (FileNotFoundException e) {
            _logger.warn("[{}] Could not load config: {}: Using default values.", _name, e.getMessage());
            // Create defaults
            try {
                _cfg = clazz.getDeclaredConstructor().newInstance();
                // Save file
                save();

            } catch (Exception ex) {
                _logger.error("[{}] Error creating config: {}", _name, ex.getMessage());
                throw new RuntimeException(ex);
            }

        } catch (Exception ex) {
            _logger.error("[{}] Error loading config: {}", _name, ex.getMessage());
            throw new RuntimeException(ex);

        }

    }

    /**
     * Saves the configuration to a file
     */
    public void save() {
        if (_cfg == null) {
            return;
        }

        // Locate the file in the config folder and return it as a File object
        File configFile = FabricLoader.getInstance().getConfigDir().resolve(_name + ".json5").toFile();

        var jankson = Jankson.builder().build();
        var result = jankson
                .toJson(_cfg)                       // The first call makes a JsonObject
                .toJson(true, true); // The second turns the JsonObject into a String

        try {
            // Check if the file exists or if we successfully created new one
            var fileIsUsable = configFile.exists() || configFile.createNewFile();
            if (!fileIsUsable) throw new FileNotFoundException();

            // Create output stream from our file.
            var out = new FileOutputStream(configFile, false);

            // Write out the file and close it.
            out.write(result.getBytes());
            out.flush();
            out.close();

        } catch (IOException e) {
            _logger.warn("[{}] Could not save config: {}", _name, e.getMessage());

        }
    }

}











public class ConfigManager {
    private Config _cfg;
    private String _name;
    public String wowMessage = "Wow! You lived for %s day(s). That is quite an accomplishment!";
    public String congratsMessage = "Congrats, you lived for %s day(s).";
    public String tryAgainMessage = "You lived for %s day(s). How about we try that again, shall we?";
    public String tryHarderMessage = "You lived for %s day(s). Let's see if we can last a bit longer next time.";
    public String maybeNextTimeMessage ="You lived for %s day(s). Uh... maybe next time will be better.";
    public String timeTravelMessage ="Wait... did you travel back in time?";
    public String newRecordMessage ="All right! New record! You surpassed your previous record of %s day(s)!";
    public String queryPlayerMessage = "%s has lived for %s day(s). Previous record is %s day(s).";
    public String statsNotFoundMessage = "Statistics not found for %s.";

    public Config load(){
// Create a new Jankson instance
        var jankson = Jankson.builder().build();
        // Parse the config file into a JSON Object
        try {
            // Locate the file in the config folder and return it as a File object
            File configFile = FabricLoader.getInstance().getConfigDir().resolve(_name + ".json5").toFile();

            // Load the file
            JsonObject configJson = jankson.load(configFile);

            // Convert the raw object into your POJO type
            T config = jankson.fromJson(configJson, clazz);

            _logger.info("[{}] Loaded configuration", _name);

            _cfg = config;

        } catch (FileNotFoundException e) {
            // Create defaults
            try {
                _cfg = new Config();
                // Save file
                //save();

            } catch (Exception ex) {
                TimeLived.logger.error("[{}] Error creating config: {}", _name, ex.getMessage());
                throw new RuntimeException(ex);
            }

        } catch (Exception ex) {
            TimeLived.logger.error("[{}] Error loading config: {}", _name, ex.getMessage());
            throw new RuntimeException(ex);

        }
    }

}
