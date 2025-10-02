package io.codetoil.dynamic_registries_transformer;

import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModProvider;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public class DynamicRegistriesTransformerTypeModProvider implements IModProvider {
    @Override
    public String name() {
        return "Dynamic Registries Transformer Type Mod Provider";
    }

    @Override
    public void scanFile(IModFile modFile, Consumer<Path> pathConsumer) {
        Path path = modFile.findResource("dynamic_registries_transformer.config");
        pathConsumer.accept(path);
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {

    }

    @Override
    public boolean isValid(IModFile modFile) {
        return true;
    }
}
