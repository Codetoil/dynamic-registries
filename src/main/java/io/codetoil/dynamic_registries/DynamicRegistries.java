package io.codetoil.dynamic_registries;

import com.mojang.logging.LogUtils;
import io.codetoil.dynamic_registries.api.DynamicRegistriesObjectHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLServiceProvider;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DynamicRegistries.MODID)
public class DynamicRegistries {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "dynamic_registries";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DynamicRegistriesObjectHelper.DynamicRegistriesDynamicClassLoader
            dynamicRegistriesDynamicClassLoader = new DynamicRegistriesObjectHelper.
            DynamicRegistriesDynamicClassLoader(DynamicRegistries.class.getClassLoader());

    public DynamicRegistries(FMLJavaModLoadingContext context) {
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }
}
