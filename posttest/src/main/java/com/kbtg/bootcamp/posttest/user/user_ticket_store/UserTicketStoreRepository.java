package com.kbtg.bootcamp.posttest.user.user_ticket_store;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTicketStoreRepository extends JpaRepository<UserTicketStore, Long> {

    Optional<UserTicketStore> findByUseridAndTicket(String userid, String ticket);


    @Modifying
    @Transactional
    @Query(value ="UPDATE user_ticket_store SET amount =:amount WHERE userid =:userid AND ticket=:ticket", nativeQuery = true)
    void updateAmountByuserIdAndTicket(@Param("amount") String amount, @Param("userid") String userid,
                                       @Param("ticket") String ticket);

    @Modifying
    @Transactional
    @Query(value ="UPDATE user_ticket_store SET price =:price WHERE ticket=:ticket", nativeQuery = true)
    void updatePriceByTicket(@Param("price") String price, @Param("ticket") String ticket);

    List<UserTicketStore> findByuserid(String userid);

    Optional<UserTicketStore> findByticket(String ticket);


    @Modifying
    @Transactional
    @Query(value ="DELETE FROM user_ticket_store WHERE ticket=:ticket AND userid =:userid", nativeQuery = true)
    void deleteTicketByuserId(@Param("ticket") String ticket,@Param("userid") String userid);

    @Modifying
    @Transactional
    @Query(value = "SELECT DISTINCT ticket FROM user_ticket_store WHERE userId=:userId",nativeQuery = true)
    List<String> findDistinctTicketByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query(value = "SELECT * FROM user_ticket_store WHERE userId=:userId", nativeQuery = true)
    List<UserTicketStore> findUserTicketStoreByUserId(@Param("userId") String userId);
}

