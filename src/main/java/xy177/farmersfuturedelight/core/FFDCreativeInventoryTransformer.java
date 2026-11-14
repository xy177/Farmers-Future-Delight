package xy177.farmersfuturedelight.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class FFDCreativeInventoryTransformer implements IClassTransformer {
    private static final String HOOK = "xy177/farmersfuturedelight/client/FFDCreativeInventory";
    private static final String TAB = "net/minecraft/creativetab/CreativeTabs";
    private static final String LIST = "net/minecraft/util/NonNullList";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;
        String target = transformedName == null ? name : transformedName;
        boolean tabs = "net.minecraft.creativetab.CreativeTabs".equals(target);
        boolean dedicated = "xy177.farmersfuturedelight.common.FFDCreativeTab".equals(target);
        boolean gui = "net.minecraft.client.gui.inventory.GuiContainerCreative".equals(target);
        if (!tabs && !dedicated && !gui) return bytes;
        if (!net.minecraftforge.fml.relauncher.FMLLaunchHandler.side().isClient()) return bytes;
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (gui && instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (TAB.equals(call.owner) && "getTabPage".equals(call.name) && "()I".equals(call.desc)) {
                        call.setOpcode(Opcodes.INVOKESTATIC);
                        call.owner = HOOK;
                        call.name = "pageOf";
                        call.desc = "(L" + TAB + ";)I";
                        changed = true;
                    }
                    if ("net/minecraft/item/Item".equals(call.owner)
                            && ("getCreativeTab".equals(call.name) || "func_77640_w".equals(call.name))
                            && ("renderToolTip".equals(method.name) || "func_146285_a".equals(method.name))) {
                        InsnList hook = new InsnList();
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "displayTab",
                                "(L" + TAB + ";Lnet/minecraft/item/ItemStack;)L" + TAB + ";", false));
                        method.instructions.insert(instruction, hook);
                        changed = true;
                    }
                    if ("java/util/Arrays".equals(call.owner) && "copyOfRange".equals(call.name)
                            && "([Ljava/lang/Object;II)[Ljava/lang/Object;".equals(call.desc)) {
                        call.owner = HOOK;
                        call.name = "pageTabs";
                        changed = true;
                    }
                }
                if ((tabs || dedicated) && instruction.getOpcode() == Opcodes.RETURN
                        && ("displayAllRelevantItems".equals(method.name) || "func_78018_a".equals(method.name))
                        && ("(L" + LIST + ";)V").equals(method.desc)) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "afterDisplay",
                            "(L" + TAB + ";L" + LIST + ";)V", false));
                    method.instructions.insertBefore(instruction, hook);
                    changed = true;
                }
                if (tabs && instruction.getOpcode() == Opcodes.ARETURN
                        && ("getTranslatedTabLabel".equals(method.name) || "func_78024_c".equals(method.name))) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "tabLabel",
                            "(Ljava/lang/String;L" + TAB + ";)Ljava/lang/String;", false));
                    method.instructions.insertBefore(instruction, hook);
                    changed = true;
                }
                if (tabs && instruction.getOpcode() == Opcodes.ARETURN
                        && ("getIconItemStack".equals(method.name) || "func_151244_d".equals(method.name))) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "tabIcon",
                            "(Lnet/minecraft/item/ItemStack;L" + TAB + ";)Lnet/minecraft/item/ItemStack;", false));
                    method.instructions.insertBefore(instruction, hook);
                    changed = true;
                }
                if (tabs && instruction.getOpcode() == Opcodes.IRETURN && "getTabPage".equals(method.name)) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "tabPage", "(IL" + TAB + ";)I", false));
                    method.instructions.insertBefore(instruction, hook);
                    changed = true;
                }
            }
        }
        if (!changed) return bytes;
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
