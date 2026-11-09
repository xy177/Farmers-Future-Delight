package xy177.farmersfuturedelight.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

final class FFDCommandCoordinates {
    private FFDCommandCoordinates() {
    }

    static Vec3d parse(ICommandSender sender, String[] arguments, int start,
                       boolean centerBlock) throws CommandException {
        validate(arguments, start, centerBlock);
        if (arguments[start].startsWith("^")) {
            double left = parseLocalOffset(arguments[start]);
            double up = parseLocalOffset(arguments[start + 1]);
            double forward = parseLocalOffset(arguments[start + 2]);
            Vec3d origin;
            float yaw;
            float pitch;
            if (sender instanceof ContextSender) {
                ContextSender context = (ContextSender) sender;
                origin = context.getAnchorPosition();
                yaw = context.getYaw();
                pitch = context.getPitch();
            } else {
                origin = sender.getPositionVector();
                Entity entity = sender.getCommandSenderEntity();
                yaw = entity == null ? 0.0F : entity.rotationYaw;
                pitch = entity == null ? 0.0F : entity.rotationPitch;
            }
            float yawRadians = (yaw + 90.0F) * 0.017453292F;
            float pitchRadians = -pitch * 0.017453292F;
            float pitchUpRadians = (-pitch + 90.0F) * 0.017453292F;
            double forwardX = MathHelper.cos(yawRadians) * MathHelper.cos(pitchRadians);
            double forwardY = MathHelper.sin(pitchRadians);
            double forwardZ = MathHelper.sin(yawRadians) * MathHelper.cos(pitchRadians);
            double upX = MathHelper.cos(yawRadians) * MathHelper.cos(pitchUpRadians);
            double upY = MathHelper.sin(pitchUpRadians);
            double upZ = MathHelper.sin(yawRadians) * MathHelper.cos(pitchUpRadians);
            double leftX = -(forwardY * upZ - forwardZ * upY);
            double leftY = -(forwardZ * upX - forwardX * upZ);
            double leftZ = -(forwardX * upY - forwardY * upX);
            return new Vec3d(origin.x + leftX * left + upX * up + forwardX * forward,
                    origin.y + leftY * left + upY * up + forwardY * forward,
                    origin.z + leftZ * left + upZ * up + forwardZ * forward);
        }
        Vec3d origin = sender.getPositionVector();
        return new Vec3d(
                CommandBase.parseDouble(origin.x, arguments[start], -30000000, 30000000,
                        centerBlock),
                CommandBase.parseDouble(origin.y, arguments[start + 1], -30000000, 30000000,
                        false),
                CommandBase.parseDouble(origin.z, arguments[start + 2], -30000000, 30000000,
                        centerBlock));
    }

    static BlockPos parseBlockPosition(ICommandSender sender, String[] arguments, int start)
            throws CommandException {
        Vec3d position = parse(sender, arguments, start, false);
        return new BlockPos(position.x, position.y, position.z);
    }

    static void validate(String[] arguments, int start, boolean centerBlock)
            throws CommandException {
        boolean localX = arguments[start].startsWith("^");
        boolean localY = arguments[start + 1].startsWith("^");
        boolean localZ = arguments[start + 2].startsWith("^");
        if (localX || localY || localZ) {
            if (!localX || !localY || !localZ) {
                throw new CommandException(
                        "commands.farmers_future_delight.execute.local_mixed");
            }
            parseLocalOffset(arguments[start]);
            parseLocalOffset(arguments[start + 1]);
            parseLocalOffset(arguments[start + 2]);
            return;
        }
        CommandBase.parseDouble(0.0D, arguments[start], -30000000, 30000000,
                centerBlock);
        CommandBase.parseDouble(0.0D, arguments[start + 1], -30000000, 30000000, false);
        CommandBase.parseDouble(0.0D, arguments[start + 2], -30000000, 30000000,
                centerBlock);
    }

    private static double parseLocalOffset(String value) throws CommandException {
        String offset = value.substring(1);
        if (offset.isEmpty()) {
            return 0.0D;
        }
        try {
            double parsed = Double.parseDouble(offset);
            if (!Double.isFinite(parsed)) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new CommandException(
                    "commands.farmers_future_delight.execute.local_invalid", value);
        }
    }
}
