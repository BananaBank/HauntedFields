package team.bananabank.hauntedfields.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.bananabank.hauntedfields.HauntedFields;
import team.bananabank.hauntedfields.entity.CrowEntity;
import team.bananabank.hauntedfields.entity.ScarecrowEntity;

public class HEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, HauntedFields.ID);

    public static final RegistryObject<EntityType<ScarecrowEntity>> SCARECROW =
            REGISTRY.register("scarecrow", () -> EntityType.Builder.of(ScarecrowEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 2.6f)
                    .build(new ResourceLocation(HauntedFields.ID, "scarecrow").toString()));

    public static final RegistryObject<EntityType<CrowEntity>> CROW =
            REGISTRY.register("crow", () -> EntityType.Builder.<CrowEntity>of(CrowEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 0.9F) // matching parrot
                    .build(new ResourceLocation(HauntedFields.ID, "crow").toString()));

}
