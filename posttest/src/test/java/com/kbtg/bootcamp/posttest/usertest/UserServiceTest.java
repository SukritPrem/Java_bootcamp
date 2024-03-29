package com.kbtg.bootcamp.posttest.usertest;

import com.kbtg.bootcamp.posttest.exception.NotFoundException;
import com.kbtg.bootcamp.posttest.exception.Status200Exception;
import com.kbtg.bootcamp.posttest.lottery.Lottery;
import com.kbtg.bootcamp.posttest.lottery.LotteryRepository;
import com.kbtg.bootcamp.posttest.user.User;
import com.kbtg.bootcamp.posttest.user.UserRepository;
import com.kbtg.bootcamp.posttest.user.UserService;
import com.kbtg.bootcamp.posttest.user.userOperation.UserOperation;
import com.kbtg.bootcamp.posttest.user.user_ticket.UserTicket;
import com.kbtg.bootcamp.posttest.user.user_ticket.UserTicketRepository;
import com.kbtg.bootcamp.posttest.user.user_ticket_store.UserTicketStore;
import com.kbtg.bootcamp.posttest.user.user_ticket_store.UserTicketStoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private LotteryRepository lotteryRepository;

    @Mock
    private UserTicketRepository userTicketRepository;

    @Mock
    private UserTicketStoreService userTicketStoreService;



    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("CheckUserAndLottery"
                + "When User and Lottery Found Success")
    void testCheckUserAndLottery() throws NotFoundException, Status200Exception {
        // Mock user and lottery data
        User user = new User(1,"1234567890","USER","123454321234"); // Assuming admin user is present
        Lottery lottery = new Lottery("123","123456","234");

        when(userRepository.findByuserid("1234567890")).thenReturn(Optional.of(user));
        when(lotteryRepository.findByTicket("123")).thenReturn(Optional.of(lottery));

        UserOperation userOperation = userService.checkUserAndLottery("1234567890", "123");

        assertNotNull(userOperation);
        assertEquals(user, userOperation.getUser());
        assertEquals(lottery, userOperation.getLottery());

        verify(userRepository).findByuserid("1234567890");
        verify(lotteryRepository).findByTicket("123");
    }

    //It's less some case because logic Operation follow By Worker UserTicketStoreService

    @Test
    @DisplayName("CheckUserAndLottery"
            + "When UserOperation Return Buy Status")
    public void testUserBuyTicketWhenActionIsBuy() throws NotFoundException, Status200Exception {
        // Arrange
        SpyUserServiceTest spyUserServiceTest = new SpyUserServiceTest(userRepository,lotteryRepository,userTicketRepository,userTicketStoreService);

        UserOperation userOperation = spyUserServiceTest.checkUserAndLottery("dummy","dummy");

        UserTicket userTicket = new UserTicket(
                userOperation.getUser().getUserId(),
                userOperation.getUser().getRoles(),
                "BUY",
                userOperation.getLottery().getTicket(),
                userOperation.getLottery().getAmount(),
                userOperation.getLottery().getPrice());
        userTicket.setId(1);

        doReturn(userOperation).when(userTicketStoreService).updateUserTicketStoreAndLottery(any());
        doReturn(userTicket).when(userTicketRepository).save(any());

        Integer result = spyUserServiceTest.userBuyTicket(
                userOperation.getUser().getUserId(),
                userOperation.getLottery().getTicket()
        );

        assertEquals(userTicket.getId(), result);

        verify(userTicketStoreService).updateUserTicketStoreAndLottery(any());
        verify(userTicketRepository).save(any());
    }

    @Test
    @DisplayName("CheckUserAndLottery"
            + "When not found user")
    public void testUserBuyTicketWhenUserNotFound()
    {
        Lottery lottery = new Lottery("132","32414","0");
        when(userRepository.findByuserid(anyString())).thenReturn(Optional.empty());
        when(lotteryRepository.findByTicket(anyString())).thenReturn(Optional.of(lottery));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.userBuyTicket("user_id", "ticket_id");
        });

        assertEquals("Error user id not found", exception.getMessage());
    }

    @Test
    @DisplayName("CheckUserAndLottery"
            + "When not found lotter")
    public void testUserBuyTicketWhenLotteryNotFound()
    {
        User user = new User(1,"123456789","1324","42314");
//        Lottery lottery = new Lottery("132","32414","0");
        when(userRepository.findByuserid(anyString())).thenReturn(Optional.of(user));
        when(lotteryRepository.findByTicket(anyString())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.userBuyTicket("user_id", "ticket_id");
        });

        assertEquals("Error lottery not found", exception.getMessage());
    }

    @Test
    @DisplayName( "lottery.checkAmounteqaulZero()" +
           "When lottery Sold out"
    )
    public void testUserBuyTicketWhenLotteryAmountZero()
    {
        User user = new User(1,"123456789","1324","42314");
        Lottery lottery = new Lottery("132","32414","0");
        when(userRepository.findByuserid(anyString())).thenReturn(Optional.of(user));
        when(lotteryRepository.findByTicket(anyString())).thenReturn(Optional.of(lottery));

        Status200Exception exception = assertThrows(Status200Exception.class, () -> {
            userService.userBuyTicket("user_id", "ticket_id");
        });

        assertEquals("Sorry Lottery Sold out.", exception.getMessage());
    }
    @Test
    @DisplayName("CheckUserAndLottery"
            + "When UserOperation Return Buy Status")
    public void testUserBuyTicketWhenLotterySoldOut()
    {
        when(userRepository.findByuserid(anyString())).thenReturn(Optional.empty());
        when(lotteryRepository.findByTicket(anyString())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.userBuyTicket("user_id", "ticket_id");
        });

        assertEquals("Error user id not found", exception.getMessage());
    }

    @Test
    @DisplayName("deleteTicket" +
            "When User delete Ticket success")
    void deleteTicket_Success() throws NotFoundException {

        User user = new User(1,"12345689","USER","23432134321234");
        UserTicketStore userTicketStore = new UserTicketStore(user.getUserId(),"123456","9","27");
        UserTicket expected = new UserTicket(
                userTicketStore.getUserid(),
                "USER",
                "DELETE",
                userTicketStore.getTicket(),
                userTicketStore.getAmount(),
                userTicketStore.getPrice()
        );
        expected.setId(1);
        when(userRepository.findByuserid(user.getUserId())).thenReturn(Optional.of(user));
        doReturn(userTicketStore).when(userTicketStoreService).deleteTicketInUserTicketStore(any(),any());
        when(userTicketRepository.save(any())).thenReturn(expected);

        String result = userService.deleteTicket(user.getUserId(),"123456");

        assertEquals(expected.getTicket(), result);

    }

    @Test
    @DisplayName("deleteTicket"+
            "When User Not found")
    void deleteTicket_UserNotFound() throws NotFoundException {
        // Arrange
        when(userRepository.findByuserid("userId"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.deleteTicket("userId", "ticket"));

        // Verify interactions
        verify(userRepository, times(1)).findByuserid("userId");
        verify(userTicketStoreService, never()).deleteTicketInUserTicketStore(any(), any());
        verify(userTicketRepository, never()).save(any());
    }
}

class SpyUserServiceTest extends UserService
{
    public SpyUserServiceTest(UserRepository userRepository,
                              LotteryRepository lotteryRepository,
                              UserTicketRepository userTicketRepository,
                              UserTicketStoreService userTicketStoreService) {
        super(userRepository, lotteryRepository, userTicketRepository, userTicketStoreService);
    }

    @Override
    public UserOperation checkUserAndLottery(String userId, String ticketId) throws NotFoundException
    {
        User user = new User(1,"1234567890","USER","123454321234"); // Assuming admin user is present
        Lottery lottery = new Lottery("123","123456","234");
        UserTicketStore userTicketStore= new UserTicketStore(user.getUserId(),
                lottery.getTicket(),
                lottery.getPrice(),
                lottery.getAmount());
        UserOperation userOperation = new UserOperation();
        userOperation.setLottery(lottery);
        userOperation.setUser(user);
        userOperation.setUserTicketStore(userTicketStore);
        userOperation.setAction("BUY");
        return userOperation;
    };

    @Override
    public Integer userBuyTicket(String userid, String ticket) throws NotFoundException, Status200Exception {
        return super.userBuyTicket(userid,ticket);
    };

}

