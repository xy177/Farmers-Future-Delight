package xy177.farmersfuturedelight.core;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class FFDAlfheimCompatTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("FFD Height Core");
    private static final String HOOKS =
            "xy177/farmersfuturedelight/core/FFDHeightHooks";
    private static final String WORLD_MIXIN = "dev.redstudio.alfheim.mixin.WorldMixin";
    private static final String CHUNK_MIXIN = "dev.redstudio.alfheim.mixin.ChunkMixin";
    private static final String CHUNK_CACHE_MIXIN =
            "dev.redstudio.alfheim.mixin.ChunkCacheMixin";
    private static final String STORAGE =
            "net/minecraft/world/chunk/storage/ExtendedBlockStorage";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        String className = transformedName == null ? name : transformedName;
        byte[] transformedClass = basicClass;
        if (className != null && className.startsWith("dev.redstudio.alfheim.mixin.")) {
            transformedClass = stripWrongSideMembers(basicClass);
        }
        try {
            if (WORLD_MIXIN.equals(className)) {
                return transformWorldMixin(transformedClass);
            }
            if (CHUNK_MIXIN.equals(className)) {
                return transformChunkMixin(transformedClass);
            }
            if (CHUNK_CACHE_MIXIN.equals(className)) {
                return transformChunkCacheMixin(transformedClass);
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("Skipping optional height compatibility for {}", className, exception);
        }
        return transformedClass;
    }

    private static byte[] transformWorldMixin(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("dev/redstudio/alfheim/mixin/WorldMixin".equals(node.name),
                "Unexpected Alfheim WorldMixin class");

        prependExtendedWorldBranch(findMethod(node, "func_180500_c", "func_180500_c",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z"),
                loadMixinSelfAsWorld(), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/World"),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/World",
                                "ffd$checkLightFor",
                                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z",
                                false),
                        new InsnNode(Opcodes.IRETURN)));
        prependExtendedWorldBranch(findMethod(node, "func_175721_c", "func_175721_c",
                "(Lnet/minecraft/util/math/BlockPos;Z)I"), loadMixinSelfAsWorld(), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/World"),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ILOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getWorldLight",
                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)I",
                                false),
                        new InsnNode(Opcodes.IRETURN)));
        MethodNode neighborLight = findMethodOptional(node, "func_175705_a", "func_175705_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I");
        if (neighborLight != null) {
            prependExtendedWorldBranch(neighborLight, loadMixinSelfAsWorld(), list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                            "net/minecraft/world/World"),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                            "getWorldLightFromNeighborsFor",
                            "(Lnet/minecraft/world/World;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                            false),
                    new InsnNode(Opcodes.IRETURN)));
        }
        LOGGER.info("Patched Alfheim 1.6 WorldMixin for signed extended-height lighting");
        return write(node);
    }

    private static byte[] transformChunkMixin(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("dev/redstudio/alfheim/mixin/ChunkMixin".equals(node.name),
                "Unexpected Alfheim ChunkMixin class");
        InsnList world = loadAlfheimChunkWorld(node.name);

        prependExtendedWorldBranch(findMethodByName(node, "onGetLightSubtracted"),
                copy(world), list(new InsnNode(Opcodes.RETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "onLoad"),
                copy(world), list(new InsnNode(Opcodes.RETURN)));
        MethodNode setLightRedirect = findMethodByName(node,
                "setLightForRedirectGenerateSkylightMap");
        setMixinAnnotationInt(setLightRedirect,
                "Lorg/spongepowered/asm/mixin/injection/Redirect;", "require", 0);
        prependExtendedWorldBranch(setLightRedirect, copy(world),
                list(new InsnNode(Opcodes.RETURN)));

        prependExtendedWorldBranch(findMethod(node, "func_76615_h", "func_76615_h", "(III)V"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new VarInsnNode(Opcodes.ILOAD, 1),
                        new VarInsnNode(Opcodes.ILOAD, 2),
                        new VarInsnNode(Opcodes.ILOAD, 3),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "relightBlock",
                                "(Lnet/minecraft/world/chunk/Chunk;III)V", false),
                        new InsnNode(Opcodes.RETURN)));
        prependExtendedWorldBranch(findMethod(node, "func_177413_a", "func_177413_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getLightFor",
                                "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                                false),
                        new InsnNode(Opcodes.IRETURN)));
        prependExtendedWorldBranch(findMethod(node, "func_150809_p", "func_150809_p", "()V"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/chunk/Chunk",
                                "ffd$checkLight", "()V", false),
                        new InsnNode(Opcodes.RETURN)));
        prependExtendedWorldBranch(findMethod(node, "func_150803_c", "func_150803_c", "(Z)V"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new VarInsnNode(Opcodes.ILOAD, 1),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/chunk/Chunk",
                                "ffd$recheckGaps", "(Z)V", false),
                        new InsnNode(Opcodes.RETURN)));

        prependExtendedWorldBranch(findMethodByName(node, "setBlockStateCreateSectionVanilla"),
                copy(world), list(
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.NEW, STORAGE),
                        new InsnNode(Opcodes.DUP),
                        new VarInsnNode(Opcodes.ILOAD, 1),
                        new VarInsnNode(Opcodes.ILOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESPECIAL, STORAGE, "<init>", "(IZ)V", false),
                        new InsnNode(Opcodes.ARETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "preventGenerateSkylightMap"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ILOAD, 1),
                        new InsnNode(Opcodes.IRETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "doPropagateSkylight"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name,
                                "field_76639_c", "[Z"),
                        new VarInsnNode(Opcodes.ILOAD, 2),
                        new VarInsnNode(Opcodes.ILOAD, 3),
                        new IntInsnNode(Opcodes.BIPUSH, 16),
                        new InsnNode(Opcodes.IMUL),
                        new InsnNode(Opcodes.IADD),
                        new InsnNode(Opcodes.ICONST_1),
                        new InsnNode(Opcodes.BASTORE),
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new InsnNode(Opcodes.ICONST_1),
                        new org.objectweb.asm.tree.FieldInsnNode(Opcodes.PUTFIELD, node.name,
                                "field_76650_s", "Z"),
                        new InsnNode(Opcodes.RETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "fakeGetLightFor"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new VarInsnNode(Opcodes.ALOAD, 3),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getLightFor",
                                "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                                false),
                        new InsnNode(Opcodes.IRETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "alfheim$getCachedLightFor"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getLightFor",
                                "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                                false),
                        new InsnNode(Opcodes.I2B),
                        new InsnNode(Opcodes.IRETURN)));

        LOGGER.info("Patched Alfheim 1.6 ChunkMixin for FFD's 24-section light storage");
        return write(node);
    }

    private static byte[] transformChunkCacheMixin(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("dev/redstudio/alfheim/mixin/ChunkCacheMixin".equals(node.name),
                "Unexpected Alfheim ChunkCacheMixin class");
        MethodNode lightForExt = findMethodOptional(node, "func_175629_a", "func_175629_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I");
        if (lightForExt != null) {
            InsnList condition = list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                            "net/minecraft/world/ChunkCache"),
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/ChunkCache",
                            "ffd$isExtended", "()Z", false));
            prependConditionBranch(lightForExt, condition, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                            "net/minecraft/world/ChunkCache"),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/ChunkCache",
                            "ffd$getLightForExt",
                            "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                            false),
                    new InsnNode(Opcodes.IRETURN)));
        }
        LOGGER.info("Patched Alfheim 1.6 ChunkCacheMixin for extended-height light access");
        return write(node);
    }

    private static InsnList loadMixinSelfAsWorld() {
        return list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                        "net/minecraft/world/World"));
    }

    private static InsnList loadAlfheimChunkWorld(String owner) {
        return list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner,
                        "field_76637_e", "Lnet/minecraft/world/World;"));
    }

    private static void prependExtendedWorldBranch(MethodNode method, InsnList worldLoad,
            InsnList extendedBody) {
        worldLoad.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isExtended",
                "(Lnet/minecraft/world/World;)Z", false));
        prependConditionBranch(method, worldLoad, extendedBody);
    }

    private static void prependConditionBranch(MethodNode method, InsnList condition,
            InsnList trueBody) {
        LabelNode original = new LabelNode();
        InsnList prefix = new InsnList();
        prefix.add(condition);
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        prefix.add(trueBody);
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        AbstractInsnNode first = method.instructions.getFirst();
        if (first == null) {
            method.instructions.add(prefix);
        } else {
            method.instructions.insertBefore(first, prefix);
        }
    }

    private static InsnList copy(InsnList source) {
        Map<LabelNode, LabelNode> labels = new IdentityHashMap<LabelNode, LabelNode>();
        for (AbstractInsnNode instruction = source.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof LabelNode) {
                labels.put((LabelNode) instruction, new LabelNode());
            }
        }
        InsnList result = new InsnList();
        for (AbstractInsnNode instruction = source.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            result.add(instruction.clone(labels));
        }
        return result;
    }

    private static void setMixinAnnotationInt(MethodNode method, String desc, String key,
            int value) {
        AnnotationNode annotation = findAnnotation(method.visibleAnnotations, desc);
        if (annotation == null) {
            annotation = findAnnotation(method.invisibleAnnotations, desc);
        }
        require(annotation != null, "Missing annotation " + desc + " on " + method.name);
        if (annotation.values == null) {
            annotation.values = new java.util.ArrayList<Object>();
        }
        for (int index = 0; index < annotation.values.size(); index += 2) {
            if (key.equals(annotation.values.get(index))) {
                annotation.values.set(index + 1, Integer.valueOf(value));
                return;
            }
        }
        annotation.values.add(key);
        annotation.values.add(Integer.valueOf(value));
    }

    private static AnnotationNode findAnnotation(List<AnnotationNode> annotations, String desc) {
        if (annotations == null) {
            return null;
        }
        for (AnnotationNode annotation : annotations) {
            if (desc.equals(annotation.desc)) {
                return annotation;
            }
        }
        return null;
    }

    private static byte[] stripWrongSideMembers(byte[] basicClass) {
        String currentSide = currentSide();
        if (currentSide == null) {
            return basicClass;
        }
        ClassNode node = read(basicClass);
        boolean changed = false;
        for (int index = node.fields.size() - 1; index >= 0; index--) {
            FieldNode field = node.fields.get(index);
            if (hasWrongSide(field.visibleAnnotations, currentSide)
                    || hasWrongSide(field.invisibleAnnotations, currentSide)) {
                node.fields.remove(index);
                changed = true;
            }
        }
        for (int index = node.methods.size() - 1; index >= 0; index--) {
            MethodNode method = node.methods.get(index);
            if (hasWrongSide(method.visibleAnnotations, currentSide)
                    || hasWrongSide(method.invisibleAnnotations, currentSide)) {
                node.methods.remove(index);
                changed = true;
            }
        }
        return changed ? write(node) : basicClass;
    }

    private static boolean hasWrongSide(List<AnnotationNode> annotations, String currentSide) {
        AnnotationNode sideOnly = findAnnotation(annotations,
                "Lnet/minecraftforge/fml/relauncher/SideOnly;");
        if (sideOnly == null || sideOnly.values == null) {
            return false;
        }
        for (int index = 0; index < sideOnly.values.size(); index += 2) {
            if (!"value".equals(sideOnly.values.get(index))) {
                continue;
            }
            Object value = sideOnly.values.get(index + 1);
            if (value instanceof String[]) {
                String[] enumValue = (String[]) value;
                return enumValue.length == 2 && !currentSide.equals(enumValue[1]);
            }
        }
        return false;
    }

    private static String currentSide() {
        try {
            Class<?> handler = Class.forName(
                    "net.minecraftforge.fml.relauncher.FMLLaunchHandler", false,
                    FFDAlfheimCompatTransformer.class.getClassLoader());
            Object side = handler.getMethod("side").invoke(null);
            return String.valueOf(side);
        } catch (ReflectiveOperationException | LinkageError exception) {
            return null;
        }
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode(Opcodes.ASM5);
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static byte[] write(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static MethodNode findMethod(ClassNode node, String mcpName, String srgName,
            String desc) {
        MethodNode method = findMethodOptional(node, mcpName, srgName, desc);
        if (method != null) {
            return method;
        }
        throw new IllegalStateException("Missing method " + node.name + "." + mcpName + desc);
    }

    private static MethodNode findMethodByName(ClassNode node, String name) {
        MethodNode result = null;
        for (MethodNode method : node.methods) {
            if (!name.equals(method.name)) {
                continue;
            }
            require(result == null, "Multiple methods named " + name + " in " + node.name);
            result = method;
        }
        require(result != null, "Missing method " + node.name + "." + name);
        return result;
    }

    private static MethodNode findMethodOptional(ClassNode node, String mcpName, String srgName,
            String desc) {
        for (MethodNode method : node.methods) {
            if (desc.equals(method.desc)
                    && (mcpName.equals(method.name) || srgName.equals(method.name))) {
                return method;
            }
        }
        return null;
    }

    private static InsnList list(AbstractInsnNode... nodes) {
        InsnList list = new InsnList();
        for (AbstractInsnNode node : nodes) {
            list.add(node);
        }
        return list;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
