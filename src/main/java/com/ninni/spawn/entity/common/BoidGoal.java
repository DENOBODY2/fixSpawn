package com.ninni.spawn.entity.common;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

// Code took and modified from https://github.com/Tomate0613/boids,
// I went ahead and used it because the project's license is MIT,
// but if you are the author or someone that knows the author reading this
// and you are not ok with me using it, please put me in contact with the author directly and I will act accordingly by removing it

public class BoidGoal extends Goal {

    public final float separationInfluence;
    public final float separationRange;
    public final float alignmentInfluence;
    public final float cohesionInfluence;
    private final AbstractSchoolingFish mob;
    private int timeToFindNearbyEntities;
    List<? extends AbstractSchoolingFish> nearbyMobs;
    private boolean enabled = true;

    public BoidGoal(AbstractSchoolingFish mob, float separationInfluence, float separationRange, float alignmentInfluence, float cohesionInfluence) {
        timeToFindNearbyEntities = 0;

        this.mob = mob;
        this.separationInfluence = separationInfluence;
        this.separationRange = separationRange;
        this.alignmentInfluence = alignmentInfluence;
        this.cohesionInfluence = cohesionInfluence;
    }

    @Override
    public boolean canUse() {
        return mob.isInWaterOrBubble() && (mob.isFollower() || mob.hasFollowers());
    }

    public void tick() {
        if (!enabled) {
            return;
        }

        if (--this.timeToFindNearbyEntities <= 0) {
            this.timeToFindNearbyEntities = this.adjustedTickDelay(40);
            nearbyMobs = getNearbyEntitiesOfSameClass(mob);
        }

        if (nearbyMobs.isEmpty()) enabled = false;

        if (mob.getRandom().nextInt(5) == 1) {
            mob.addDeltaMovement(separation());
        }
        mob.addDeltaMovement(random());
        mob.addDeltaMovement(cohesion());
        mob.addDeltaMovement(alignment());
    }

    public static List<? extends AbstractSchoolingFish> getNearbyEntitiesOfSameClass(AbstractSchoolingFish mob) {
        Predicate<AbstractSchoolingFish> predicate = (_mob) -> true;

        return mob.level().getEntitiesOfClass(mob.getClass(), mob.getBoundingBox().inflate(4.0, 4.0, 4.0), predicate);
    }

    public Vec3 random() {
        var velocity = mob.getDeltaMovement();

        if (Mth.abs((float) velocity.x) < 0.1 && Mth.abs((float) velocity.z) < 0.1)
            return new Vec3(randomSign() * 0.2, 0, randomSign() * 0.2);

        return Vec3.ZERO;
    }

    public int randomSign() {
        var isNegative = mob.getRandom().nextBoolean();

        if (isNegative) {
            return -1;
        }

        return 1;
    }

    public Vec3 separation() {
        var c = Vec3.ZERO;

        for (AbstractSchoolingFish nearbyMob : nearbyMobs) {
            if ((nearbyMob.position().subtract(mob.position()).length()) < separationRange && !nearbyMob.isDeadOrDying()) {
                c = c.subtract(nearbyMob.position().subtract(mob.position()));
            }
        }

        return c.scale(separationInfluence);
    }

    public Vec3 alignment() {
        var c = Vec3.ZERO;

        for (AbstractSchoolingFish nearbyMob : nearbyMobs) {
            if (!nearbyMob.isDeadOrDying())
                c = c.add(nearbyMob.getDeltaMovement());
        }

        c = c.scale(1f / nearbyMobs.size());
        c = c.subtract(mob.getDeltaMovement());
        return c.scale(alignmentInfluence);
    }

    public Vec3 cohesion() {
        var c = Vec3.ZERO;

        for (AbstractSchoolingFish nearbyMob : nearbyMobs) {
            if (!nearbyMob.isDeadOrDying())
                c = c.add(nearbyMob.position());
        }

        c = c.scale(1f / nearbyMobs.size());
        c = c.subtract(mob.position());
        return c.scale(cohesionInfluence);
    }
}