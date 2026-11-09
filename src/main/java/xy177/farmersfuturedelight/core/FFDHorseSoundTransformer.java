package xy177.farmersfuturedelight.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class FFDHorseSoundTransformer {
    private FFDHorseSoundTransformer() {
    }

    public static byte[] transformArmor(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) {
            if (!("onInventoryChanged".equals(method.name) || "func_76316_a".equals(method.name))
                    || !"(Lnet/minecraft/inventory/IInventory;)V".equals(method.desc)) continue;
            String getter = "func_76316_a".equals(method.name) ? "func_184783_dl" : "getHorseArmorType";
            int previousArmor = -1;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                AbstractInsnNode store = instruction.getNext();
                if (getter.equals(call.name) && store instanceof VarInsnNode
                        && store.getOpcode() == Opcodes.ASTORE) {
                    previousArmor = ((VarInsnNode) store).var;
                    break;
                }
            }
            if (previousArmor < 0) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() != Opcodes.RETURN) continue;
                InsnList notify = new InsnList();
                notify.add(new VarInsnNode(Opcodes.ALOAD, 0));
                notify.add(new VarInsnNode(Opcodes.ALOAD, previousArmor));
                notify.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "xy177/farmersfuturedelight/common/entity/FFDHorseSounds", "armorChanged",
                        "(Lnet/minecraft/entity/passive/EntityHorse;Lnet/minecraft/entity/passive/HorseArmorType;)V", false));
                method.instructions.insertBefore(instruction, notify);
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean mapped = false;
        String descriptor = "(Lnet/minecraft/util/SoundEvent;FF)V";
        for (MethodNode method : node.methods) {
            if (("playSound".equals(method.name) || "func_184185_a".equals(method.name))
                    && descriptor.equals(method.desc)) return bytes;
            if ("func_70636_d".equals(method.name)) mapped = true;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode) instruction;
                    if (field.getOpcode() == Opcodes.GETSTATIC && "net/minecraft/init/SoundEvents".equals(field.owner)
                            && ("ENTITY_HORSE_EAT".equals(field.name) || "field_187711_cp".equals(field.name))) {
                        InsnList adapt = new InsnList();
                        adapt.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        adapt.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "xy177/farmersfuturedelight/common/entity/FFDHorseSounds", "adapt",
                                "(Lnet/minecraft/util/SoundEvent;Lnet/minecraft/entity/passive/AbstractHorse;)Lnet/minecraft/util/SoundEvent;", false));
                        method.instructions.insert(instruction, adapt);
                    }
                }
                if (("getAngrySound".equals(method.name) || "func_184785_dv".equals(method.name))
                        && instruction.getOpcode() == Opcodes.ARETURN) {
                    InsnList adapt = new InsnList();
                    adapt.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    adapt.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "xy177/farmersfuturedelight/common/entity/FFDHorseSounds", "angry",
                            "(Lnet/minecraft/util/SoundEvent;Lnet/minecraft/entity/passive/AbstractHorse;)Lnet/minecraft/util/SoundEvent;", false));
                    method.instructions.insertBefore(instruction, adapt);
                }
            }
        }
        String name = mapped ? "func_184185_a" : "playSound";
        MethodNode sound = new MethodNode(Opcodes.ACC_PUBLIC, name, descriptor, null, null);
        sound.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        sound.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        sound.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        sound.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "xy177/farmersfuturedelight/common/entity/FFDHorseSounds", "adapt",
                "(Lnet/minecraft/util/SoundEvent;Lnet/minecraft/entity/passive/AbstractHorse;)Lnet/minecraft/util/SoundEvent;", false));
        sound.instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
        sound.instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
        sound.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, node.superName, name, descriptor, false));
        sound.instructions.add(new InsnNode(Opcodes.RETURN));
        node.methods.add(sound);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
