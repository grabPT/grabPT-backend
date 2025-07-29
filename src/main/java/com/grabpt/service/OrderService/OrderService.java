package com.grabpt.service.OrderService;

import com.grabpt.domain.entity.Order;
import com.grabpt.domain.entity.Users;

public interface OrderService {
	// 주문 정보 저장 (사용자 정보와 프론트에서 제공해준 Payment를 입력받는다)
	Order order(Users users);

	Order customOrder(Users user, Long price, String itemName);

}
