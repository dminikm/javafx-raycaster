package app;

import java.util.List;

public class Renderer {
    public Renderer(int width, int height, World world, Textureregistry registry) {
        this.internalWidth = width;
        this.internalHeight = height;

        this.buffer = new ColorBuffer(width, height);
        this.zBuffer = new double[width];

        this.world = world;
        this.textureRegistry = registry;

        this.multicoreRendering = false;
    }

    public ColorBuffer render(double delta) {
        if (!this.multicoreRendering) {
            this.renderPart(delta, 0, internalWidth);
        } else {
            Thread[] threads = this.getThreads();

            int step = this.internalWidth / (threads.length + 1);
            for (int i = 0; i < threads.length; i++) {
                int start = step * i;
                int end = step * (i + 1);
                threads[i] = new Thread(
                    new ThreadRenderer(this, delta, start, end)
                );

                System.out.printf("[Rendering] thread: %d, start: %d ,end: %d\n", i, start, end);
                threads[i].start();
            }

            this.renderPart(delta, step * threads.length, this.internalWidth);

            for (int i = 0; i < threads.length; i++) {
                try { threads[i].join(); } catch(Exception e) {
                    System.out.printf("[Rendering] thread %d could not be joined!\n", i);
                }
            }
        }

        this.renderSprites(delta);

        return this.buffer;
    }

    public void renderPart(double delta, int start, int end) {
        buffer.clearRect(start, 0, end - start, buffer.getHeight());

        Vec2 dir = this.world.getPlayer().getDirection();
        Vec2 pos = this.world.getPlayer().getPosition();
        Vec2 plane = this.getPlane();
        

        for (int x = start; x < end; x++) {
            double cameraX = 2 * x / (double)this.internalWidth - 1;
            Vec2 rayDir = dir.add(plane.mul(cameraX));

            RayResult rayResult = this.world.castRay(pos, rayDir);
            RayHit res = rayResult.blockHit;
            
            if (res.hit) {
                int lineHeigth = (int)(this.internalHeight / res.distance);
                int drawStart = Math.max(0, -lineHeigth / 2 + this.internalHeight / 2);
                int drawEnd = Math.min(this.internalHeight - 1, lineHeigth / 2 + this.internalHeight / 2);

                double wallX;
                if (res.side == 0) {
                    wallX = pos.y + res.distance * rayDir.y;
                } else {
                    wallX = pos.x + res.distance * rayDir.x;
                }

                wallX -= Math.floor(wallX);

                Texture t = this.textureRegistry.getTextureForId(this.world.getBlockFromRayResult(rayResult));
                int texX = (int)(wallX * (double)(t.width));
                if ((res.side == 0 && rayDir.x > 0) ||
                    (res.side == 1 && rayDir.y < 0)) {
                    texX = t.width - texX - 1;
                }

                for (int y = drawStart; y < drawEnd; y++) {
                    int d = (int)(y * 256 - this.internalHeight * 128 + lineHeigth * 128);
                    int texY = ((d * t.height) / lineHeigth) / 256;
                    int c = t.getPixel(texX, texY);
                    
                    if(res.side == 1) c = 0xFF000000 | (((c & 0xFFFFFF) >>> 1) & 8355711);

                    buffer.setPixel(x, y, c);
                }

                this.zBuffer[x] = res.distance;
            }

            rayResult.tileEntitiesHit.sort((final TileEntityRayHit t1, final TileEntityRayHit t2) -> {
                Double s1Distance = t1.distance;
                Double s2Distance = t2.distance;
    
                return s2Distance.compareTo(s1Distance);
            });

            for (TileEntityRayHit terh : rayResult.tileEntitiesHit) {
                if (terh.hit && terh.distance < this.zBuffer[x]) {
                    int lineHeigth = (int)(this.internalHeight / terh.distance);
                    int drawStart = Math.max(0, -lineHeigth / 2 + this.internalHeight / 2);
                    int drawEnd = Math.min(this.internalHeight - 1, lineHeigth / 2 + this.internalHeight / 2);
    
                    double wallX;
                    if (terh.side == 0) {
                        wallX = pos.y + terh.distance * rayDir.y;
                    } else {
                        wallX = pos.x + terh.distance * rayDir.x;
                    }
    
                    wallX -= Math.floor(wallX);
    
                    Texture t = this.textureRegistry.getTextureForId(terh.entity.getTextureId());
                    int texX = (int)(wallX * (double)(t.width));
                    if ((terh.side == 0 && rayDir.x > 0) ||
                        (terh.side == 1 && rayDir.y < 0)) {
                        texX = t.width - texX - 1;
                    }
    
                    for (int y = drawStart; y < drawEnd; y++) {
                        int d = (int)(y * 256 - this.internalHeight * 128 + lineHeigth * 128);
                        int texY = ((d * t.height) / lineHeigth) / 256;
                        int c = t.getPixel(texX, texY);
                        
                        //if(terh.side == 1) c = 0xFF000000 | (((c & 0xFFFFFF) >>> 1) & 8355711);
    
                        buffer.setPixelTransparent(x, y, c);
                    }
    
                    this.zBuffer[x] = terh.distance;
                }
            }
        }
    }

    private void renderSprites(double delta) {
        List<Sprite> sprites = this.world.getAllSprites();
        Vec2 pos = this.world.getPlayer().getPosition();
        Vec2 dir = this.world.getPlayer().getDirection();
        Vec2 plane = this.getPlane();

        sprites.sort((final Sprite s1, final Sprite s2) -> {
            Double s1Distance = s1.pos.distance(pos);
            Double s2Distance = s2.pos.distance(pos);

            return s2Distance.compareTo(s1Distance);
        });

        for (Sprite sprite : sprites) {
            Vec2 spritePos = sprite.pos.sub(pos);

            double invDet = 1.0 / (plane.x * dir.y - dir.x * plane.y);

            double transformX = invDet * (dir.y * spritePos.x - dir.x * spritePos.y);
            double transformY = invDet * (-plane.y * spritePos.x + plane.x * spritePos.y);

            int spriteScreenX = (int)((this.internalWidth / 2) * (1 + transformX / transformY));

            int spriteHeight = Math.abs((int)(this.internalHeight / transformY));
            int drawStartY = Math.max(0, -spriteHeight / 2 + this.internalHeight / 2);
            int drawEndY = Math.min(this.internalHeight - 1, spriteHeight / 2 + this.internalHeight / 2);

            int spriteWidth = Math.abs((int)(this.internalHeight / transformY));
            int drawStartX = Math.max(0, -spriteWidth / 2 + spriteScreenX);
            int drawEndX = Math.min(this.internalWidth - 1, spriteWidth /2 + spriteScreenX);

            Texture tex = this.textureRegistry.getTextureForId(sprite.textureId);

            for (int stripe = drawStartX; stripe < drawEndX; stripe++) {
                int texX = (int)(256 * (stripe - (-spriteWidth / 2 + spriteScreenX)) * tex.width / spriteWidth) / 256;

                if (transformY > 0 &&                           // In front of the camer
                    stripe > 0 &&                               // On the screen
                    stripe < this.internalWidth &&              //
                    transformY < this.zBuffer[stripe]           // not behind the wall
                ) {
                    for (int y = drawStartY; y < drawEndY; y++) {
                        int d = (y) * 256 - this.internalHeight * 128 + spriteHeight * 128;
                        int texY = ((d * tex.height) / spriteHeight) / 256;

                        int color = tex.getPixel(texX, texY);
                        this.buffer.setPixelTransparent(stripe, y, color);
                    }
                }
            }
        }
    }

    private Vec2 getPlane() {
        Vec2 dir = this.world.getPlayer().getDirection();
        return Vec2.fromAngle(dir.toAngle() + 90).mul(0.66);
    }

    private Thread[] getThreads() {
        int numThreads = Runtime.getRuntime().availableProcessors() - 2;

        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread();
        }

        return threads;
    }
    
    private class ThreadRenderer implements Runnable {
        public ThreadRenderer(Renderer r, double delta, int start, int end) {
            this.r = r;
            this.start = start;
            this.end = end;
            this.delta = delta;
        }

        @Override
        public void run() {
            r.renderPart(this.delta, this.start, this.end);
        }

        private Renderer r;
        private int start;
        private int end;
        private double delta;
    }

    private int internalWidth;
    private int internalHeight;
    
    private ColorBuffer buffer;
    private double[] zBuffer;

    private World world;
    private Textureregistry textureRegistry;

    private boolean multicoreRendering;

}