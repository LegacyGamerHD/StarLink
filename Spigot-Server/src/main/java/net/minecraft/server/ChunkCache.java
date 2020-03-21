package net.minecraft.server;

import javax.annotation.Nullable;

import cc.bukkit.starlink.annotation.ObfuscateHelper;

public class ChunkCache implements IBlockAccess, ICollisionAccess {

    protected final int a;
    protected final int b;
    protected final IChunkAccess[][] c;
    protected boolean d;
    protected final World e;

    public ChunkCache(World world, BlockPosition blockposition, BlockPosition blockposition1) {
        this.e = world;
        this.a = blockposition.getX() >> 4;
        this.b = blockposition.getZ() >> 4;
        int i = blockposition1.getX() >> 4;
        int j = blockposition1.getZ() >> 4;

        this.c = new IChunkAccess[i - this.a + 1][j - this.b + 1];
        IChunkProvider ichunkprovider = world.getChunkProvider();

        this.d = true;

        int k;
        int l;

        for (k = this.a; k <= i; ++k) {
            for (l = this.b; l <= j; ++l) {
                this.c[k - this.a][l - this.b] = ichunkprovider.a(k, l);
            }
        }

        for (k = blockposition.getX() >> 4; k <= blockposition1.getX() >> 4; ++k) {
            for (l = blockposition.getZ() >> 4; l <= blockposition1.getZ() >> 4; ++l) {
                IChunkAccess ichunkaccess = this.c[k - this.a][l - this.b];

                if (ichunkaccess != null && !ichunkaccess.a(blockposition.getY(), blockposition1.getY())) {
                    this.d = false;
                    return;
                }
            }
        }

    }

    @ObfuscateHelper("getCachedChunkAt") // StarLink
    private IChunkAccess d(BlockPosition blockposition) {
        return this.a(blockposition.getX() >> 4, blockposition.getZ() >> 4);
    }

    @ObfuscateHelper("getCachedChunkAt") // StarLink
    private IChunkAccess a(int i, int j) {
        int k = i - this.a;
        int l = j - this.b;

        if (k >= 0 && k < this.c.length && l >= 0 && l < this.c[k].length) {
            IChunkAccess ichunkaccess = this.c[k][l];

            return (IChunkAccess) (ichunkaccess != null ? ichunkaccess : new ChunkEmpty(this.e, new ChunkCoordIntPair(i, j)));
        } else {
            return new ChunkEmpty(this.e, new ChunkCoordIntPair(i, j));
        }
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.e.getWorldBorder();
    }

    @Override
    @ObfuscateHelper("getCachedChunkAt") // StarLink
    public IBlockAccess c(int i, int j) {
        return this.a(i, j);
    }

    @Nullable
    @Override
    @ObfuscateHelper("getCachedTileEntityAt") // StarLink
    public TileEntity getTileEntity(BlockPosition blockposition) {
        IChunkAccess ichunkaccess = this.d(blockposition);

        return ichunkaccess.getTileEntity(blockposition);
    }

    @Override
    @ObfuscateHelper("getCachedTypeAt") // StarLink
    public IBlockData getType(BlockPosition blockposition) {
        if (World.isOutsideWorld(blockposition)) {
            return Blocks.AIR.getBlockData();
        } else {
            IChunkAccess ichunkaccess = this.d(blockposition);

            return ichunkaccess.getType(blockposition);
        }
    }

    @Override
    @ObfuscateHelper("getCachedFluidAt") // StarLink
    public Fluid getFluid(BlockPosition blockposition) {
        if (World.isOutsideWorld(blockposition)) {
            return FluidTypes.EMPTY.h();
        } else {
            IChunkAccess ichunkaccess = this.d(blockposition);

            return ichunkaccess.getFluid(blockposition);
        }
    }
}