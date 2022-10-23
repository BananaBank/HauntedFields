package team.bananabank.hauntedfields.entity.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import team.bananabank.hauntedfields.HauntedFields;
import team.bananabank.hauntedfields.entity.CrowEntity;

public class CrowModel extends AnimatedGeoModel<CrowEntity> {
    @Override
    public ResourceLocation getModelResource(CrowEntity object) {
        return new ResourceLocation(HauntedFields.ID, "geo/crow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CrowEntity object) {
        return new ResourceLocation(HauntedFields.ID, "textures/entity/crow/crow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CrowEntity animatable) {
        return new ResourceLocation(HauntedFields.ID, "animations/crow.animation.json");
    }
}
