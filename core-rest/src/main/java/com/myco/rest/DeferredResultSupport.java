package com.myco.rest;

import org.springframework.web.context.request.async.DeferredResult;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;

public class DeferredResultSupport {

  public static <T> DeferredResult<T> from(final CompletableFuture<T> future) {
    final DeferredResult<T> deferred = new DeferredResult<>();
    future.thenAccept(deferred::setResult);
    future.exceptionally(ex -> {
      if (ex instanceof CompletionException) {
        deferred.setErrorResult(ex.getCause());
      }
      else {
        deferred.setErrorResult(ex);
      }
      return null;
    });
    return deferred;
  }

  public static <R, E extends Throwable, Q> BiConsumer<R, E> completeQuery(Q query) {
    return (response, throwable) -> {
      if (throwable != null) {
        if (RuntimeException.class.isAssignableFrom(throwable.getClass())) {
          throw (RuntimeException) throwable;
        }
        else {
          throw new RuntimeException(throwable);
        }
      }
      if (response == null || (Optional.class.isAssignableFrom(response.getClass()) && !((Optional) response)
          .isPresent())) {
        throw new NoSuchElementException("No results for: " + query);
      }
    };
  }
}
