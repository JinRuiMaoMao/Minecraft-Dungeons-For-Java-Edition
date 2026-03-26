package jinrui.mcdar.api.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Summoned mobs that store an owner UUID (Yarn has no Mojmap {@code OwnableEntity}).
 */
public interface OwnableSummon extends Summonable {
    @Nullable UUID getOwnerUUID();

    @Nullable
    default LivingEntity getOwner() {
        if (!(this instanceof LivingEntity self)) {
            return null;
        }
        UUID uuid = getOwnerUUID();
        if (uuid == null) {
            return null;
        }
        World world = self.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return null;
        }
        Entity entity = serverWorld.getEntity(uuid);
        return entity instanceof LivingEntity living ? living : null;
    }
}
