package app;

public class Renderer {
    public Renderer(int width, int height, World world) {
        this.internalWidth = width;
        this.internalHeight = height;

        this.buffer = new Backbuffer(width, height);

        try {
            this.textures = new Texture[] {
                Texture.from_image_path("data/eagle.png"),
                Texture.from_image_path("data/redbrick.png"),
                Texture.from_image_path("data/purplestone.png"),
                Texture.from_image_path("data/greystone.png"),
                Texture.from_image_path("data/bluestone.png"),
                Texture.from_image_path("data/mossy.png"),
                Texture.from_image_path("data/wood.png"),
                Texture.from_image_path("data/colorstone.png")
            };
        } catch (Exception e) {};

        this.world = world;

        this.pos = new Vec2(22, 12);
        this.dir = new Vec2(-1, 0);
        this.plane = new Vec2(0, 0.66);

        this.multicoreRendering = true;
    }

    public Backbuffer render(double delta) {
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

        return this.buffer;
    }

    public void renderPart(double delta, int start, int end) {
        buffer.clearRect(start, 0, end - start, buffer.getHeight());

        for (int x = start; x < end; x++) {
            double cameraX = 2 * x / (double)this.internalWidth - 1;
            Vec2 rayDir = dir.add(plane.mul(cameraX));

            RayResult res = this.world.castRay(RaycastMode.RaycastWorld, pos, rayDir);

            int lineHeigth = (int)(this.internalHeight / res.distance);
            int drawStart = Math.max(0, -lineHeigth / 2 + this.internalHeight / 2);
            int drawEnd = Math.min(this.internalHeight - 1, lineHeigth / 2 + this.internalHeight / 2);

            int texNum = this.world.getBlockFromRayResult(res) - 1;
            double wallX;
            if (res.side == 0) {
                wallX = pos.y + res.distance * rayDir.y;
            } else {
                wallX = pos.x + res.distance * rayDir.x;
            }

            wallX -= Math.floor(wallX);

            Texture t = textures[texNum];
            int texX = (int)(wallX * (double)(t.width));
            if ((res.side == 0 && rayDir.x > 0) ||
                (res.side == 1 && rayDir.y < 0)) {
                texX = t.width - texX - 1;
            }

            for (int y = drawStart; y < drawEnd; y++) {
                int d = (int)(y * 256 - this.internalHeight * 128 + lineHeigth * 128);
                int texY = ((d * t.height) / lineHeigth) / 256;
                int c = t.getColor(texX, texY);
                
                if(res.side == 1) c = 0xFF000000 | (((c & 0xFFFFFF) >> 1) & 8355711);

                buffer.setPixel(x, y, c);
            }
        }
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

    private Vec2 pos;
    private Vec2 dir;
    private Vec2 plane;

    private int internalWidth;
    private int internalHeight;
    private Texture[] textures;
    private Backbuffer buffer;

    private World world;

    private boolean multicoreRendering;

}