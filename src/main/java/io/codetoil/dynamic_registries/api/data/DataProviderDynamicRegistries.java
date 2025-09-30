package io.codetoil.dynamic_registries.api.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import com.mojang.serialization.Codec;
import io.codetoil.dynamic_registries.DynamicRegistries;
import io.codetoil.dynamic_registries.api.DynamicRegistriesObjectHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public record DataProviderDynamicRegistries<O>(PackOutput output,
                                               CompletableFuture<HolderLookup.Provider> registries,
                                               BiMap<O, O> objectMap,
                                               DynamicRegistriesObjectHelper<O> dynamicRegistriesObjectHelper)
        implements DataProvider {

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        Path rootPath = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve(
                dynamicRegistriesObjectHelper.getId().toDebugFileName() + "/");

        return this.registries.thenCompose(provider -> {
            DynamicRegistries.LOGGER.info("Starting the generation for {}", dynamicRegistriesObjectHelper.getId());
            Map<O, byte[]> data = Maps.transformValues(
                    Maps.transformValues(
                            this.objectMap,
                            (O o) -> {
                                assert o != null;
                                return (Class<O>) o.getClass();
                            }),
                    dynamicRegistriesObjectHelper::getClassByteArray);
            DynamicRegistries.LOGGER.info("Finished generating {}", dynamicRegistriesObjectHelper.getId());

            return DataProvider.saveAll(cachedOutput, Codec.list(Codec.BYTE), (object) ->
                            rootPath.resolve(object.toString()),
                    Maps.transformValues(data, Bytes::asList));
        });
    }

    @Override
    public @NotNull String getName() {
        return "Dynamic Registry' Data Provider for " + dynamicRegistriesObjectHelper.getId();
    }

    @Override
    public String toString() {
        return getName() + ":" + this.hashCode();
    }


}
