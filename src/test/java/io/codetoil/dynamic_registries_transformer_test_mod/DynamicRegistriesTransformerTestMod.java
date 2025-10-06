package io.codetoil.dynamic_registries_transformer_test_mod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DynamicRegistriesTransformerTestMod.MODID)
public final class DynamicRegistriesTransformerTestMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "dynamic_registries_transformer_test_mod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public DynamicRegistriesTransformerTestMod(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        // Register the commonSetup method for modloading
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
        FMLClientSetupEvent.getBus(modBusGroup).addListener(this::onClientSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    public void onClientSetup(FMLClientSetupEvent event) {
    }
}
