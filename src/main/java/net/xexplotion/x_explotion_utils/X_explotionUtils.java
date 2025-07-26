package net.xexplotion.x_explotion_utils;

import net.fabricmc.api.ModInitializer;

import net.xexplotion.x_explotion_utils.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class X_explotionUtils implements ModInitializer {
	public static final String MOD_ID = "x_explotion_utils";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	ConfigManager configManager = new ConfigManager();
	protected static X_explotionUtils instance;

	public static X_explotionUtils getInstance() {
		if(instance == null) return new X_explotionUtils();
		return instance;
	}


	public ConfigManager getConfigManager() {
		return configManager;
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		instance = this;

//		ConfigManager.registerConfig(X_explotionUtilsConfig.class);

		ConfigManager.initializeConfigs();


		LOGGER.info("Hello Fabric world!");
	}
}