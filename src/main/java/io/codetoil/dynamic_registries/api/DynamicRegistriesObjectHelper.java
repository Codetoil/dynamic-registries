package io.codetoil.dynamic_registries.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import io.codetoil.dynamic_registries.DynamicRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamicRegistriesObjectHelper<O> {
    private final BiMap<Class<? extends O>, Class<? extends O>> classMap = HashBiMap.create();
    private final Map<Class<? extends O>, byte[]> byteArrayMap = Maps.newHashMap();
    private final Map<Class<? extends O>, Constructor<? extends O>> selectedConstructor
            = Maps.newHashMap();
    private final Function<Constructor<? extends O>, Function<O, Object>> constructorArguments;
    private final ResourceLocation id;

    public DynamicRegistriesObjectHelper(Function<Constructor<? extends O>, Function<O, Object>>
                                                 constructorArguments, ResourceLocation id) {
        this.constructorArguments = constructorArguments;
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public <A extends O> byte[] getClassByteArray(Class<A> originalClass) {
        if (byteArrayMap.containsKey(originalClass)) {
            return byteArrayMap.get(originalClass);
        }
        return generateClassByteArray(originalClass);
    }

    private <A extends O> byte[] generateClassByteArray(Class<A> originalClass) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        classWriter.visit(
                Opcodes.V21,
                Opcodes.ACC_PUBLIC,
                "dynamic_registries_dynamic/"  + id.getNamespace() + "/" + id.getPath() +
                        "/Dynamic" + originalClass.getName().replace('.', '_')
                        .replace('$', '_'),
                null,
                originalClass.getName().replace('.', '/'),
                new String[]{});

        MethodVisitor constructorVisitor = classWriter.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "(" + originalClass.descriptorString() + ")V",
                null,
                null);

        Label startConstructorParameters = new Label();
        Label endConstructorParameters = new Label();
        constructorVisitor.visitLocalVariable("constructorParameters", Object[].class.descriptorString(),
                null, startConstructorParameters, endConstructorParameters, 2);

        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructorVisitor.visitLdcInsn(Type.getType(originalClass));
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        constructorVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                "io/codetoil/dynamic_registries/DynamicRegistriesObjectHelper",
                "createConstructorParameters",
                "<A:" + originalClass.descriptorString() + ">(" + Class.class.descriptorString() + "<"
                        + originalClass.descriptorString() + ">" + originalClass.descriptorString() + ")["
                        + Object.class.descriptorString(),
                false);
        constructorVisitor.visitLabel(startConstructorParameters);
        constructorVisitor.visitVarInsn(Opcodes.ASTORE, 2);
        for (int index = 0; index < getSelectedConstructor(originalClass).getParameterCount(); index++) {
            constructorVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            constructorVisitor.visitIntInsn(Opcodes.SIPUSH, index); // Limited to Short.MAX_VALUE parameters.
            constructorVisitor.visitInsn(Opcodes.AALOAD);
            constructorVisitor.visitTypeInsn(Opcodes.CHECKCAST, getSelectedConstructor(originalClass)
                    .getParameterTypes()[index].getName().replace('.', '/'));
            switch (getSelectedConstructor(originalClass).getParameterTypes()[index].getName()) {
                case "io.codetoil.dynamicregistries.DynamicRegistriesObjectHelper$ByteWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/DynamicRegistriesObjectHelper$ByteWrapper",
                            "value",
                            "()B",
                            false);
                case "io.codetoil.dynamicregistries.DynamicRegistriesObjectHelper$ShortWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/DynamicRegistriesObjectHelper$ShortWrapper",
                            "value",
                            "()S",
                            false);
                case "io.codetoil.dynamicregistries.DynamicRegistriesObjectHelper$IntWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/DynamicRegistriesObjectHelper$IntWrapper",
                            "value",
                            "()I",
                            false);
                case "io.codetoil.dynamicregistries.DynamicRegistriesObjectHelper$LongWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/DynamicRegistriesObjectHelper$LongWrapper",
                            "value",
                            "()J",
                            false);
                case "io.codetoil.dynamicregistries.DynamicRegistriesObjectHelper$CharWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/DynamicRegistriesObjectHelper$CharWrapper",
                            "value",
                            "()C",
                            false);
                case "io.codetoil.dynamicregistries.DynamicRegistriesObjectHelper$FloatWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/DynamicRegistriesObjectHelper$FloatWrapper",
                            "value",
                            "()F",
                            false);
                case "io.codetoil.dynamicregistries.DynamicRegistriesObjectHelper$DoubleWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/DynamicRegistriesObjectHelper$DoubleWrapper",
                            "value",
                            "()D",
                            false);
                case "io.codetoil.dynamicregistries.DynamicRegistriesObjectHelper$BooleanWrapper":
                    constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "io/codetoil/dynamic_registries/DynamicRegistriesObjectHelper$BooleanWrapper",
                            "getValue",
                            "()Z",
                            false);
            }
        }
        constructorVisitor.visitLabel(endConstructorParameters);

        constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                originalClass.getName().replace('.', '/'),
                "<init>",
                "(" +
                        Arrays.stream(getSelectedConstructor(originalClass).getParameterTypes())
                                .map(Class::descriptorString).collect(Collectors.joining()) + ")V",
                false);

        constructorVisitor.visitInsn(Opcodes.RETURN);
        constructorVisitor.visitMaxs(1, 1);

        classWriter.visitEnd();
        byte[] result = classWriter.toByteArray();
        byteArrayMap.put(originalClass, result);
        return result;
    }

    public @NotNull <A extends O> Class<A> getClass(Class<A> originalClass)
            throws IllegalAccessException {
        if (classMap.containsKey(originalClass)) {
            return (Class<A>) classMap.get(originalClass);
        }

        String purpuredItemClassName = "dynamic_registries_dynamic." + id.getNamespace() + "." + id.getPath()
                + ".Dynamic" + originalClass.getName().replace('.', '_')
                .replace('$', '_');

        Class<A> purpuredItemClass;
        try {
            purpuredItemClass = (Class<A>) DynamicRegistries.dynamicRegistriesDynamicClassLoader
                    .loadClass(purpuredItemClassName);
        } catch (ClassNotFoundException e) {
            purpuredItemClass = (Class<A>) DynamicRegistries.dynamicRegistriesDynamicClassLoader
                    .defineClass(purpuredItemClassName,
                    getClassByteArray(originalClass));
        }

        classMap.put(originalClass, purpuredItemClass);

        return purpuredItemClass;
    }

    @SuppressWarnings("unused") // Used by Generated Classes
    public <A extends O> Object[] createConstructorParameters(Class<A> originalClass, A original) {
        Constructor<A> constructor = getSelectedConstructor(originalClass);
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
            } else if (this.constructorArguments.apply(constructor) != null) {
                result[index] = this.constructorArguments.apply(constructor).apply(original);
            } else {
                result[index] = null;
            }
        }
        return result;
    }

    public <A extends O> Constructor<A> getSelectedConstructor(Class<A> originalClass) {
        if (selectedConstructor.containsKey(originalClass)) {
            return (Constructor<A>) selectedConstructor.get(originalClass);
        }

        return selectConstructor(originalClass);
    }

    private <A extends O> Constructor<A> selectConstructor(Class<A> originalClass) {
        Constructor<A>[] constructors = (Constructor<A>[]) originalClass.getDeclaredConstructors();
        if (constructors.length == 0)
            throw new IllegalArgumentException("Class " + originalClass + " does not contain any constructors " +
                    "(should not be possible, is someone messing with bytecode?)");
        int sizeOfMinimumSizedConstructor = Integer.MAX_VALUE;
        int indexOfMinimumSizedConstructor = 0;
        for (int index = 0; index < constructors.length; index++) {
            Constructor<A> constructor = constructors[index];
            /*
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == Item.Properties.class) {
                return constructor;
            }
            if (parameterTypes.length == 2 && parameterTypes[0] == Block.class
                    && parameterTypes[1] == Item.Properties.class) {
                return constructor;
            }
            */
            if (this.constructorArguments.apply(constructor) != null) {
                return constructor;
            }
            if (constructor.getParameterCount() < sizeOfMinimumSizedConstructor) {
                indexOfMinimumSizedConstructor = index;
                sizeOfMinimumSizedConstructor = constructor.getParameterCount();
            }
        }

        Constructor<A> constructor = constructors[indexOfMinimumSizedConstructor];
        selectedConstructor.put(originalClass, constructor);
        return constructor;
    }

    public <A extends O> boolean isObject(Class<A> aClass) {
        return classMap.containsValue(aClass);
    }

    public static final class DynamicRegistriesDynamicClassLoader extends ClassLoader {
        public DynamicRegistriesDynamicClassLoader(ClassLoader parent) {
            super( "DynamicRegistriesDynamicClassLoader of " + parent.getName() + "@" +
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
