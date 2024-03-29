package com.kbtg.bootcamp.posttest.admin;

import com.kbtg.bootcamp.posttest.lottery.LotteryRequest;
import com.kbtg.bootcamp.posttest.lottery.LotteryService;
import com.kbtg.bootcamp.posttest.user.user_ticket.UserTicket;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final LotteryService lotteryService;


    public AdminController(LotteryService lotteryService){
        this.lotteryService = lotteryService;
    }

    @Transactional
    @PostMapping("/lotteries")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> createLottery(@Valid @RequestBody LotteryRequest lotteryRequest){
        UserTicket userTicket = lotteryService.createNewLotteryByAdmin(lotteryRequest);
        return Map.of("ticket",  userTicket.getTicket());
    }
}
