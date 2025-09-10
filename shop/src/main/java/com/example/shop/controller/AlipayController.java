package com.example.shop.controller;

import com.example.shop.service.AlipayService;
import com.example.shop.service.OrderService;
import com.example.shop.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/alipay", "/api/alipay"})
public class AlipayController {

	@Autowired
	private AlipayService alipayService;

	@Autowired
	private OrderService orderService;

	@PostMapping("/create")
	public ResponseEntity<?> createPayment(@RequestParam String orderNo) {
		try {
			Order order = orderService.getOrderByOrderNo(orderNo);
			if (order == null) {
				Map<String, Object> resp = new HashMap<>();
				resp.put("code", 400);
				resp.put("success", false);
				resp.put("message", "订单不存在");
				return ResponseEntity.ok(resp);
			}
			if (order.getStatus() != Order.OrderStatus.UNPAID) {
				Map<String, Object> resp = new HashMap<>();
				resp.put("code", 400);
				resp.put("success", false);
				resp.put("message", "订单状态不正确");
				return ResponseEntity.ok(resp);
			}
			String html = alipayService.createPaymentPage(order);
			Map<String, Object> resp = new HashMap<>();
			resp.put("code", 200);
			resp.put("success", true);
			resp.put("paymentPage", html);
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			Map<String, Object> resp = new HashMap<>();
			resp.put("code", 400);
			resp.put("success", false);
			resp.put("message", "创建支付页面失败: " + e.getMessage());
			return ResponseEntity.ok(resp);
		}
	}

	/**
	 * 直接返回支付宝收银台表单HTML，前端可直接以新窗口/页面打开，无需再注入并提交表单。
	 * 用法：GET /alipay/create-page?orderNo=xxx
	 */
	@GetMapping(value = "/create-page", produces = "text/html;charset=UTF-8")
	public ResponseEntity<String> createPaymentPage(@RequestParam String orderNo) {
		try {
			Order order = orderService.getOrderByOrderNo(orderNo);
			if (order == null || order.getStatus() != Order.OrderStatus.UNPAID) {
				return ResponseEntity.badRequest().body("<h3>订单不存在或状态不可支付</h3>");
			}
			String html = alipayService.createPaymentPage(order);
			return ResponseEntity.ok(html);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("<h3>创建支付页面失败: " + e.getMessage() + "</h3>");
		}
	}

	@PostMapping("/notify")
	public String notify(HttpServletRequest request) {
		try {
			System.out.println("[Alipay][Notify] received notify call");
			request.getParameterMap().forEach((k, v) -> {
				System.out.println("[Alipay][Notify] " + k + "=" + String.join(",", v));
			});
			return alipayService.handleNotify(request.getParameterMap());
		} catch (Exception e) {
			System.out.println("[Alipay][Notify] exception: " + e.getMessage());
			return "fail";
		}
	}

	@GetMapping(value = "/return", produces = "text/html;charset=UTF-8")
	public ResponseEntity<String> returnUrl(HttpServletRequest request) {
		String orderNo = request.getParameter("out_trade_no");
		try {
			// 将GET参数转为Map<String,String>并验签，通过则直接标记订单为已支付（同步兜底）
			Map<String, String[]> requestParams = request.getParameterMap();
			Map<String, String> params = new HashMap<>();
			requestParams.forEach((k, v) -> {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < v.length; i++) {
					sb.append(v[i]);
					if (i < v.length - 1) sb.append(",");
				}
				params.put(k, sb.toString());
			});
			boolean signOk = alipayService.verifyNotify(params);
			String tradeStatus = params.getOrDefault("trade_status", "");
			if (signOk && ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))) {
				try {
					orderService.markPaidByGateway(orderNo, params.getOrDefault("trade_no", ""));
				} catch (Exception ignored) { }
			}
		} catch (Exception ignored) {}
		
		// 返回带有跳过ngrok警告与禁止缓存的HTML，并做强制跳转
		HttpHeaders headers = new HttpHeaders();
		headers.add("ngrok-skip-browser-warning", "true");
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		String frontendUrl = "http://localhost:3000/orders?paid=1&orderNo=" + orderNo;
		String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付完成</title></head>"
				+ "<body style='font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #f5f5f5;'>"
				+ "<div style='background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 400px; margin: 0 auto;'>"
				+ "<h2 style='color: #67C23A; margin-bottom: 20px;'>✅ 支付完成</h2>"
				+ "<p style='color: #606266; margin: 10px 0;'>订单号：" + orderNo + "</p>"
				+ "<p style='color: #409EFF; margin: 20px 0;'>正在跳转到订单页面...</p>"
				+ "<div style='margin: 20px 0;'>"
				+ "<a href='" + frontendUrl + "' style='display: inline-block; padding: 10px 20px; background: #409EFF; color: white; text-decoration: none; border-radius: 4px; margin: 5px;'>查看订单</a>"
				+ "<button onclick='closeWindow()' style='padding: 10px 20px; background: #909399; color: white; border: none; border-radius: 4px; margin: 5px; cursor: pointer;'>关闭窗口</button>"
				+ "</div>"
				+ "</div>"
				+ "<script>"
				+ "function closeWindow() {"
				+ "  try { window.close(); } catch(e) { window.location.href = 'about:blank'; }"
				+ "}"
				+ "console.log('支付完成回跳(return)，开始跳转...');"
				+ "try {"
				+ "  if (window.opener && !window.opener.closed) {"
				+ "    window.opener.postMessage({ type: 'ALIPAY_PAYMENT_SUCCESS', orderNo: '" + orderNo + "' }, '*');"
				+ "    window.opener.location.href = '" + frontendUrl + "';"
				+ "    setTimeout(function(){ try { window.close(); } catch(e){} }, 1000);"
				+ "  } else {"
				+ "    setTimeout(function(){ window.location.href='" + frontendUrl + "'; }, 2000);"
				+ "  }"
				+ "} catch(e) {"
				+ "  setTimeout(function(){ window.location.href='" + frontendUrl + "'; }, 2000);"
				+ "}"
				+ "setTimeout(function(){ if (window.location.href.includes('/alipay/return')) {"
				+ "  var p=document.createElement('p'); p.style.color='#409EFF'; p.innerText='如果没有自动跳转，请点击上方按钮'; document.body.appendChild(p); } }, 5000);"
				+ "</script>"
				+ "</body></html>";
		return ResponseEntity.ok().headers(headers).body(html);
	}

	/**
	 * 支付宝支付完成后的重定向端点
	 * 这个端点专门用于处理支付宝回调并重定向到前端
	 */
	@GetMapping(value = "/redirect", produces = "text/html;charset=UTF-8")
	public ResponseEntity<String> redirectUrl(HttpServletRequest request) {
		// 添加ngrok跳过警告的响应头
		HttpHeaders headers = new HttpHeaders();
		headers.add("ngrok-skip-browser-warning", "true");
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		String orderNo = request.getParameter("out_trade_no");
		System.out.println("[Alipay][Redirect] Processing redirect for order: " + orderNo);
		
		try {
			// 处理支付回调逻辑
			Map<String, String[]> requestParams = request.getParameterMap();
			Map<String, String> params = new HashMap<>();
			requestParams.forEach((k, v) -> {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < v.length; i++) {
					sb.append(v[i]);
					if (i < v.length - 1) sb.append(",");
				}
				params.put(k, sb.toString());
			});
			
			boolean signOk = alipayService.verifyNotify(params);
			String tradeStatus = params.getOrDefault("trade_status", "");
			System.out.println("[Alipay][Redirect] Sign OK: " + signOk + ", Trade Status: " + tradeStatus);
			
			if (signOk && ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))) {
				try {
					orderService.markPaidByGateway(orderNo, params.getOrDefault("trade_no", ""));
					System.out.println("[Alipay][Redirect] Order marked as paid: " + orderNo);
				} catch (Exception e) {
					System.out.println("[Alipay][Redirect] Error marking order as paid: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			System.out.println("[Alipay][Redirect] Error processing redirect: " + e.getMessage());
		}
		
		// 创建一个优化的页面，实现自动关闭和跳转
		String frontendUrl = "http://localhost:3000/orders?paid=1&orderNo=" + orderNo;
		
		// 添加调试信息
		System.out.println("[Alipay][Redirect] Frontend URL: " + frontendUrl);
		System.out.println("[Alipay][Redirect] Headers: " + headers);
		String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付完成</title></head>"
				+ "<body style='font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #f5f5f5;'>"
				+ "<div style='background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 400px; margin: 0 auto;'>"
				+ "<h2 style='color: #67C23A; margin-bottom: 20px;'>✅ 支付完成</h2>"
				+ "<p style='color: #606266; margin: 10px 0;'>订单号：" + orderNo + "</p>"
				+ "<p style='color: #409EFF; margin: 20px 0;'>正在跳转到订单页面...</p>"
				+ "<div style='margin: 20px 0;'>"
				+ "<a href='" + frontendUrl + "' style='display: inline-block; padding: 10px 20px; background: #409EFF; color: white; text-decoration: none; border-radius: 4px; margin: 5px;'>查看订单</a>"
				+ "<button onclick='closeWindow()' style='padding: 10px 20px; background: #909399; color: white; border: none; border-radius: 4px; margin: 5px; cursor: pointer;'>关闭窗口</button>"
				+ "</div>"
				+ "</div>"
				+ "<script>"
				+ "function closeWindow() {"
				+ "  try { window.close(); } catch(e) { window.location.href = 'about:blank'; }"
				+ "}"
				+ ""
				+ "// 自动跳转逻辑"
				+ "console.log('开始处理支付完成跳转...');"
				+ "console.log('订单号:', '" + orderNo + "');"
				+ "console.log('前端URL:', '" + frontendUrl + "');"
				+ ""
				+ "// 立即尝试通知父窗口"
				+ "try {"
				+ "  if (window.opener && !window.opener.closed) {"
				+ "    console.log('检测到父窗口，发送消息并跳转父窗口...');"
				+ "    // 通知父窗口支付完成"
				+ "    window.opener.postMessage({ type: 'ALIPAY_PAYMENT_SUCCESS', orderNo: '" + orderNo + "' }, '*');"
				+ "    // 直接跳转父窗口"
				+ "    window.opener.location.href = '" + frontendUrl + "';"
				+ "    // 延迟关闭当前窗口"
				+ "    setTimeout(function() {"
				+ "      try { window.close(); } catch(e) { console.log('关闭窗口失败:', e); }"
				+ "    }, 1000);"
				+ "  } else {"
				+ "    console.log('没有父窗口，直接跳转当前窗口');"
				+ "    // 延迟跳转，让用户看到支付完成信息"
				+ "    setTimeout(function() {"
				+ "      window.location.href = '" + frontendUrl + "';"
				+ "    }, 2000);"
				+ "  }"
				+ "} catch(e) {"
				+ "  console.log('跳转失败，使用备用方案:', e);"
				+ "  // 备用方案：延迟跳转"
				+ "  setTimeout(function() {"
				+ "    window.location.href = '" + frontendUrl + "';"
				+ "  }, 2000);"
				+ "}"
				+ ""
				+ "// 备用方案：如果5秒后还在这个页面，显示手动跳转"
				+ "setTimeout(function() {"
				+ "  if (window.location.href.includes('/alipay/redirect')) {"
				+ "    document.querySelector('p[style*=\"color: #409EFF\"]').innerHTML = '如果页面没有自动跳转，请点击上方按钮';"
				+ "  }"
				+ "}, 5000);"
				+ "</script>"
				+ "</body></html>";
		
		return ResponseEntity.ok().headers(headers).body(html);
	}
}
