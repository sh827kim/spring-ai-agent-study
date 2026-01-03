package org.spark.ai.gourmetagent.config;

import org.spark.ai.gourmetagent.domain.TableType;
import org.spark.ai.gourmetagent.entity.Customer;
import org.spark.ai.gourmetagent.entity.Menu;
import org.spark.ai.gourmetagent.entity.RestaurantTable;
import org.spark.ai.gourmetagent.repository.CustomerRepository;
import org.spark.ai.gourmetagent.repository.MenuRepository;
import org.spark.ai.gourmetagent.repository.RestaurantTableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final RestaurantTableRepository restaurantTableRepository; // 정확한 리포지토리 이름 사용
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository; // 생성자 주입 필요

    public DataLoader(RestaurantTableRepository restaurantTableRepository, CustomerRepository customerRepository, MenuRepository menuRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if(restaurantTableRepository.count()==0){
            restaurantTableRepository.save(new RestaurantTable(1L, 4, TableType.WINDOW));
            restaurantTableRepository.save(new RestaurantTable(2L, 4, TableType.WINDOW));
            restaurantTableRepository.save(new RestaurantTable(3L, 4, TableType.HALL));
            restaurantTableRepository.save(new RestaurantTable(4L, 6, TableType.HALL));
            restaurantTableRepository.save(new RestaurantTable(5L, 8, TableType.ROOM));
            System.out.println("--- [Init] 레스토랑 테이블 5개 생성 완료 ---");
        }
        if(menuRepository.count()==0){
            menuRepository.save(new Menu(101L, "티본 스테이크", 150000, "MAIN"));
            menuRepository.save(new Menu(102L, "봉골레 파스타", 28000, "MAIN"));
            menuRepository.save(new Menu(103L, "트러플 리조또", 35000, "MAIN"));
            menuRepository.save(new Menu(201L, "카베르네 소비뇽", 80000, "WINE"));
            menuRepository.save(new Menu(202L, "샴페인", 120000, "WINE"));
            // [New] 디저트 메뉴 추가!
            menuRepository.save(new Menu(301L, "수제 티라미수", 12000, "DESSERT"));
            System.out.println("--- [Init] 메뉴 데이터 생성 완료 ---");
        }
        if(customerRepository.count()==0){
            customerRepository.save(Customer.builder()
                    .name("홍길동")
                    .phoneNumber("010-0000-0000")
                    .visitCount(10)
                    .memo("레드 와인을 선호하심")
                    .build()
            );
            System.out.println("--- [Init] VIP 고객(홍길동) 생성 완료 ---");
        }
    }
}
