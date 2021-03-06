package com.nicetravel.controller.customer;

import com.nicetravel.controller.admin.FileUploadUtil;
import com.nicetravel.custom.UserServices;
import com.nicetravel.entity.Account;
import com.nicetravel.security.auth.CustomOAuth2User;
import com.nicetravel.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/customer")
public class InformationCustomerController {

	private final AccountService accountService;

	private final UserServices userServices;

	private final PasswordEncoder passwordEncoder;


	@Autowired
	public InformationCustomerController(AccountService accountService, UserServices userServices, PasswordEncoder passwordEncoder) {
		this.accountService = accountService;
		this.userServices = userServices;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/information-customer")
	public String getInformationCustomer(Model model, HttpServletRequest request, Authentication authentication) {

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

		model.addAttribute("account", accountService.findAccountsByUsername(username));
		return "customer/InformationCustomer";
	}
	
	@GetMapping("/edit-information-customer")
	public String getEditInformationCustomer(HttpServletRequest request, Model model, Authentication authentication) {
//		Account account = accountService.findAccountsByUsername(request.getRemoteUser()); // remote

		String username = userServices.getUserName(request, authentication);
		Account userRequest = accountService.findAccountsByUsername(username);
		model.addAttribute("userRequest", userRequest);
		return "/customer/EditInformationCustomer";
	}

	@PostMapping("/edit-information-customer")
	public String update(@Valid @ModelAttribute(name = "userRequest") Account userRequest,
						 BindingResult result,
						 RedirectAttributes redirect, @RequestParam("fileImage") MultipartFile multipartFile, HttpServletRequest request, Authentication authentication) {
		String username = userServices.getUserName(request, authentication);
		String errorMessage = null;

		Account account = accountService.findAccountsByUsername(username);
		String password = account.getPassword();
		System.out.println("password: " + account.getPassword());

		try {
			// check if userRequest is not valid
			if (result.hasErrors()) {
				errorMessage = "T??i kho???n kh??ng h???p l???";
				System.out.println(errorMessage);
				redirect.addFlashAttribute("errorMessage", errorMessage);
			} else {
				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
				if (fileName.equals("") || fileName.length() == 0 || fileName == null){
					System.out.println("accountImg: " + account.getImg());
					account.setImg(account.getImg());
					account.setPassword(password);
				}
				else {
					account.setImg(fileName);
				}

				accountService.update(account);
				accountService.update(userRequest);

				System.out.println("Account update: " + account);

				String uploadDir = "photos/" + "accounts/" + userRequest.getUsername();

				Path uploadPath = Paths.get(uploadDir);

				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				try (InputStream inputStream = multipartFile.getInputStream()) {
					Path filePath = uploadPath.resolve(fileName);
					Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new IOException("Could not save upload file: " + fileName);
				}


				FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
//				accountService.update(userRequest);
				String successMessage = "T??i kho???n " + userRequest.getFullname() + " ???? ???????c c???p nh???t";
				redirect.addFlashAttribute("successMessage", successMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = "Kh??ng th??? c???p nh???t t??i kho???n " + userRequest.getFullname() + " , vui l??ng th??? l???i!";
		}

		if (!ObjectUtils.isEmpty(errorMessage)) { // khong null
			redirect.addFlashAttribute("errorMessage", errorMessage);
		}
		return "redirect:/customer/information-customer";
	}

	@GetMapping("/change-password")
	public String getChangePassword(HttpServletRequest request, Model model, Authentication authentication) {
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

		Account userRequest = accountService.findAccountsByUsername(username);
		model.addAttribute("userRequest", userRequest);
		return "/customer/ChangePassword";
	}

	@PostMapping("/change-password")
	public String postChangePassword(HttpServletRequest request,
									 Model model, RedirectAttributes ra, Authentication authentication) throws Exception {

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


		Account acc = accountService.findAccountsByUsername(username);

		String oldPassword = request.getParameter("oldPassword");
		String newPassword = request.getParameter("newPassword");

		model.addAttribute("pageTitle", "Thay ?????i m???t kh???u ???? h???t h???n");

		if (oldPassword.equals(newPassword)) {
			ra.addFlashAttribute("message", "M???t kh???u m???i c???a b???n ph???i kh??c m???t kh???u c??.");
			System.out.println("M???t kh???u m???i c???a b???n ph???i kh??c m???t kh???u c??.");
			return "redirect:/customer/change-password";
		}

		if (!passwordEncoder.matches(oldPassword, acc.getPassword())) {
			ra.addFlashAttribute("message", "M???t kh???u c?? c???a b???n kh??ng ch??nh x??c.");
			System.out.println("M???t kh???u c?? c???a b???n kh??ng ch??nh x??c.");
			return "redirect:/customer/change-password";

		} else {
			userServices.changePassword(acc, passwordEncoder.encode(newPassword));
			request.logout();
			ra.addFlashAttribute("message", "B???n ???? ?????i m???t kh???u th??nh c??ng. "
					+ "Vui l??ng ????ng nh???p l???i.");

			return "redirect:/login";
		}
	}

}
