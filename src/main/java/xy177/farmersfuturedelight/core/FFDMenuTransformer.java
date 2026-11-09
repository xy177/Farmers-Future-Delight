package xy177.farmersfuturedelight.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class FFDMenuTransformer implements IClassTransformer {
    private static final String HOOK = "xy177/farmersfuturedelight/client/FFDModernMenus";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;
        String target = transformedName == null ? name : transformedName;
        boolean menuClass = target.startsWith("net.minecraft.client.gui.") && !target.contains(".inventory.");
        if (!menuClass && !target.equals("net.minecraft.client.gui.GuiMainMenu")
                && !target.equals("net.minecraft.client.gui.GuiSlot") && !target.equals("net.minecraft.client.gui.GuiCreateWorld")
                && !target.equals("net.minecraft.client.Minecraft")
                && !target.equals("net.minecraft.client.network.NetHandlerPlayClient")) return bytes;
        if (!net.minecraftforge.fml.relauncher.FMLLaunchHandler.side().isClient()) return bytes;
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            if (target.endsWith("GuiSlot") && (method.name.equals("drawScreen") || method.name.equals("func_148128_a"))) {
                int overlays = 0;
                int fades = 0;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.name.equals("overlayBackground") || call.name.equals("func_148136_c")) overlays++;
                    if (overlays >= 2 && fades < 2 && call.owner.equals("net/minecraft/client/renderer/Tessellator")
                            && (call.name.equals("draw") || call.name.equals("func_78381_a"))) {
                        call.setOpcode(Opcodes.INVOKESTATIC); call.owner = HOOK; call.name = "listFade";
                        call.desc = "(Lnet/minecraft/client/renderer/Tessellator;)V"; call.itf = false;
                        fades++; changed = true;
                    }
                }
            }
            if (target.endsWith("GuiCreateWorld") && (method.name.equals("recreateFromExistingWorld") || method.name.equals("func_146318_a"))) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) if (instruction.getOpcode() == Opcodes.RETURN) {
                    InsnList copy = new InsnList();
                    copy.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    copy.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    copy.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xy177/farmersfuturedelight/client/FFDWorldCreation",
                            "recreate", "(Lnet/minecraft/client/gui/GuiCreateWorld;Lnet/minecraft/world/storage/WorldInfo;)V", false));
                    method.instructions.insertBefore(instruction, copy); changed = true;
                }
            }
            if (target.equals("net.minecraft.client.network.NetHandlerPlayClient")
                    && (method.name.equals("handleRespawn") || method.name.equals("func_147280_a"))) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.owner.equals("net/minecraft/network/PacketThreadUtil")
                            && (call.name.equals("checkThreadAndEnqueue") || call.name.equals("func_180031_a"))) {
                        InsnList track = new InsnList();
                        track.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        track.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xy177/farmersfuturedelight/client/FFDMenuTransitions",
                                "changeDimension", "(Lnet/minecraft/network/play/server/SPacketRespawn;)V", false));
                        method.instructions.insert(call, track); changed = true;
                    }
                }
            }
            if (target.endsWith("GuiWinGame") && (method.name.equals("drawScreen") || method.name.equals("func_73863_a"))) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.owner.equals("net/minecraft/client/renderer/texture/TextureManager")
                            && (call.name.equals("bindTexture") || call.name.equals("func_110577_a"))) {
                        method.instructions.insertBefore(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "xy177/farmersfuturedelight/client/FFDMenuTransitions", "vignette",
                                "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;", false));
                        changed = true;
                    }
                }
            }
            if (menuClass && !target.endsWith("GuiCreateWorld") && !target.endsWith("GuiWinGame")
                    && (method.name.equals("drawScreen") || method.name.equals("func_73863_a"))) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if ((call.name.equals("drawCenteredString") || call.name.equals("func_73732_a"))
                            && call.desc.equals("(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)V")
                            && call.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        call.setOpcode(Opcodes.INVOKESTATIC);
                        call.owner = HOOK; call.name = "drawTitle";
                        call.desc = "(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)V";
                        call.itf = false; changed = true;
                    }
                }
            }
            if (target.equals("net.minecraft.client.gui.GuiCreateWorld")) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if ((call.name.equals("launchIntegratedServer") || call.name.equals("func_71371_a"))
                            && call.desc.equals("(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;)V")) {
                        method.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
                        call.setOpcode(Opcodes.INVOKESTATIC);
                        call.owner = "xy177/farmersfuturedelight/client/FFDWorldCreation";
                        call.name = "launch";
                        call.desc = "(Lnet/minecraft/client/Minecraft;Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/client/gui/GuiCreateWorld;)V";
                        call.itf = false;
                        changed = true;
                    }
                }
            }
            if (target.equals("net.minecraft.client.Minecraft")
                    && (method.name.equals("launchIntegratedServer") || method.name.equals("func_71371_a"))) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.owner.equals("net/minecraft/world/storage/WorldInfo") && call.name.equals("<init>")
                            && call.desc.equals("(Lnet/minecraft/world/WorldSettings;Ljava/lang/String;)V")) {
                        InsnList hookInfo = new InsnList();
                        hookInfo.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "xy177/farmersfuturedelight/client/FFDWorldCreation", "prepare",
                                "(Lnet/minecraft/world/storage/WorldInfo;)Lnet/minecraft/world/storage/WorldInfo;", false));
                        method.instructions.insert(call, hookInfo);
                        changed = true;
                    }
                }
            }
            String hook = null;
            String descriptor = null;
            String hookOwner = HOOK;
            if (target.endsWith("GuiCreateWorld") && (method.name.equals("drawScreen") || method.name.equals("func_73863_a"))
                    && method.desc.equals("(IIF)V")) {
                hook = "draw";
                hookOwner = "xy177/farmersfuturedelight/client/FFDWorldCreation";
                descriptor = "(Lnet/minecraft/client/gui/GuiCreateWorld;IIF)Z";
            } else if (target.endsWith("GuiWinGame") && (method.name.equals("drawWinGameScreen") || method.name.equals("func_146575_b"))) {
                hook = "credits";
                hookOwner = "xy177/farmersfuturedelight/client/FFDMenuTransitions";
                descriptor = "(Lnet/minecraft/client/gui/GuiWinGame;)Z";
            }
            if (target.endsWith("GuiScreen") && (method.name.equals("drawWorldBackground") || method.name.equals("func_146270_b")
                    || method.name.equals("drawBackground") || method.name.equals("func_146278_c")) && method.desc.equals("(I)V")) {
                hook = "background";
                descriptor = "(Lnet/minecraft/client/gui/GuiScreen;)Z";
            } else if (target.endsWith("GuiMainMenu") && (method.name.equals("renderSkybox") || method.name.equals("func_73971_c"))) {
                hook = "titleBackground";
                descriptor = "(Lnet/minecraft/client/gui/GuiScreen;)Z";
            } else if (target.endsWith("GuiSlot") && method.name.equals("drawContainerBackground")) {
                hook = "listBackground";
                descriptor = "(Lnet/minecraft/client/gui/GuiSlot;)Z";
            } else if (target.endsWith("GuiSlot") && (method.name.equals("overlayBackground") || method.name.equals("func_148136_c"))) {
                hook = "listOverlay";
                descriptor = "(Lnet/minecraft/client/gui/GuiSlot;IIII)Z";
            }
            if (hook == null) continue;
            InsnList entry = new InsnList();
            entry.add(new VarInsnNode(Opcodes.ALOAD, 0));
            if (hook.equals("draw")) {
                entry.add(new VarInsnNode(Opcodes.ILOAD, 1));
                entry.add(new VarInsnNode(Opcodes.ILOAD, 2));
                entry.add(new VarInsnNode(Opcodes.FLOAD, 3));
            }
            if (hook.equals("listOverlay")) for (int i = 1; i <= 4; i++) entry.add(new VarInsnNode(Opcodes.ILOAD, i));
            entry.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hookOwner, hook, descriptor, false));
            LabelNode original = new LabelNode();
            entry.add(new JumpInsnNode(Opcodes.IFEQ, original));
            entry.add(new InsnNode(Opcodes.RETURN));
            entry.add(original);
            entry.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            method.instructions.insert(entry);
            changed = true;
        }
        if (!changed) return bytes;
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
