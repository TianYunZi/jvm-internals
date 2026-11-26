package org.example.jvminternals.ch22;

/** 用有限存活关系与对象大小展示区域角色和回收周期。 */
public final class RegionJourney {
    private static final System.Logger LOG = System.getLogger(RegionJourney.class.getName());
    private static final byte[][] retained = new byte[1024][];
    private static volatile byte[] latest;

    private RegionJourney() { }

    /** 固定128 MiB堆运行；所有分配次数和等待均有上限。 */
    public static void main(String[] args) throws InterruptedException {
        for (int index = 0; index < retained.length; index++) {
            retained[index] = new byte[32768];
        }
        LOG.log(System.Logger.Level.INFO, "阶段=长期持有32 MiB，短命分配64 MiB");
        allocate(32);
        for (int index = 0; index < retained.length; index += 2) {
            retained[index] = null;
        }
        LOG.log(System.Logger.Level.INFO, "阶段=释放一半长期持有，请求并发回收");
        System.gc();
        // 600 KiB超过显式1 MiB区域的一半，展示大对象区域路径。
        latest = new byte[600 * 1024];
        Thread.sleep(100);
        LOG.log(System.Logger.Level.INFO, "阶段=后续128 MiB短命分配，保留16 MiB");
        allocate(64);
        LOG.log(System.Logger.Level.INFO, "阶段=正常退出，保留块={0}，末块字节={1}",
                retained[1].length, latest[0]);
    }

    /** 每批2 MiB短命载荷并留10毫秒展示间隔，无吞吐统计。 */
    private static void allocate(int batches) throws InterruptedException {
        for (int batch = 0; batch < batches; batch++) {
            for (int item = 0; item < 64; item++) {
                byte[] payload = new byte[32768];
                payload[0] = (byte) batch;
                latest = payload;
            }
            Thread.sleep(10);
        }
    }
}
