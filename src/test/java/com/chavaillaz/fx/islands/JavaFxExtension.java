package com.chavaillaz.fx.islands;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

/**
 * Starts JavaFX once for all the tests, and runs the test classes, from their construction to their test methods, on
 * the JavaFX application thread, where the nodes they build and show have to be used.
 * <p>
 * JavaFX runs on its headless platform, which needs no display, so that the tests run the same on a build server as on
 * a desktop, unless another platform is given with {@code -Dglass.platform}.
 */
public final class JavaFxExtension implements BeforeAllCallback, InvocationInterceptor {

    private static final AtomicBoolean STARTED = new AtomicBoolean();

    @Override
    public void beforeAll(ExtensionContext context) throws InterruptedException {
        if (STARTED.compareAndSet(false, true)) {
            if (System.getProperty("glass.platform") == null) {
                System.setProperty("glass.platform", "Headless");
                System.setProperty("prism.order", "sw");
            }
            CountDownLatch started = new CountDownLatch(1);
            Platform.startup(started::countDown);
            Platform.setImplicitExit(false);
            started.await();
        }
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation,
                                               ReflectiveInvocationContext<Constructor<T>> invocationContext,
                                               ExtensionContext extensionContext) throws Throwable {
        return proceedOnFxThread(invocation);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
                                          ReflectiveInvocationContext<Method> invocationContext,
                                          ExtensionContext extensionContext) throws Throwable {
        proceedOnFxThread(invocation);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        proceedOnFxThread(invocation);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
                                            ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) throws Throwable {
        proceedOnFxThread(invocation);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext,
                                         ExtensionContext extensionContext) throws Throwable {
        proceedOnFxThread(invocation);
    }

    private static <T> T proceedOnFxThread(Invocation<T> invocation) throws Throwable {
        CompletableFuture<T> done = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                done.complete(invocation.proceed());
            } catch (Throwable failure) {
                done.completeExceptionally(failure);
            }
        });
        try {
            return done.get();
        } catch (ExecutionException failure) {
            throw failure.getCause();
        }
    }
}
