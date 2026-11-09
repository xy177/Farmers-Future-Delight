package xy177.farmersfuturedelight.common.command;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public final class ContextSender implements ICommandSender {
    private final ICommandSender delegate;
    private final Entity executor;
    private final World world;
    private final Vec3d position;
    private final float pitch;
    private final float yaw;
    private final boolean eyesAnchor;
    private Integer commandResult;
    private Integer queryResult;
    private boolean commandSucceeded;

    public ContextSender(ICommandSender delegate, Entity executor, World world, Vec3d position) {
        this(delegate, executor, world, position,
                executor == null ? 0.0F : executor.rotationPitch,
                executor == null ? 0.0F : executor.rotationYaw, false);
    }

    private ContextSender(ICommandSender delegate, Entity executor, World world, Vec3d position,
                          float pitch, float yaw, boolean eyesAnchor) {
        this.delegate = delegate;
        this.executor = executor;
        this.world = world;
        this.position = position;
        this.pitch = pitch;
        this.yaw = yaw;
        this.eyesAnchor = eyesAnchor;
    }

    public ContextSender withExecutor(Entity entity) {
        return new ContextSender(delegate, entity, world, position, pitch, yaw, eyesAnchor);
    }

    public ContextSender at(Entity entity) {
        return new ContextSender(delegate, executor, entity.getEntityWorld(),
                entity.getPositionVector(), entity.rotationPitch, entity.rotationYaw, eyesAnchor);
    }

    public ContextSender in(World world) {
        double scale = this.world.provider.getMovementFactor() / world.provider.getMovementFactor();
        return new ContextSender(delegate, executor, world,
                new Vec3d(position.x * scale, position.y, position.z * scale),
                pitch, yaw, eyesAnchor);
    }

    public ContextSender positionedAt(Entity entity) {
        return new ContextSender(delegate, executor, world, entity.getPositionVector(),
                pitch, yaw, eyesAnchor);
    }

    public ContextSender positionedAt(Vec3d position) {
        return new ContextSender(delegate, executor, world, position, pitch, yaw, eyesAnchor);
    }

    public ContextSender positionedAtFeet(Vec3d position) {
        return new ContextSender(delegate, executor, world, position, pitch, yaw, false);
    }

    public ContextSender withRotation(float pitch, float yaw) {
        return new ContextSender(delegate, executor, world, position, pitch, yaw, eyesAnchor);
    }

    public ContextSender withEyesAnchor(boolean eyesAnchor) {
        return new ContextSender(delegate, executor, world, position, pitch, yaw, eyesAnchor);
    }

    public ContextSender facing(Vec3d target) {
        double originY = position.y;
        if (eyesAnchor && executor != null) {
            originY += executor.getEyeHeight();
        }
        double deltaX = target.x - position.x;
        double deltaY = target.y - originY;
        double deltaZ = target.z - position.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float nextPitch = MathHelper.wrapDegrees((float) (-MathHelper.atan2(deltaY,
                horizontalDistance) * 57.2957763671875D));
        float nextYaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(deltaZ, deltaX)
                * 57.2957763671875D) - 90.0F);
        return withRotation(nextPitch, nextYaw);
    }

    public float getPitch() {
        return pitch;
    }

    public void clearCommandResult() {
        commandResult = null;
        queryResult = null;
        commandSucceeded = false;
    }

    public void setCommandResult(int result, boolean succeeded) {
        commandResult = result;
        commandSucceeded = succeeded;
    }

    public int getCommandResult(int fallback) {
        return commandResult != null ? commandResult : queryResult != null ? queryResult : fallback;
    }

    public boolean didCommandSucceed(int fallback) {
        return commandResult == null ? fallback > 0 : commandSucceeded;
    }

    public float getYaw() {
        return yaw;
    }

    public Vec3d getAnchorPosition() {
        if (!eyesAnchor || executor == null) {
            return position;
        }
        return new Vec3d(position.x, position.y + executor.getEyeHeight(), position.z);
    }

    @Override
    public String getName() {
        return executor == null ? delegate.getName() : executor.getName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return executor == null ? delegate.getDisplayName() : executor.getDisplayName();
    }

    @Override
    public void sendMessage(ITextComponent component) {
        delegate.sendMessage(component);
    }

    @Override
    public boolean canUseCommand(int permissionLevel, String commandName) {
        return delegate.canUseCommand(permissionLevel, commandName);
    }

    @Override
    public BlockPos getPosition() {
        return new BlockPos(position.x, position.y, position.z);
    }

    @Override
    public Vec3d getPositionVector() {
        return position;
    }

    @Override
    public World getEntityWorld() {
        return world;
    }

    @Override
    public Entity getCommandSenderEntity() {
        return executor;
    }

    @Override
    public boolean sendCommandFeedback() {
        return delegate.sendCommandFeedback();
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount) {
        if (type == CommandResultStats.Type.QUERY_RESULT) {
            queryResult = amount;
        }
        delegate.setCommandStat(type, amount);
    }

    @Override
    public MinecraftServer getServer() {
        return delegate.getServer();
    }
}
