package cpw.mods.sidedtest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

class SidedExecutorTest {
    public static class GoodProxy {
        static Object PROXY = SidedExecutor.runSided(() -> GoodClass::new, () -> BadClass::new);
    }

    public static class BadProxy {
        static Object PROXY = SidedExecutor.runSided(() -> BadClass::new, () -> GoodClass::new);
    }

    @BeforeEach
    void setUp() {
        SidedExecutor.side = SidedExecutor.Side.CLIENT;
    }

    @Test
    void runOn() {
        final Supplier<Callable<BadClass>> badSupplier = () -> BadClass::new;
        Executable goodSide = () -> SidedExecutor.runOn(SidedExecutor.Side.SERVER, badSupplier);
        Executable badSide = () -> SidedExecutor.runOn(SidedExecutor.Side.CLIENT, badSupplier);

        Assertions.assertAll(goodSide);
        Assertions.assertThrows(ExceptionInInitializerError.class, badSide);
    }

    @Test
    void runSided() {
        Executable runSided = () -> SidedExecutor.runSided(()-> GoodClass::new, ()->BadClass::new);
        SidedExecutor.side = SidedExecutor.Side.CLIENT;
        // when we call for client side, we're good
        Assertions.assertAll(runSided);
        SidedExecutor.side = SidedExecutor.Side.SERVER;
        // when we call for server side, we're bad
        Assertions.assertThrows(ExceptionInInitializerError.class, runSided);
    }

    @Test
    void staticInitSided() {
        Executable goodProxy = () -> GoodProxy.PROXY.toString();
        SidedExecutor.side = SidedExecutor.Side.CLIENT;
        // when we call for client side, we're good
        Assertions.assertAll(goodProxy);
        Executable badProxy = () -> BadProxy.PROXY.toString();
        SidedExecutor.side = SidedExecutor.Side.CLIENT;
        // when we call for client side, we're good
        Assertions.assertThrows(ExceptionInInitializerError.class, badProxy);
    }
}