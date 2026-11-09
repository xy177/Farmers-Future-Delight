package xy177.farmersfuturedelight.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class FFDVanillaAdvancementTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("FFD Advancement Core");
    private static final String COOKING_HOOKS =
            "xy177/farmersfuturedelight/common/advancement/FFDCookingRecipeAdvancements";
    private static final String RECIPE_LOOKUP =
            "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/item/crafting/IRecipe;";
    private static final String RECIPE_UNLOCK = "([Lnet/minecraft/util/ResourceLocation;)V";
    private static final String COOKING_UNLOCK =
            "(Lnet/minecraft/entity/player/EntityPlayerMP;[Lnet/minecraft/util/ResourceLocation;)V";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return basicClass;
        String target = transformedName == null ? name : transformedName;
        if ("net.minecraft.advancements.AdvancementRewards$Deserializer".equals(target)) {
            return transformCookingCall(basicClass, "deserialize", "deserialize",
                    "(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;"
                            + "Lcom/google/gson/JsonDeserializationContext;)"
                            + "Lnet/minecraft/advancements/AdvancementRewards;", true);
        }
        if ("net.minecraft.advancements.critereon.RecipeUnlockedTrigger".equals(target)) {
            return transformCookingCall(basicClass, "deserializeInstance", "func_192166_a",
                    "(Lcom/google/gson/JsonObject;Lcom/google/gson/JsonDeserializationContext;)"
                            + "Lnet/minecraft/advancements/critereon/RecipeUnlockedTrigger$Instance;", true);
        }
        if ("net.minecraft.advancements.AdvancementRewards".equals(target)) {
            return transformCookingCall(basicClass, "apply", "func_192113_a",
                    "(Lnet/minecraft/entity/player/EntityPlayerMP;)V", false);
        }
        if (!"net.minecraft.advancements.AdvancementManager".equals(target)) return basicClass;
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() == Opcodes.INVOKEVIRTUAL
                        && "net/minecraft/advancements/AdvancementList".equals(call.owner)
                        && ("loadAdvancements".equals(call.name) || "func_192083_a".equals(call.name))
                        && "(Ljava/util/Map;)V".equals(call.desc)) {
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = COOKING_HOOKS;
                    call.name = "loadAdvancements";
                    call.desc = "(Lnet/minecraft/advancements/AdvancementList;Ljava/util/Map;)V";
                    call.itf = false;
                    patched = true;
                }
            }
            if (!"(Ljava/util/Map;)V".equals(method.desc)) {
                continue;
            }
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() == Opcodes.INVOKESTATIC
                        && "java/nio/file/Files".equals(call.owner)
                        && "newBufferedReader".equals(call.name)
                        && ("(Ljava/nio/file/Path;)Ljava/io/BufferedReader;".equals(call.desc)
                        || "(Ljava/nio/file/Path;Ljava/nio/charset/Charset;)Ljava/io/BufferedReader;"
                        .equals(call.desc))) {
                    call.owner = "xy177/farmersfuturedelight/common/advancement/FFDVanillaAdvancements";
                    patched = true;
                }
            }
        }
        if (!patched) {
            LOGGER.warn("Could not find the built-in advancement reader in {}", node.name);
            return basicClass;
        }
        ClassWriter writer = new ClassWriter(reader, 0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] transformCookingCall(byte[] basicClass, String methodName, String srgName,
                                               String descriptor, boolean lookup) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        int patched = 0;
        int existing = 0;
        for (MethodNode method : node.methods) {
            if ((!methodName.equals(method.name) && !srgName.equals(method.name))
                    || !descriptor.equals(method.desc)) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                String hookName = lookup ? "resolveRecipe" : "unlockRecipes";
                String hookDescriptor = lookup ? RECIPE_LOOKUP : COOKING_UNLOCK;
                if (call.getOpcode() == Opcodes.INVOKESTATIC && COOKING_HOOKS.equals(call.owner)
                        && hookName.equals(call.name) && hookDescriptor.equals(call.desc)) {
                    existing++;
                    continue;
                }
                boolean matches = lookup
                        ? call.getOpcode() == Opcodes.INVOKESTATIC
                                && "net/minecraft/item/crafting/CraftingManager".equals(call.owner)
                                && ("getRecipe".equals(call.name) || "func_193373_a".equals(call.name))
                                && RECIPE_LOOKUP.equals(call.desc)
                        : call.getOpcode() == Opcodes.INVOKEVIRTUAL
                                && "net/minecraft/entity/player/EntityPlayerMP".equals(call.owner)
                                && ("unlockRecipes".equals(call.name) || "func_193102_a".equals(call.name))
                                && RECIPE_UNLOCK.equals(call.desc);
                if (!matches) continue;
                call.setOpcode(Opcodes.INVOKESTATIC);
                call.owner = COOKING_HOOKS;
                call.name = hookName;
                call.desc = hookDescriptor;
                call.itf = false;
                patched++;
            }
        }
        if (patched + existing != 1) {
            LOGGER.warn("Expected one cooking advancement call in {}.{}{}, found {}", node.name,
                    methodName, descriptor, patched + existing);
            return basicClass;
        }
        if (patched == 0) return basicClass;
        ClassWriter writer = new ClassWriter(reader, 0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
