package com.vietnl.sharedlibrary.core.cqrs;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.command.Command;
import com.eps.shared.core.cqrs.command.CommandBus;
import com.eps.shared.core.cqrs.query.Query;
import com.eps.shared.core.cqrs.query.QueryBus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Mediator {

  private final CommandBus commandBus;
  private final QueryBus queryBus;

  /**
   * Gửi command (write operation).
   *
   * @param context header context chứa user info, trace id, v.v.
   * @param command command cần xử lý
   * @param <R>     kiểu kết quả trả về
   * @return kết quả thực thi
   */
  public <R> R send(HeaderContext context, Command<R> command) {
    return commandBus.dispatch(context, command);
  }

  /**
   * Gửi query (read operation).
   *
   * @param context header context chứa user info, trace id, v.v.
   * @param query     query cần xử lý
   * @param <R>     kiểu kết quả trả về
   * @return kết quả truy vấn
   */
  public <R> R query(HeaderContext context, Query<R> query) {
    return queryBus.dispatch(context, query);
  }
}
