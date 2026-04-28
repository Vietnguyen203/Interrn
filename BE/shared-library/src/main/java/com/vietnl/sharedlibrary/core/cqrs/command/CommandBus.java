package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.exception.CommandHandlerNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CommandBus {

  private final List<CommandMiddleware> middlewares;

  private final Map<String, CommandHandler<?, ?>> handlerCache;

  public CommandBus(ApplicationContext applicationContext, List<CommandMiddleware> middlewares) {
    this.middlewares = new ArrayList<>(middlewares);
    AnnotationAwareOrderComparator.sort(this.middlewares);
    this.handlerCache = buildHandlerCache(applicationContext);
    log.info(
        "[CQRS] MVC Initialized with {} command handlers, {} middlewares",
        handlerCache.size(),
        this.middlewares.size());
  }

  @SuppressWarnings("unchecked")
  public <R> R dispatch(HeaderContext context, Command<R> command) {
    CommandHandler<?, ?> handler = handlerCache.get(command.getResolvableType().toString());

    if (handler == null) {
      throw new CommandHandlerNotFoundException(
          "No handler found for command: " + command.getClass().getSimpleName());
    }

    CommandHandlerDelegate<R> pipeline = buildPipeline((CommandHandler<Command<R>, R>) handler);
    return pipeline.handle(context, command);
  }

  private <R> CommandHandlerDelegate<R> buildPipeline(CommandHandler<Command<R>, R> handler) {
    CommandHandlerDelegate<R> chain = handler::handle;

    for (int i = middlewares.size() - 1; i >= 0; i--) {
      final CommandMiddleware middleware = middlewares.get(i);
      final CommandHandlerDelegate<R> next = chain;
      chain = (ctx, cmd) -> middleware.handle(ctx, cmd, next);
    }

    return chain;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<String, CommandHandler<?, ?>> buildHandlerCache(ApplicationContext ctx) {
    Map<String, CommandHandler<?, ?>> cache = new ConcurrentHashMap<>();

    String[] beanNames = ctx.getBeanNamesForType(CommandHandler.class);
    for (String beanName : beanNames) {
      CommandHandler<?, ?> handler = (CommandHandler<?, ?>) ctx.getBean(beanName);

      ResolvableType commandType =
          ResolvableType.forClass(handler.getClass())
              .as(CommandHandler.class)
              .getGeneric(0);

      if (cache.containsKey(commandType.toString())) {
        log.warn(
            "Duplicate CommandHandler for type: {} at bean: {}. Overwriting...",
            commandType,
            beanName);
      }

      cache.put(commandType.toString(), handler);
    }

    return cache;
  }
}
