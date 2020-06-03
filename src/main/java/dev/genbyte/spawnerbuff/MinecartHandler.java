package dev.genbyte.spawnerbuff;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import de.tr7zw.nbtapi.NBTTileEntity;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTEntity;

public class MinecartHandler implements Listener {
	private final SpawnerBuff sb;

	public MinecartHandler(SpawnerBuff sb) {
		this.sb = sb;
	}

	@EventHandler
	public void onRedstoneEvent(BlockRedstoneEvent e) {
		Material mat = e.getBlock().getType();

		if (mat.equals(Material.DETECTOR_RAIL)) {
			Block drail = e.getBlock();
			Block above = drail.getRelative(0, 1, 0);
			World world = drail.getWorld();

			if (sb.allowLoad && above.getType().equals(Material.SPAWNER)) {
				loadMinecart(above, world);
			} else if (sb.allowUnload && above.getType().equals(Material.AIR)) {
				unloadMinecart(above, world);
			}
		}
	}

	private void loadMinecart(Block above, World world) {
		CreatureSpawner spawner = (CreatureSpawner) above.getState();
		NBTTileEntity spawnerNbt = new NBTTileEntity(spawner);
		Optional<String> spawnIdOpt = getSpawnId(spawnerNbt);
		if (!spawnIdOpt.isPresent()) {
			sb.getLogger().log(Level.SEVERE, "Spawner at " + spawner.getLocation() + " with invalid NBT");
			return;
		}
		String spawnId = spawnIdOpt.get();

		Collection<Entity> entities = world.getNearbyEntities(spawner.getLocation(), 0.75, 0.75, 0.75, p -> p.getType().equals(EntityType.MINECART));
		if (entities.size() == 0) {
			return;
		}

		Minecart ent = (Minecart) entities.iterator().next();
		if (ent.getPassengers().size() > 0) {
			return;
		}

		ent.remove();

		SpawnerMinecart minecart = (SpawnerMinecart) world.spawnEntity(ent.getLocation(), EntityType.MINECART_MOB_SPAWNER);
		minecart.setVelocity(ent.getVelocity());

		NBTEntity minecartNbt = new NBTEntity(minecart);
		setSpawnId(minecartNbt, spawnId);

		spawner.setType(Material.AIR);
		spawner.getBlock().setType(Material.AIR);
	}

	private void unloadMinecart(Block above, World world) {
		Collection<Entity> entities = world.getNearbyEntities(above.getLocation(), 0.75, 0.75, 0.75, p -> p.getType().equals(EntityType.MINECART_MOB_SPAWNER));
		if (entities.size() == 0) {
			return;
		}

		SpawnerMinecart ent = (SpawnerMinecart) entities.iterator().next();
		NBTEntity nbtent = new NBTEntity(ent);
		Optional<String> spawnIdOpt = getSpawnId(nbtent);
		if (!spawnIdOpt.isPresent()) {
			sb.getLogger().log(Level.SEVERE, "Spawner Minecart at " + ent.getLocation() + " with invalid NBT");
			return;
		}
		String spawnId = spawnIdOpt.get();

		ent.remove();
		Minecart minecart = (Minecart) world.spawnEntity(ent.getLocation(), EntityType.MINECART);
		minecart.setVelocity(ent.getVelocity());

		above.setType(Material.SPAWNER);
		CreatureSpawner spawner = (CreatureSpawner) above.getState();
		NBTTileEntity spawnerNbt = new NBTTileEntity(spawner);
		setSpawnId(spawnerNbt, spawnId);
	}

	private Optional<String> getSpawnId(NBTCompound compound) {
		NBTCompound spawnData = compound.getCompound("SpawnData");
		if (spawnData == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(spawnData.getString("id"));
	}

	private void setSpawnId(NBTCompound compound, String id) {
		NBTCompound spawnData = compound.getCompound("SpawnData");
		if (spawnData == null) {
			spawnData = compound.addCompound("SpawnData");
		}

		spawnData.setString("id", id);
	}
}