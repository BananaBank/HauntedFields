package team.bananabank.hauntedfields.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import team.bananabank.hauntedfields.HauntedFields;

public class HEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, HauntedFields.ID);
}
