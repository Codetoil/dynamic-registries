package io.codetoil.dynamic_registries.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.codetoil.dynamic_registries.DynamicRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamicRegistriesObjectHelper<P> {
    private final BiMap<Class<? extends P>, Class<? extends P>> classMap = HashBiMap.create();
    private final Map<Class<? extends P>, byte[]> byteArrayMap = Maps.newHashMap();
    private final Map<Class<? extends P>, Constructor<? extends P>> selectedConstructor
            = Maps.newHashMap();
    private final BiFunction<Constructor<? extends P>, Integer, Function<P, ?>> constructorArguments;
    private final ResourceLocation id;

    public DynamicRegistriesObjectHelper(BiFunction<Constructor<? extends P>, Integer, Function<P, ?>>
                                                 constructorArguments, ResourceLocation id) {
        this.constructorArguments = constructorArguments;
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public <A extends P> byte[] getClassByteArray(Class<A> originalClass) {
        if (byteArrayMap.containsKey(originalClass)) {
            return byteArrayMap.get(originalClass);
        }
        return generateClassByteArray(originalClass);
    }

    private <C extends P> byte[] generateClassByteArray(Class<C> childClass) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        classWriter.visit(
                Opcodes.V21,
                Opcodes.ACC_PUBLIC,
                "dynamic_registries_dynamic/" + id.getNamespace() + "/" + id.getPath() +
                        "/Dynamic" + childClass.getName().replace('.', '_')
                        .replace('$', '_'),
                null,
                Type.getInternalName(childClass),
                new String[]{});

        MethodVisitor constructorVisitor = classWriter.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "(" + childClass.descriptorString()
                        + DynamicRegistriesObjectHelper.class.descriptorString() + ")V",
                null,
                null);

        Label startConstructorParameters = new Label();
        Label endConstructorParameters = new Label();
        constructorVisitor.visitLocalVariable("constructorParameters", Object[].class.descriptorString(),
                null, startConstructorParameters, endConstructorParameters, 3);

        constructorVisitor.visitCode();

        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);

        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 2);
        constructorVisitor.visitLdcInsn(Type.getType(childClass));
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        constructorVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "io/codetoil/dynamic_registries/api/DynamicRegistriesObjectHelper",
                "createConstructorParameters",
                "(Ljava/lang/Class;Ljava/lang/Object;)[Ljava/lang/Object;",
                false);
        constructorVisitor.visitLabel(startConstructorParameters);
        constructorVisitor.visitVarInsn(Opcodes.ASTORE, 3);

        for (int index = 0; index < getSelectedConstructor(childClass).getParameterCount(); index++) {
            constructorVisitor.visitVarInsn(Opcodes.ALOAD, 3);
            constructorVisitor.visitIntInsn(Opcodes.SIPUSH, index); // Limited to Short.MAX_VALUE parameters.
            constructorVisitor.visitInsn(Opcodes.AALOAD);
            constructorVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(getSelectedConstructor(childClass)
                    .getParameterTypes()[index]));
            switch (getSelectedConstructor(childClass).getParameterTypes()[index].getName()) {
                case "io.codetoil.dynamic_registries.api.api.DynamicRegistriesObjectHelper$ByteWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/api/DynamicRegistriesObjectHelper$ByteWrapper",
                            "value",
                            "()B",
                            false);
                case "io.codetoil.dynamic_registries.api.DynamicRegistriesObjectHelper$ShortWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/api/DynamicRegistriesObjectHelper$ShortWrapper",
                            "value",
                            "()S",
                            false);
                case "io.codetoil.dynamic_registries.api.DynamicRegistriesObjectHelper$IntWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/api/DynamicRegistriesObjectHelper$IntWrapper",
                            "value",
                            "()I",
                            false);
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/api/DynamicRegistriesObjectHelper$LongWrapper",
                            "value",
                            "()J",
                            false);
                case "io.codetoil.dynamic_registries.api.DynamicRegistriesObjectHelper$CharWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/api/DynamicRegistriesObjectHelper$CharWrapper",
                            "value",
                            "()C",
                            false);
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/api/DynamicRegistriesObjectHelper$FloatWrapper",
                            "value",
                            "()F",
                            false);
                case "io.codetoil.dynamic_registries.api.DynamicRegistriesObjectHelper$DoubleWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/api/DynamicRegistriesObjectHelper$DoubleWrapper",
                            "value",
                            "()D",
                            false);
                case "io.codetoil.dynamic_registries.api.DynamicRegistriesObjectHelper$BooleanWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/api/DynamicRegistriesObjectHelper$BooleanWrapper",
                            "getValue",
                            "()Z",
                            false);
            }
        }
        constructorVisitor.visitLabel(endConstructorParameters);

        constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getInternalName(childClass),
                "<init>",
                "(" +
                        Arrays.stream(getSelectedConstructor(childClass).getParameterTypes())
                                .map(Class::descriptorString).collect(Collectors.joining()) + ")V",
                false);

        constructorVisitor.visitInsn(Opcodes.RETURN);
        constructorVisitor.visitMaxs(10 + getSelectedConstructor(childClass).getParameterCount(), 3);

        classWriter.visitEnd();
        byte[] result = classWriter.toByteArray();
        byteArrayMap.put(childClass, result);
        return result;
    }

    @SuppressWarnings("unused")
    public @NotNull <A extends P> Class<A> getClass(Class<A> childClass) {
        if (classMap.containsKey(childClass)) {
            return (Class<A>) classMap.get(childClass);
        }

        String className = "dynamic_registries_dynamic." + id.getNamespace() + "." + id.getPath()
                + ".Dynamic" + childClass.getName().replace('.', '_')
                .replace('$', '_');

        Class<A> objectClass;
        try {
            objectClass = (Class<A>) DynamicRegistries.dynamicRegistriesDynamicClassLoader
                    .loadClass(className);
        } catch (ClassNotFoundException e) {
            objectClass = (Class<A>) DynamicRegistries.dynamicRegistriesDynamicClassLoader
                    .defineClass(className,
                            getClassByteArray(childClass));
        }

        classMap.put(childClass, objectClass);

        return objectClass;
    }

    @SuppressWarnings("unused") // Used by Generated Classes
    public <C extends P> Object[] createConstructorParameters(Class<C> childClass, C child) {
        Constructor<C> constructor = getSelectedConstructor(childClass);
        Object[] result = new Object[constructor.getParameterCount()];
        for (int index = 0; index < result.length; index++) {
            if (constructor.getParameterTypes()[index] == byte.class) {
                result[index] = new ByteWrapper((byte) 0);
            } else if (constructor.getParameterTypes()[index] == short.class) {
                result[index] = new ShortWrapper((short) 0);
            } else if (constructor.getParameterTypes()[index] == int.class) {
                result[index] = new IntWrapper(0);
            } else if (constructor.getParameterTypes()[index] == long.class) {
                result[index] = new LongWrapper(0L);
            } else if (constructor.getParameterTypes()[index] == char.class) {
                result[index] = new CharWrapper((char) 0);
            } else if (constructor.getParameterTypes()[index] == float.class) {
                result[index] = new FloatWrapper(0.0f);
            } else if (constructor.getParameterTypes()[index] == double.class) {
                result[index] = new DoubleWrapper(0.0);
            } else if (constructor.getParameterTypes()[index] == boolean.class) {
                result[index] = new BooleanWrapper(false);
            } else if (this.constructorArguments.apply(constructor, index) != null) {
                result[index] = this.constructorArguments.apply(constructor, index).apply(child);
            } else {
                result[index] = null;
            }
        }
        return result;
    }

    public <C extends P> Constructor<C> getSelectedConstructor(Class<C> childClass) {
        if (selectedConstructor.containsKey(childClass)) {
            return (Constructor<C>) selectedConstructor.get(childClass);
        }

        return selectConstructor(childClass);
    }

    private <C extends P> Constructor<C> selectConstructor(Class<C> childClass) {
        Constructor<C>[] constructors = (Constructor<C>[]) childClass.getDeclaredConstructors();
        if (constructors.length == 0)
            throw new IllegalArgumentException("Class " + childClass + " does not contain any constructors " +
                    "(should not be possible, is someone messing with bytecode?)");
        int sizeOfMinimumSizedConstructor = Integer.MAX_VALUE;
        int indexOfMinimumSizedConstructor = 0;
        for (int index = 0; index < constructors.length; index++) {
            Constructor<C> constructor = constructors[index];
            if (this.constructorArguments.apply(constructor, index) != null) {
                return constructor;
            }
            if (constructor.getParameterCount() < sizeOfMinimumSizedConstructor) {
                indexOfMinimumSizedConstructor = index;
                sizeOfMinimumSizedConstructor = constructor.getParameterCount();
            }
        }

        Constructor<C> constructor = constructors[indexOfMinimumSizedConstructor];
        selectedConstructor.put(childClass, constructor);
        return constructor;
    }

    @SuppressWarnings("unused")
    public <C extends P> boolean isObject(Class<C> classToCheck) {
        return classMap.containsValue(classToCheck);
    }

    public static final class DynamicRegistriesDynamicClassLoader extends ClassLoader {
        public DynamicRegistriesDynamicClassLoader(ClassLoader parent) {
            super("DynamicRegistriesDynamicClassLoader of " + parent.getName() + "@" +
                    Integer.toHexString(parent.hashCode()), parent);
        }

        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

    @ApiStatus.Internal
    private record ByteWrapper(byte value) {
    }

    @ApiStatus.Internal
    private record ShortWrapper(short value) {
    }

    @ApiStatus.Internal
    private record IntWrapper(int value) {
    }

    @ApiStatus.Internal
    private record LongWrapper(long value) {
    }

    @ApiStatus.Internal
    private record CharWrapper(char value) {
    }

    @ApiStatus.Internal
    private record FloatWrapper(float value) {
    }

    @ApiStatus.Internal
    private record DoubleWrapper(double value) {
    }

    @ApiStatus.Internal
    private record BooleanWrapper(boolean value) {
    }
}
