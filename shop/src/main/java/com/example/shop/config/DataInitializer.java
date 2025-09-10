package com.example.shop.config;

import com.example.shop.entity.Banner;
import com.example.shop.entity.Category;
import com.example.shop.entity.Merchant;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import com.example.shop.repository.BannerRepository;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.MerchantRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private MerchantRepository merchantRepository;
	
	@Autowired
	private BannerRepository bannerRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("=== 检查数据库初始化状态 ===");
		
		// 检查是否已经有数据
		if (userRepository.count() > 0) {
			System.out.println("数据库已有数据，跳过初始化");
			return;
		}
		
		System.out.println("数据库为空，开始初始化...");
		initializeData();
	}
	
	private void initializeData() {
		// 创建管理员用户
		User admin = new User();
		admin.setUsername("admin");
		admin.setPasswordHash(passwordEncoder.encode("123456"));
		admin.setNickname("系统管理员");
		admin.setRole(User.UserRole.ADMIN);
		admin.setStatus(User.UserStatus.ENABLED);
		userRepository.save(admin);
		System.out.println("创建管理员用户: admin/123456");
		
		// 创建商家用户
		User merchant = new User();
		merchant.setUsername("merchant");
		merchant.setPasswordHash(passwordEncoder.encode("123456"));
		merchant.setNickname("测试商家");
		merchant.setRole(User.UserRole.MERCHANT);
		merchant.setStatus(User.UserStatus.ENABLED);
		userRepository.save(merchant);
		System.out.println("创建商家用户: merchant/123456");
		
		// 创建普通用户
		User testuser = new User();
		testuser.setUsername("testuser");
		testuser.setPasswordHash(passwordEncoder.encode("123456"));
		testuser.setNickname("测试用户");
		testuser.setRole(User.UserRole.USER);
		testuser.setStatus(User.UserStatus.ENABLED);
		userRepository.save(testuser);
		System.out.println("创建普通用户: testuser/123456");
		
		// 创建商家记录
		Merchant merchantEntity = new Merchant();
		merchantEntity.setUser(merchant);
		merchantEntity.setShopName("测试零食店");
		merchantEntity.setShopDescription("专业销售各种零食");
		merchantEntity.setStatus(Merchant.MerchantStatus.APPROVED);
		merchantRepository.save(merchantEntity);
		
		// 创建分类
		Category snacks = new Category();
		snacks.setName("零食");
		snacks.setSortOrder(1);
		snacks.setStatus(Category.CategoryStatus.ENABLED);
		categoryRepository.save(snacks);
		
		Category drinks = new Category();
		drinks.setName("饮料");
		drinks.setSortOrder(2);
		drinks.setStatus(Category.CategoryStatus.ENABLED);
		categoryRepository.save(drinks);
		
		// 创建商品
		Product product1 = new Product();
		product1.setName("薯片");
		product1.setSubtitle("香脆可口");
		product1.setCategory(snacks);
		product1.setPrice(new BigDecimal("9.9"));
		product1.setUnit("包");
		product1.setStock(100);
		product1.setStatus(Product.ProductStatus.ON_SALE);
		product1.setDescription("精选土豆制作，香脆可口");
		productRepository.save(product1);
		
		Product product2 = new Product();
		product2.setName("可乐");
		product2.setSubtitle("经典口味");
		product2.setCategory(drinks);
		product2.setPrice(new BigDecimal("3.5"));
		product2.setUnit("瓶");
		product2.setStock(200);
		product2.setStatus(Product.ProductStatus.ON_SALE);
		product2.setDescription("经典可乐口味，清爽解渴");
		productRepository.save(product2);
		
		// 创建轮播图
		Banner banner1 = new Banner();
		banner1.setTitle("精选零食");
		banner1.setImageUrl("https://via.placeholder.com/800x400/FF6B6B/FFFFFF?text=精选零食");
		banner1.setLinkUrl("/products");
		banner1.setSortOrder(1);
		banner1.setStatus(Banner.BannerStatus.ENABLED);
		bannerRepository.save(banner1);
		
		Banner banner2 = new Banner();
		banner2.setTitle("新品上市");
		banner2.setImageUrl("https://via.placeholder.com/800x400/4ECDC4/FFFFFF?text=新品上市");
		banner2.setLinkUrl("/products");
		banner2.setSortOrder(2);
		banner2.setStatus(Banner.BannerStatus.ENABLED);
		bannerRepository.save(banner2);
		
		System.out.println("=== 数据初始化完成 ===");
	}
}
