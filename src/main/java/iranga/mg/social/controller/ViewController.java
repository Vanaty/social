package iranga.mg.social.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import iranga.mg.social.repository.OnlineUserRepository;
import iranga.mg.social.repository.UserRepository;
@Controller
public class ViewController {

	@Autowired
	UserRepository userRepo;

	@Autowired
	OnlineUserRepository onlineUserRepo;

	Logger logger = LoggerFactory.getLogger(ViewController.class);

	@GetMapping("/")
	public String mainPage() {
		return "pages/sign-in";
	}

	@GetMapping("/chat")
	public String privateChatView() {
		return "chat";
	}

	@GetMapping("/sign-in")
	public String signIn() {
		return "pages/sign-in";
	}

	@GetMapping("/sign-up")
	public String signUp() {
		return "pages/sign-up";
	}

	@GetMapping("/disconnect")
	public String disconnect() {
		SecurityContextHolder.clearContext();
		return "index";
	}

	@GetMapping("/create-chat")
	public String createChatView() {
		return "create-chat";
	}

}