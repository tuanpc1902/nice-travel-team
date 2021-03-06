package com.nicetravel.repository;

import com.nicetravel.entity.Account;
import com.nicetravel.entity.Total;
import com.nicetravel.entity.Travel;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.nicetravel.entity.TravelTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelRepository extends JpaRepository<Travel, Integer> {
	
	@Query("SELECT u FROM Travel u WHERE u.isDeleted = false AND u.expirationDate = false")
	List<Travel> getAll();
	
	@Query("SELECT count(u.id) FROM Travel u ")
	Integer getcountTravel();

    @Query("SELECT u FROM Travel u WHERE u.slug LIKE %?1%")
    Travel findTravelBySlug(String slug); 
    
	
	  @Query("SELECT t FROM Travel t WHERE t.place LIKE %?1% and t.name LIKE %?2% and t.startDate = CONVERT(DATETIME, ?3 ,103) and price between ?4 and ?5 and t.isDeleted = false AND t.expirationDate = false" )
	  List<Travel> searchTour(String depart, String desti, String sd, BigDecimal pmin, BigDecimal pmax) ;
	  
	  @Query("SELECT t FROM Travel t WHERE t.place LIKE %?1% and t.name LIKE %?2% and t.startDate = CONVERT(DATETIME, ?3 ,103) and price between ?4 and ?5 and t.isDeleted = false AND t.expirationDate = false" )
	  Page<Travel> searchTour2(String depart, String desti, String sd, BigDecimal pmin, BigDecimal pmax, Pageable pageable) ;
	  
	  @Query("SELECT t FROM Travel t WHERE t.place LIKE %?1% and t.name LIKE %?2% and t.price between ?3 and ?4 and t.isDeleted = false AND t.expirationDate = false" )
	  List<Travel> searchTourMinMaxNoDate(String depart, String desti,BigDecimal pmin, BigDecimal pmax);
	  
	  @Query("SELECT t FROM Travel t WHERE t.place LIKE %?1% and t.name LIKE %?2% and t.price between ?3 and ?4 and t.isDeleted = false AND t.expirationDate = false" )
	  Page<Travel> searchTourMinMaxNoDate2(String depart, String desti,BigDecimal pmin, BigDecimal pmax, Pageable pageable);
	  
	/*
	 * @Query("SELECT t FROM Travel t WHERE t.typeId.id=?1") List<Travel>
	 * findByTypeId(Integer tid,Pageable pageable);
	 */
	  //B??? l???c admin
	  @Query("SELECT t FROM Travel t WHERE t.startDate BETWEEN ?1 AND ?2")
	  Page<Travel> filterStartDate(Date day,Date end, Pageable pageable);
	  
	    @Query("SELECT t FROM Travel t WHERE t.typeId.id=?1 AND t.isDeleted = false AND t.expirationDate = false")
		Page<Travel> findByTypeId(Integer tid,Pageable pageable);
    
	    @Query("SELECT t FROM Travel t WHERE t.isDeleted = false AND t.expirationDate = false")
	    Page<Travel> getAll(Pageable pageable);
//    // s??? l?????ng b??n ra 
//    @Query(value = "{CALL sp_getTotalSold()}", nativeQuery = true)
//	List<TotalSold> getTotalSold();
    
    @Query(value = "{CALL sp_getTotalSold()}", nativeQuery = true)
   	List<String[][]> getTotalSold();
   	
   	@Query("SELECT new com.nicetravel.entity.Total (u.name, (100- ((convert(float,u.quantityNew) / convert(float,u.quantity)) * 100)),u.quantity, (u.quantity- u.quantityNew) ) From Travel u order by (100- ((convert(float,u.quantityNew) / convert(float,u.quantity)) * 100)) desc ")
   	List<Total> getTotal();
   	

    @Query(value = "{CALL sp_getTourFavorite()}" , nativeQuery = true)
	  List<Travel> getTourFavorite() ;

	// list tour admin t???t c???
	@Query(value ="SELECT * FROM Travel", nativeQuery = true)
	Page<Travel> findAllByTravel(Pageable page);

	// list tour admin ??ang ho???t ?????ng
	@Query(value ="SELECT * FROM Travel WHERE is_deleted = 0 and expiration_date = 0 ", nativeQuery = true)
	Page<Travel> findAllByTravelActive(Pageable page);

	// list tour admin ng???ng h??
	@Query(value ="SELECT * FROM Travel WHERE is_deleted = 1 ", nativeQuery = true)
	Page<Travel> findAllByTravelNonActive(Pageable page);

	// list tour admin h???t h???n
	@Query(value ="SELECT * FROM Travel WHERE expiration_date = 1 ", nativeQuery = true)
	Page<Travel> findAllByTravelExpires(Pageable page);
   	
//   	@Query("SELECT u FROM Travel u WHERE u.id =?1")
//   	Travel findTravelById(Integer id);
	@Modifying(clearAutomatically =true)
	@Query(value="UPDATE Travel SET name = ?1, departure_place = ?2,place = ?3, price = ?4, start_date= ?5, end_date = ?6, quantity= ?7, quantity_new = ?8, hour = ?9, slug = ?10 ,type_id=?11,account_Id =?12, img = ?13 WHERE id = ?14", nativeQuery = true)
	void updateTravelAdmin(String name, String departurePlace, String place, BigDecimal price, Date start_date, Date end_date, Integer quantity , Integer quantityNew, Integer hour, String slug , TravelTypes typeId, Account account, String img, Integer id);

	@Modifying(clearAutomatically =true)
    @Query(value="UPDATE Travel SET  is_Deleted = 1 WHERE id =?1", nativeQuery = true)
    void deletedTravel(Integer id);


	// danh s??ch tour trong th??ng
	@Query(value="select * from Travel where Month(created_date) = MONTH(GetDate()) and YEAR(created_date) = Year(GetDate()) and is_deleted = 0 and expiration_date =0", nativeQuery = true)
	Page<Travel> getTravelInMonth(Pageable page);


   	
   	// l???y t???ng s??? tour ???? l???t
	@Query(value = "{CALL sp_CountDaLatTour()}" , nativeQuery = true)
	 Integer countDaLatTour() ;

	// update set n???u ch???nh start date th?? kh??ng h???t h???n
	@Modifying(clearAutomatically =true)
	@Query(value = "{CALL sp_updateEXD2()}" , nativeQuery = true)
	void sp_updateEXD2() ;

	// l???y t???ng s??? tour ???? N???ng
	@Query(value = "{CALL sp_CountDaNangTour()}" , nativeQuery = true)
	Integer countDaNangTour() ;
		
	// l???y t???ng s??? tour H??Noi
	@Query(value = "{CALL sp_CountHaNoiTour()}" , nativeQuery = true)
	Integer countHaNoiTour() ;
		
	// l???y t???ng s??? tour Ph?? Qu???c
	@Query(value = "{CALL sp_CountPhuQuocTour()}" , nativeQuery = true)
	Integer countPhuQuocTour() ;

	@Query(value = "{CALL sp_upGetTravelByBookingId(:id)}", nativeQuery = true)
	Travel getTravelByBookingId(@Param("id") Integer id);

	//Update ngay het han
	@Modifying(clearAutomatically = true)
	@Query(value = "{CALL sp_updateEXD()}" , nativeQuery = true)
	void updateEX();

	// th???ng k?? s??? l?????ng tour
	@Query(value = "SELECT created_date, count(id)  "
			+ "					FROM travel "
			+ "					WHERE created_date between ?1 and ?2 "
			+ "					group by created_date",nativeQuery = true)
	String[][] getTotalTravelFromTo(String from, String to);
	
	
	//Th???ng k?? admin
	@Query(value = "{CALL sp_GetTourInMonth()}" , nativeQuery = true)
	Integer countTourInMonth() ;
	
	@Query(value = "{CALL sp_GetTourDaDat()}" , nativeQuery = true)
	Integer countTourDaDat() ;

	@Query(value ="SELECT * From travel where is_deleted = 0",nativeQuery = true)
	List<Travel> findAllTravelAdmin();
	
}
