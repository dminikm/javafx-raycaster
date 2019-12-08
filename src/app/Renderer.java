package app;

import java.util.List;

public class Renderer {
    public Renderer(int width, int height, World world, TextureRegistry registry) {
        this.internalWidth = width;
        this.internalHeight = height;

        this.buffer = new ColorBuffer(width, height);
        this.zBuffer = new double[width];

        this.world = world;
        this.textureRegistry = registry;
    }

    public ColorBuffer render(double delta) {
        this.renderWorld(delta);
        this.renderSprites(delta);
        this.renderWeapon();

        return this.buffer;
    }

    public void renderWorld(double delta) {
        buffer.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());

        Vec2 dir = this.world.getPlayer().getDirection();
        Vec2 pos = this.world.getPlayer().getPosition();
        Vec2 plane = this.getPlane();
        

        for (int x = 0; x < buffer.getWidth(); x++) {
            double cameraX = 2 * x / (double)this.internalWidth - 1;
            Vec2 rayDir = dir.add(plane.mul(cameraX));

            RaycastResult res = this.world.castRay(pos, rayDir);
                        
            if (res.hit) {
                int lineHeigth = (int)(this.internalHeight / res.distance);
                int drawStart = Math.max(0, -lineHeigth / 2 + this.internalHeight / 2);
                int drawEnd = Math.min(this.internalHeight - 1, lineHeigth / 2 + this.internalHeight / 2);

                Texture t = null;

                boolean applyShading = true;
                if (res instanceof TileEntityRaycastResult) {
                    t = this.textureRegistry.getTextureForId(((TileEntityRaycastResult)res).entity.getTextureId());
                    applyShading = ((TileEntityRaycastResult)res).entity.canApplyShading();
                }

                if (res instanceof BlockRaycastResult) {
                    t = this.textureRegistry.getTextureForId(((BlockRaycastResult)res).blockId);
                }

                int texX = (int)(res.startOffset * (double)(t.width));
                //if ((res.side == 0 && rayDir.x > 0) ||
                //    (res.side == 1 && rayDir.y < 0)) {
                //    texX = t.width - texX - 1;
                //}

                //if (texX > 0) {
                //    texX -= 1;
                //}

                for (int y = drawStart; y < drawEnd; y++) {
                    int d = (int)(y * 256 - this.internalHeight * 128 + lineHeigth * 128);
                    int texY = ((d * t.height) / lineHeigth) / 256;
                    int c = t.getPixel(texX, texY);
                    
                    if(res.side == 1 && applyShading) c = 0xFF000000 | (((c & 0xFFFFFF) >>> 1) & 8355711);

                    buffer.setPixel(x, y, c);
                }

                this.zBuffer[x] = res.distance;
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
                        int texY = (((d * tex.height) / spriteHeight) / 256);
                        int color = tex.getPixel(texX, texY);

                        this.buffer.setPixelTransparent(stripe, y, color);
                    }
                }
            }
        }
    }

    private void renderWeapon() {
        Player p = world.getPlayer();
        Weapon w = p.getCurrentWeapon();
        Texture t = this.textureRegistry.getTextureForId(w.getTexture());

        int screenX = (this.buffer.width / 2) - (t.width / 2);
        int screenY = (this.buffer.height) - (t.height);

        this.buffer.copyFromBuffer(t, screenX, screenY);
    }

    private Vec2 getPlane() {
        Vec2 dir = this.world.getPlayer().getDirection();
        return Vec2.fromAngle(dir.toAngle() + 90).mul(0.66);
    }

    private int internalWidth;
    private int internalHeight;
    
    private ColorBuffer buffer;
    private double[] zBuffer;

    private World world;
    private TextureRegistry textureRegistry;
}