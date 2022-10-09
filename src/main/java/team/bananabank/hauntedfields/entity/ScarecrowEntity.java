package team.bananabank.hauntedfields.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ScarecrowEntity extends Monster implements IAnimatable {
    private AnimationFactory factory = new AnimationFactory(this);

    public ScarecrowEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, (double)0.3F)
                .build();
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.addBehaviorGoals();
    }

    protected void addBehaviorGoals() {
        //this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        //this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, () -> true));
        this.goalSelector.addGoal(7, new ScarecrowEntity.MoveRandomlyGoal(this, 1.0D));
        //this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class)); // Add nearby crows if added to mod
        //this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        //this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        //this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        //this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (isNightTime(level)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.scarecrow.idle_night", true));
        } else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.scarecrow.idle_day", true));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public static boolean isNightTime(Level level) {
        long dayTime = level.getDayTime();

        return dayTime > 13000L && dayTime < 23999L;
    }

    private static class MoveRandomlyGoal extends WaterAvoidingRandomStrollGoal {
        public MoveRandomlyGoal(PathfinderMob mob, double probability) {
            super(mob, probability);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !isNightTime(mob.level);
        }
    }
}
