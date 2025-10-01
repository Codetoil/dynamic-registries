package io.codetoil.dynamic_registries_transformer;

import com.google.common.collect.Lists;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public record DynamicRegistriesTransformerTransformer<T>(Predicate<String> isTopLevelParent) implements ITransformer<T> {

    @Override
    public @NotNull T transform(T input, ITransformerVotingContext context) {
        return input;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target> targets() {
        new Predicate<String>() {
            public boolean test(String name) {
                Collection<String> superNames = Lists.newArrayList(name);
                String superName;
                try {
                    do {
                        ClassReader classReader = new ClassReader(name);
                        superName = classReader.getSuperName();
                    } while (superName != null && !superNames.contains(superName) &&
                            isTopLevelParent.test(superName));
                    return isTopLevelParent.test(superName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };


        return Set.of();
    }

    @Override
    public String[] labels() {
        return new String[]{"Dynamic Registries Core"};
    }
}
