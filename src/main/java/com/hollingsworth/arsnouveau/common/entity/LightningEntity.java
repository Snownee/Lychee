package com.hollingsworth.arsnouveau.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;

public class LightningEntity extends LightningBolt {

	@SuppressWarnings("unused")
	private int lightningState;

	public LightningEntity(EntityType<? extends LightningBolt> p_20865_, Level p_20866_) {
		super(p_20865_, p_20866_);
	}

	@Override
	public void tick() {
		thunderHit((ServerLevel) level, this);
	}

}
