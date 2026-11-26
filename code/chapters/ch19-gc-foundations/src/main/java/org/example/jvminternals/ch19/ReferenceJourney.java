package org.example.jvminternals.ch19;

import java.lang.ref.Cleaner;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/** 用真实引用对象观察强持有、队列通知与显式清理，不要求回收时点。 */
public final class ReferenceJourney {
    private static final System.Logger LOG = System.getLogger(ReferenceJourney.class.getName());
    private static Object retained;

    private ReferenceJourney() { }

    /** 最多等待两次1500毫秒；未收到通知也是有效观察。 */
    public static void main(String[] args) throws InterruptedException {
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        retained = new byte[1024 * 1024];
        WeakReference<Object> weak = new WeakReference<>(retained, queue);
        PhantomReference<Object> phantom = new PhantomReference<>(retained, queue);
        LOG.log(System.Logger.Level.INFO, "阶段=强持有，弱引用仍指向对象={0}", !weak.refersTo(null));
        System.gc();
        LOG.log(System.Logger.Level.INFO, "阶段=请求后仍强持有，弱引用仍指向对象={0}", !weak.refersTo(null));
        retained = null;
        LOG.log(System.Logger.Level.INFO, "阶段=释放强持有，发出第二次回收请求");
        System.gc();
        for (int index = 0; index < 2; index++) {
            Reference<?> notification = queue.remove(1500);
            LOG.log(System.Logger.Level.INFO, "队列观察序号={0}，引用类型={1}", index,
                    notification == null ? "窗口内未通知" : notification.getClass().getSimpleName());
        }
        LOG.log(System.Logger.Level.INFO, "阶段=窗口结束，弱引用已清除={0}，虚引用get={1}",
                weak.refersTo(null), phantom.get());
        showExplicitCleanup();
        // 队列不会替调用者保活尚未入队的引用对象。
        Reference.reachabilityFence(weak);
        Reference.reachabilityFence(phantom);
    }

    /** 显式调用清理动作；动作不捕获被登记对象，且不依赖后台线程调度。 */
    private static void showExplicitCleanup() {
        Cleaner cleaner = Cleaner.create();
        Object owner = new Object();
        Cleaner.Cleanable cleanable = cleaner.register(owner,
                () -> LOG.log(System.Logger.Level.INFO, "清理动作=显式执行；本例没有外部资源"));
        cleanable.clean();
        Reference.reachabilityFence(owner);
    }
}
