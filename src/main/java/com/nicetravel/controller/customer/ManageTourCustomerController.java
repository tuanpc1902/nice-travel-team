package com.nicetravel.controller.customer;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.nicetravel.custom.UserServices;
import com.nicetravel.entity.Account;
import com.nicetravel.entity.Event;
import com.nicetravel.security.auth.CustomOAuth2User;
import com.nicetravel.service.EventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.nicetravel.entity.Booking;
import com.nicetravel.service.AccountService;
import com.nicetravel.service.BookingDetailService;
import com.nicetravel.service.BookingService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
public class ManageTourCustomerController {
    @Autowired
    BookingService bookingService;
    @Autowired
    BookingDetailService bookingDetailService;
    @Autowired
    AccountService accountService;

    @Autowired
    UserServices userServices;

    @Autowired
    EventsService eventsService;

    @Autowired
    private JavaMailSender mailSender;


    @GetMapping("/tour-da-dat")
    public String getManageTour(Model model, HttpServletRequest request, Authentication authentication) {
        Account account = accountService.findAccountsByUsername(request.getRemoteUser()); // remote

        String username = null;

        if (account == null){
            CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
            Account accountOauth = accountService.findByEmail(oauth2User.getEmail());
            username = accountOauth.getUsername();
        }
        else {
            username = account.getUsername();
        }

        model.addAttribute("userRequest", accountService.findAccountsByUsername(username));
        List<Booking> items = bookingService.getAllBookingByAcId(username);
        model.addAttribute("items", items);
        return "/customer/TourDaDat";
    }

//    @RequestMapping("/isDelete/{id}")
//    public String isDelete(@PathVariable("id") Integer id) {
//        Booking booking = bookingService.findById(id);
//        booking.setIsDeleted(true);
//        bookingService.updateBooking(booking);
//
//        return "redirect:/customer/tour-da-dat";
//    }


//	H???y Tour

    @GetMapping("/cancel-tour/{id}")
    public String cancelTour(@PathVariable("id") Integer id, Model model) {
        Booking booking = bookingService.findById(id);
        model.addAttribute("booking", booking);
        model.addAttribute("message", "B???n c?? ch???c mu???n h???y tour ?");
        model.addAttribute("events", new Event());
        return "/customer/CancelTour";
    }

    @PostMapping("/process-cancel-tour")
    public String processCancelTour(Booking booking, @Valid @ModelAttribute("events") Event event, HttpServletRequest request, Model model
            , BindingResult result, RedirectAttributes redirect)
            throws UnsupportedEncodingException, MessagingException {
        String errorMessage = null;
        try{
            if(result.hasErrors()){
                errorMessage = "???? x???y ra l???i khi h???y tour, xin th??? l???i!";
            }
            else {
                userServices.cancelTour(booking, event, getSiteURLBooking(request));
//                model.addAttribute("successMessage", "Vui l??ng ki???m tra email ????? x??c nh???n h???y tour");
                String successMessage = "M???t li??n k???t x??c nh???n h???y tour ???? ???????c g???i ?????n email c???a b???n.";
                redirect.addFlashAttribute("successMessage", successMessage);
            }
        }catch(Exception e){
            e.printStackTrace();
            errorMessage = "Kh??ng th??? c???p nh???t tr???ng th??i cho tour, xin th??? l???i!";
        }

        if (!ObjectUtils.isEmpty(errorMessage)) { // khong null
            redirect.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/customer/tour-da-dat";
    }

    private String getSiteURLBooking(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }


}
