package team.bananabank.hauntedfields.entity.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import team.bananabank.hauntedfields.HauntedFields;
import team.bananabank.hauntedfields.entity.ScarecrowEntity;

public class ScarecrowModel extends AnimatedGeoModel<ScarecrowEntity> {
    @Override
    public ResourceLocation getModelResource(ScarecrowEntity object) {
        return new ResourceLocation(HauntedFields.ID, "geo/scarecrow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ScarecrowEntity object) {
        return new ResourceLocation(HauntedFields.ID, "textures/entity/scarecrow/scarecrow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ScarecrowEntity animatable) {
        return new ResourceLocation(HauntedFields.ID, "animations/scarecrow.animation.json");
    }
}
