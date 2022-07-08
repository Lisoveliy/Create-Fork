package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.client.Camera;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class ContraptionInstanceManager extends BlockEntityInstanceManager {

	protected ArrayList<ActorInstance> actors = new ArrayList<>();

	private final VirtualRenderWorld renderWorld;

	private Contraption contraption;

	ContraptionInstanceManager(MaterialManager materialManager, VirtualRenderWorld renderWorld, Contraption contraption) {
		super(materialManager);
		this.renderWorld = renderWorld;
		this.contraption = contraption;
	}

	public void tick() {
		actors.forEach(ActorInstance::tick);
	}

	@Override
	protected boolean canCreateInstance(BlockEntity blockEntity) {
		return !contraption.isHiddenInPortal(blockEntity.getBlockPos());
	}

	@Override
	public void beginFrame(TaskEngine taskEngine, Camera info) {
		super.beginFrame(taskEngine, info);

		actors.forEach(ActorInstance::beginFrame);
	}

	@Override
	protected void updateInstance(DynamicInstance dyn, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		dyn.beginFrame();
	}

	@Nullable
	public ActorInstance createActor(Pair<StructureBlockInfo, MovementContext> actor) {
		StructureBlockInfo blockInfo = actor.getLeft();
		MovementContext context = actor.getRight();

		if (contraption.isHiddenInPortal(context.localPos))
			return null;

		MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state);

		if (movementBehaviour != null && movementBehaviour.hasSpecialInstancedRendering()) {
			ActorInstance instance = movementBehaviour.createInstance(materialManager, renderWorld, context);

			actors.add(instance);

			return instance;
		}

		return null;
	}

	// -----------------------------------------------------------------
	// The following methods should be identical to the base methods,
	// but without reference to LightUpdater.
	// -----------------------------------------------------------------

	@Override
	protected AbstractInstance createInternal(BlockEntity obj) {
		AbstractInstance renderer = createRaw(obj);

		if (renderer != null) {
			renderer.init();
			renderer.updateLight();
			instances.put(obj, renderer);

			if (renderer instanceof TickableInstance r) {
				tickableInstances.put(obj, r);
				r.tick();
			}

			if (renderer instanceof DynamicInstance r) {
				dynamicInstances.put(obj, r);
				r.beginFrame();
			}
		}

		return renderer;
	}

	protected void removeInternal(BlockEntity obj, AbstractInstance instance) {
		instance.remove();
		instances.remove(obj);
		dynamicInstances.remove(obj);
		tickableInstances.remove(obj);
	}

	@Override
	public void detachLightListeners() {
		// noop
	}
}

