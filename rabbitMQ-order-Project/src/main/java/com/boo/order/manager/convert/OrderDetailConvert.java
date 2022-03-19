package com.boo.order.manager.convert;

import com.boo.order.manager.dto.OrderMessageDTO;
import com.boo.order.manager.po.OrderDetail;
import com.boo.order.manager.vo.OrderCreateVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderDetailConvert {

  /**
   * entity转换为值对象
   *
   * @param entity 实体
   * @return {@link OrderCreateVO}
   */
  OrderCreateVO entity2ValueObject(OrderDetail entity);

  /**
   * vo转换entity
   *
   * @param entity 实体
   * @return {@link OrderDetail}
   */
  OrderDetail valueObject2Entity(OrderCreateVO entity);

  /**
   * entityTO数据传输对象
   *
   * @param entity 实体
   * @return {@link OrderMessageDTO}
   */
  OrderMessageDTO entity2DataTransferObject(OrderDetail entity);
}
