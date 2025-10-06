package io.codetoil.dynamic_registries_transformer;

import com.google.common.collect.Streams;
import net.minecraftforge.forgespi.locating.IDependencyLocator;
import net.minecraftforge.forgespi.locating.IModFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class DynamicRegistriesTransformerTypeLocator implements IDependencyLocator {

    private static final String CONFIG_NAME = "dynamic_registries_transformer_config.json";

    @Override
    public String name() {
        return "Dynamic Registries Transformer Type Locator";
    }

    @Override
    public void scanFile(IModFile modFile, Consumer<Path> pathConsumer) {
        Path path = modFile.findResource(CONFIG_NAME);
        pathConsumer.accept(path);
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {

    }

    @Override
    public boolean isValid(IModFile modFile) {
        return true;
    }

    @Override
    public List<IModFile> scanMods(Iterable<IModFile> loadedMods) {

        return Streams.stream(loadedMods).filter(Objects::nonNull)
                .filter(modFile -> Files.exists(modFile.findResource(CONFIG_NAME)))
                .toList();
    }
}
